package employee.web;

import employee.service.EmployeeService;
import employee.web.dto.EmployeeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public EmployeeResponse createEmployee() {
        return new EmployeeResponse();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployee(@PathVariable Long id) {
        return new EmployeeResponse();
    }

    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(@PathVariable Long id) {
        return new EmployeeResponse();
    }

    @DeleteMapping("/{id}")
    public EmployeeResponse deleteEmployee(@PathVariable Long id) {
        return new EmployeeResponse();
    }

    @GetMapping
    public EmployeeResponse getEmployees() {
        return new EmployeeResponse();
    }

}
