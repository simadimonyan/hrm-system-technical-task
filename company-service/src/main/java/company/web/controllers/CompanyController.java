package company.web.controllers;

import company.repository.entities.CompanyEntity;
import company.service.company.CompanyService;
import company.service.kafka.KafkaProducerService;
import company.service.configurations.DiscoveryConfiguration;
import company.service.messages.company.ChangeCompanyEvent;
import company.service.messages.company.ClearCompanyEvent;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.CompanyFullResponse;
import company.web.dto.response.CompanyResponse;
import company.web.dto.response.EmployeeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final String EMPLOYEE_SERVICE;
    private final WebClient webClient;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public CompanyController(DiscoveryConfiguration discoveryConfiguration, CompanyService companyService, WebClient.Builder builder, KafkaProducerService kafkaProducerService) {
        this.EMPLOYEE_SERVICE = discoveryConfiguration.getEmployeeService();
        this.companyService = companyService;
        this.webClient = builder.build();
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping
    private ResponseEntity<String> createCompany(@RequestBody CompanyRequest request) throws Exception {

        UUID id = UUID.randomUUID();

        // company employees
        if (!(request.getEmployeeIds().isEmpty())) {
            request.getEmployeeIds().forEach(eId ->
                kafkaProducerService.sendChangeCompany(new ChangeCompanyEvent(eId, id))
            );
        }

        companyService.createCompany(id, request);
        return ResponseEntity.ok("Successfully created!");
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> getCompany(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean extraInfo) {
        var result = companyService.readCompany(id);

        // extraInfo - employees data
        if (extraInfo) {
            List<EmployeeResponse> employees = new ArrayList<>();

            // employee ids requests
            for (var eId : result.getEmployeeIds()) {
                try {
                    EmployeeResponse employee = webClient.get()
                            .uri(EMPLOYEE_SERVICE + "employees/" + eId)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                                            errorBody -> Mono.error(new RuntimeException("CompanyService: " + errorBody))
                                    )
                            )
                            .bodyToMono(EmployeeResponse.class)
                            .block();
                    employees.add(employee);
                } catch (Exception ignored) {}
            }

            return ResponseEntity.ok(
                new CompanyFullResponse(
                    result.getId(),
                    result.getName(),
                    result.getBudget(),
                    employees
                )
            );
        }

        return ResponseEntity.ok(
            new CompanyResponse(
                result.getId(),
                result.getName(),
                result.getBudget(),
                result.getEmployeeIds()
            )
        );
    }

    @PutMapping("/{id}")
    private ResponseEntity<String> updateCompany(@PathVariable UUID id, @RequestBody CompanyRequest request) {
        var result = companyService.readCompany(id);
        log.info("--- {} {} {}", request.getName(), request.getBudget(), request.getEmployeeIds());

        // request idempotency check
        if (result.getEmployeeIds() != request.getEmployeeIds() && request.getEmployeeIds() != null) {
            result.getEmployeeIds().forEach(eId ->
                kafkaProducerService.sendClearCompany(new ClearCompanyEvent(eId))
            );
            request.getEmployeeIds().forEach(eId ->
                kafkaProducerService.sendChangeCompany(new ChangeCompanyEvent(eId, id))
            );
        }

        companyService.updateCompany(id, request);
        return ResponseEntity.ok("Successfully updated!");
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<String> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok("Successfully deleted!");
    }

    @GetMapping("/all")
    public ResponseEntity<Page<?>> getAllCompanies(Pageable pageable, @RequestParam(defaultValue = "false") boolean extraInfo) {
        Page<CompanyEntity> page = companyService.getAllCompanies(pageable);

        if (extraInfo) {
            List<CompanyFullResponse> companyResponses = new ArrayList<>();

            for (CompanyEntity company : page) {
                List<EmployeeResponse> employees = new ArrayList<>();

                for (UUID eId : company.getEmployeeIds()) {
                    try {
                        EmployeeResponse employee = webClient.get()
                                .uri(EMPLOYEE_SERVICE + "employees/" + eId)
                                .retrieve()
                                .onStatus(
                                        status -> status.is4xxClientError() || status.is5xxServerError(),
                                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                                                errorBody -> Mono.error(new RuntimeException("CompanyService: " + errorBody))
                                        )
                                )
                                .bodyToMono(EmployeeResponse.class)
                                .block();
                        employees.add(employee);
                    } catch (Exception ignored) {}
                }

                companyResponses.add(
                    new CompanyFullResponse(
                            company.getId(),
                            company.getName(),
                            company.getBudget(),
                            employees
                    )
                );
            }

            Page<CompanyFullResponse> response = new PageImpl<>(companyResponses, pageable, page.getTotalElements());
            return ResponseEntity.ok(response);
        }

        Page<CompanyResponse> response = page.map(company ->
            new CompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.getBudget(),
                    company.getEmployeeIds()
                ));
        return ResponseEntity.ok(response);
    }


}
