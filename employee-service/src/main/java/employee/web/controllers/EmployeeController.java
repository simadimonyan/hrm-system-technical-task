package employee.web.controllers;

import employee.repository.entities.EmployeeEntity;
import employee.service.employee.contracts.EmployeeService;
import employee.web.dto.request.EmployeeRequest;
import employee.web.dto.response.contracts.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public EmployeeEntity createEmployee(@RequestBody EmployeeRequest request) {
        log.info("Received request to create employee: {}", request);
        return employeeService.createEmployee(request);
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean extraInfo) {
        log.info("Received request to get employee: {}, {}", id, extraInfo);
        return employeeService.readEmployee(id, extraInfo);
    }

    @PutMapping("/{id}")
    public EmployeeEntity updateEmployee(@PathVariable UUID id, @RequestBody EmployeeRequest request) {
        log.info("Received request to update employee: {}, {}", id, request);
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    public EmployeeEntity deleteEmployee(@PathVariable UUID id) {
        log.info("Received request to delete employee: {}", id);
        return employeeService.deleteEmployee(id);
    }

    @GetMapping("/all")
    public Page<? extends Employee> getAllEmployees(Pageable pageable, @RequestParam(defaultValue = "false") boolean extraInfo) {
        log.info("Received request to get all employees: {}, {}", pageable, extraInfo);
        return employeeService.getAllEmployees(pageable, extraInfo);
    }

}
