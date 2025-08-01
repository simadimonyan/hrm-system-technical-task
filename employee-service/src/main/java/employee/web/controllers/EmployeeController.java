package employee.web.controllers;

import employee.repository.entities.EmployeeEntity;
import employee.service.EmployeeService;
import employee.service.configurations.DiscoveryConfiguration;
import employee.web.dto.request.CompanyRequest;
import employee.web.dto.request.EmployeeRequest;
import employee.web.dto.response.CompanyResponse;
import employee.web.dto.response.EmployeeFullResponse;
import employee.web.dto.response.EmployeeResponse;
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
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final String COMPANY_SERVICE;
    private final WebClient webClient;


    @Autowired
    public EmployeeController(DiscoveryConfiguration discoveryConfiguration, EmployeeService employeeService, WebClient.Builder builder) {
        this.COMPANY_SERVICE = discoveryConfiguration.getCompanyService();
        this.employeeService = employeeService;
        this.webClient = builder.build();
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeRequest request) {

        UUID id = UUID.randomUUID();

        if (request.getCompanyId() != null) {
            // company updates
            try {
                addEmployee(id, request);
            } catch (Exception ignored) {
                log.info("Company: " + request.getCompanyId() + " for employee: " + id + " does not exist");
            }
        }

        employeeService.createEmployee(id, request);
        return ResponseEntity.ok("Successfully created!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean extraInfo) {
        var entity = employeeService.readEmployee(id);

        // extraInfo - company data
        if (extraInfo) {
            // company service data request by id
            try {
                CompanyResponse company = webClient.get()
                        .uri(COMPANY_SERVICE + "companies/" + entity.getCompanyId())
                        .retrieve()
                        .onStatus(
                                status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                                        errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                                )
                        )
                        .bodyToMono(CompanyResponse.class)
                        .block();

                return ResponseEntity.ok(new EmployeeFullResponse(
                        entity.getId(),
                        entity.getFirstName(),
                        entity.getLastName(),
                        entity.getPhone(),
                        company
                ));
            } catch (Exception e) {
                return ResponseEntity.ok(new EmployeeFullResponse(
                        entity.getId(),
                        entity.getFirstName(),
                        entity.getLastName(),
                        entity.getPhone(),
                        null
                ));
            }
        }

        return ResponseEntity.ok(new EmployeeResponse(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getPhone(),
            entity.getCompanyId()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable UUID id, @RequestBody EmployeeRequest request) {
        var result = employeeService.readEmployee(id);

        // request idempotency check
        if (request.getCompanyId() != null && !request.getCompanyId().equals(result.getCompanyId())) {
            if (result.getCompanyId() != null) removeEmployee(id, result);
            log.info("---1 {} {} {} {}", request.getFirstName(), request.getLastName(), request.getPhone(), request.getCompanyId());
            addEmployee(id, request);
        }

        employeeService.updateEmployee(id, request);
        return ResponseEntity.ok("Successfully updated!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID id) {
        var result = employeeService.readEmployee(id);

//        if (result.getCompanyId() != null) {
//            // company updates
//            removeEmployee(id, result);
//        }

        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Successfully deleted!");
    }

    @GetMapping("/all")
    public ResponseEntity<Page<?>> getAllEmployees(Pageable pageable, @RequestParam(defaultValue = "false") boolean extraInfo) {
        Page<EmployeeEntity> page = employeeService.getAllEmployees(pageable);

        // extraInfo - company data
        if (extraInfo) {

            List<EmployeeFullResponse> responses = new ArrayList<>();

            // company service data request by id
            for (var employee : page) {
                try {

                    CompanyResponse company = webClient.get()
                            .uri(COMPANY_SERVICE + "companies/" + employee.getCompanyId())
                            .retrieve()
                            .onStatus(
                                    status -> status.is4xxClientError() || status.is5xxServerError(),
                                    clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                                            errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                                    )
                            )
                            .bodyToMono(CompanyResponse.class)
                            .block();

                    responses.add(new EmployeeFullResponse(
                            employee.getId(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getPhone(),
                            company
                    ));

                } catch (Exception e) {
                    responses.add(new EmployeeFullResponse(
                            employee.getId(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            employee.getPhone(),
                            null
                    ));
                }
            }
            Page<EmployeeFullResponse> response = new PageImpl<>(responses, pageable, page.getTotalElements());
            return ResponseEntity.ok(response);
        }

        Page<EmployeeResponse> response = page.map(employee ->
                new EmployeeResponse(
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getPhone(),
                        employee.getCompanyId()
                )
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Adds employee's id to the company
     * @param id employee's id
     * @param request employee's data
     */
    private void addEmployee(UUID id, EmployeeRequest request) {

        // company service data request by id
        CompanyResponse company = webClient.get()
                .uri(COMPANY_SERVICE + "companies/" + request.getCompanyId())
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                        errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                    )
                )
                .bodyToMono(CompanyResponse.class)
                .block();

        // if exception - has handler = Not Found
        assert company != null;
        List<UUID> employees = company.getEmployeesIds() == null ? new ArrayList<>() : new ArrayList<>(company.getEmployeesIds());
        log.info("---2.1 {}", employees);
        employees.add(id);
        log.info("---2.2 {}", employees);

        CompanyRequest body = new CompanyRequest(
                company.getName(),
                company.getBudget(),
                employees
        );

        log.info("---3 {} {} {}", body.getName(), body.getBudget(), body.getEmployeeIds());

        // update company employees
        webClient.put().uri(COMPANY_SERVICE + "companies/" + request.getCompanyId())
                .bodyValue(body)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                        errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                    )
                )
                .toBodilessEntity()
                .block();

    }

    /**
     * Removes employee's id from the company
     * @param id employee's id
     * @param entity employee's data
     */
    private void removeEmployee(UUID id, EmployeeEntity entity) {

        // company service data request by id
        CompanyResponse company = webClient.get()
                .uri(COMPANY_SERVICE + "companies/" + entity.getCompanyId())
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                        errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                    )
                )
                .bodyToMono(CompanyResponse.class)
                .block();

        // if exception - has handler = Not Found
        assert company != null;
        List<UUID> employees = company.getEmployeesIds() == null ? new ArrayList<>() : new ArrayList<>(company.getEmployeesIds());
        employees.remove(id);
        company.setEmployeesIds(employees);

        // update company employees
        webClient.put().uri(COMPANY_SERVICE + "companies/" + entity.getCompanyId())
                .bodyValue(company)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                        errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                    )
                )
                .toBodilessEntity()
                .block();

    }

}
