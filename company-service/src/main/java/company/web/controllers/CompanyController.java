package company.web.controllers;

import company.repository.entities.CompanyEntity;
import company.service.CompanyService;
import company.service.configurations.DiscoveryConfiguration;
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

    @Autowired
    public CompanyController(DiscoveryConfiguration discoveryConfiguration, CompanyService companyService, WebClient.Builder builder) {
        this.EMPLOYEE_SERVICE = discoveryConfiguration.getEmployeeService();
        this.companyService = companyService;
        this.webClient = builder.build();
    }

    @PostMapping
    private ResponseEntity<String> createCompany(@RequestBody CompanyRequest request) throws Exception {

        UUID id = UUID.randomUUID();

        // company employees
        if (!(request.getEmployeeIds().isEmpty())) {
            updateEmployeesCompany(id, request);
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
            clearEmployeesCompany(result);
            updateEmployeesCompany(id, request);
        }

        companyService.updateCompany(id, request);
        return ResponseEntity.ok("Successfully updated!");
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<String> deleteCompany(@PathVariable UUID id) {
        var result = companyService.readCompany(id);

//        if (!result.getEmployeeIds().isEmpty()) {
//            // company employees
//            clearEmployeesCompany(result);
//        }

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

    /**
     * Changes employee's company
     * @param id UUID of company
     * @param request company data
     */
    private void updateEmployeesCompany(UUID id, CompanyRequest request) {
        request.getEmployeeIds().forEach(eId -> {
            // get employee
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

            // if exception - has handler = Not Found
            assert employee != null;
            employee.setCompanyId(id);

            // change employee's company
            webClient.put().uri(EMPLOYEE_SERVICE + "employees/" + eId)
                    .bodyValue(employee)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                            errorBody -> Mono.error(new RuntimeException("CompanyService: " + errorBody))
                        )
                    )
                    .toBodilessEntity()
                    .block();
        });
    }

    /**
     * Clear employee's company
     * @param entity company data
     */
    private void clearEmployeesCompany(CompanyEntity entity) {
        entity.getEmployeeIds().forEach(eId -> {
            // get employee
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

            // if exception - has handler = Not Found
            assert employee != null;
            employee.setCompanyId(null);

            // change employee's company
            webClient.put().uri(EMPLOYEE_SERVICE + "employees/" + eId)
                    .bodyValue(employee)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                            errorBody -> Mono.error(new RuntimeException("CompanyService: " + errorBody))
                        )
                    )
                    .toBodilessEntity()
                    .block();
        });
    }

}
