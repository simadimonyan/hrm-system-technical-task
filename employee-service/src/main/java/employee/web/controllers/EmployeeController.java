package employee.web.controllers;

import employee.repository.entities.EmployeeEntity;
import employee.service.company.CompanyClient;
import employee.service.employee.EmployeeService;
import employee.service.kafka.KafkaProducerService;
import employee.service.messages.employee.AddEmployeeEvent;
import employee.service.messages.employee.RemoveEmployeeEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    private final CompanyClient companyClient;

    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, KafkaProducerService kafkaProducerService, CompanyClient companyClient) {
        this.employeeService = employeeService;
        this.kafkaProducerService = kafkaProducerService;
        this.companyClient = companyClient;
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeRequest request) {

        UUID id = UUID.randomUUID();

        if (request.getCompanyId() != null) {
            // company updates
            try {
                kafkaProducerService.sendAddEmployee(new AddEmployeeEvent(request.getCompanyId(), id));
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
                CompanyResponse company = companyClient.getCompany(entity.getCompanyId());

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
            if (result.getCompanyId() != null) {
                kafkaProducerService.sendRemoveEmployee(new RemoveEmployeeEvent(request.getCompanyId(), id));
            }
            kafkaProducerService.sendAddEmployee(new AddEmployeeEvent(request.getCompanyId(), id));
        }

        employeeService.updateEmployee(id, request);
        return ResponseEntity.ok("Successfully updated!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable UUID id) {
        var result = employeeService.readEmployee(id);

        if (result.getCompanyId() != null) {
            // company updates
            kafkaProducerService.sendRemoveEmployee(new RemoveEmployeeEvent(result.getCompanyId(), id));
        }

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

                    CompanyResponse company = companyClient.getCompany(employee.getCompanyId());

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

}
