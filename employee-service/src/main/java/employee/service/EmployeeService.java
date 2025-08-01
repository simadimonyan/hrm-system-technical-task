package employee.service;

import employee.repository.EmployeeRepository;
import employee.repository.entities.EmployeeEntity;
import employee.web.dto.request.EmployeeRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public void createEmployee(String firstName, String lastName, String phone, String companyId) {
        EmployeeEntity employee = new EmployeeEntity(firstName, lastName, phone, companyId);
        employeeRepository.saveAndFlush(employee);
    }

    public EmployeeEntity readEmployee(Long id) {
        return employeeRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Employee not found with id: " + id));
    }

    @Transactional
    public void updateEmployee(Long id, EmployeeRequest request) {
        EmployeeEntity employee = employeeRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Employee not found with id: " + id));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setCompanyId(request.getCompanyId());
        employeeRepository.saveAndFlush(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
        employeeRepository.flush();
    }

}
