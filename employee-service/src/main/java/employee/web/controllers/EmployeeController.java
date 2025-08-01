package employee.web.controllers;

import employee.service.EmployeeService;
import employee.web.dto.request.EmployeeRequest;
import employee.web.dto.response.EmployeeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody EmployeeRequest request) {
        employeeService.createEmployee(
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getCompanyId()
        );
        return ResponseEntity.ok("Successfully created!");
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployee(@PathVariable Long id) {
        var entity = employeeService.readEmployee(id);
        return new EmployeeResponse(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getPhone(),
            entity.getCompanyId()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        employeeService.updateEmployee(id, request);
        return ResponseEntity.ok("Successfully updated!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Successfully deleted!");
    }

}
