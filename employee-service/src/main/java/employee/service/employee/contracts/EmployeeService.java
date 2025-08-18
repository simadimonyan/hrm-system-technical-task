package employee.service.employee.contracts;

import employee.repository.entities.EmployeeEntity;
import employee.web.dto.request.EmployeeRequest;
import employee.web.dto.response.contracts.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmployeeService {
    EmployeeEntity createEmployee(EmployeeRequest request);
    Employee readEmployee(UUID id, Boolean extraInfo);

    EmployeeEntity updateEmployee(UUID id, EmployeeRequest request);
    EmployeeEntity deleteEmployee(UUID id);

    Page<? extends Employee> getAllEmployees(Pageable pageable, Boolean extraInfo);
}
