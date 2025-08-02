package employee.service;

import employee.repository.EmployeeRepository;
import employee.repository.entities.EmployeeEntity;
import employee.web.dto.request.EmployeeRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public void createEmployee(UUID id, EmployeeRequest request) {
        EmployeeEntity employee = new EmployeeEntity(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getCompanyId()
        );
        employeeRepository.saveAndFlush(employee);
    }

    public EmployeeEntity readEmployee(UUID id) {
        return employeeRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Employee not found with id: " + id));
    }

    @Transactional
    public void updateEmployee(UUID id, EmployeeRequest request) {
        EmployeeEntity employee = employeeRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Employee not found with id: " + id));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setCompanyId(request.getCompanyId());
        employeeRepository.saveAndFlush(employee);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        employeeRepository.deleteById(id);
        employeeRepository.flush();
    }

    public Page<EmployeeEntity> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

}
