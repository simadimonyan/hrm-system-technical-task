package employee.service.employee.implementations;

import employee.repository.EmployeeRepository;
import employee.repository.entities.EmployeeEntity;
import employee.service.company.CompanyClient;
import employee.service.employee.contracts.EmployeeService;
import employee.service.kafka.KafkaProducerService;
import employee.service.messages.employee.AddEmployeeEvent;
import employee.service.messages.employee.RemoveEmployeeEvent;
import employee.web.dto.request.EmployeeRequest;
import employee.web.dto.response.CompanyResponse;
import employee.web.dto.response.EmployeeFullResponse;
import employee.web.dto.response.EmployeeResponse;
import employee.web.dto.response.contracts.Employee;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyClient companyClient;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, KafkaProducerService kafkaProducerService, CompanyClient companyClient) {
        this.employeeRepository = employeeRepository;
        this.companyClient = companyClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public EmployeeEntity createEmployee(EmployeeRequest request) {

        UUID id = UUID.randomUUID();

        if (request.getCompanyId() != null) {
            // company updates
            try {
                kafkaProducerService.sendAddEmployee(new AddEmployeeEvent(request.getCompanyId(), id));
            } catch (Exception ignored) {
                log.info("Company: " + request.getCompanyId() + " for employee: " + id + " does not exist");
            }
        }

        EmployeeEntity employee = new EmployeeEntity(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getCompanyId()
        );
        return employeeRepository.saveAndFlush(employee);
    }

    @Override
    public Employee readEmployee(UUID id, Boolean extraInfo) {

        EmployeeEntity entity = employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee not found with id: " + id));

        // extraInfo - company data
        if (extraInfo) {

            // company service data request by id
            try {
                CompanyResponse company = companyClient.getCompany(entity.getCompanyId());

                return new EmployeeFullResponse(
                        entity.getId(),
                        entity.getFirstName(),
                        entity.getLastName(),
                        entity.getPhone(),
                        company
                );

            } catch (Exception e) {
                return new EmployeeFullResponse(
                        entity.getId(),
                        entity.getFirstName(),
                        entity.getLastName(),
                        entity.getPhone(),
                        null
                );
            }
        }

        return new EmployeeResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getCompanyId()
        );
    }

    @Transactional
    public EmployeeEntity updateEmployee(UUID id, EmployeeRequest request) {

        var result = employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee not found with id: " + id));

        // request idempotency check
        if (request.getCompanyId() != null && !request.getCompanyId().equals(result.getCompanyId())) {
            if (result.getCompanyId() != null) {
                kafkaProducerService.sendRemoveEmployee(new RemoveEmployeeEvent(request.getCompanyId(), id));
            }
            kafkaProducerService.sendAddEmployee(new AddEmployeeEvent(request.getCompanyId(), id));
        }

        EmployeeEntity employee = employeeRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Employee not found with id: " + id));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setCompanyId(request.getCompanyId());

        return employeeRepository.saveAndFlush(employee);
    }

    @Transactional
    public EmployeeEntity deleteEmployee(UUID id) {
        var result = employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee not found with id: " + id));

        if (result.getCompanyId() != null) {
            // company updates
            kafkaProducerService.sendRemoveEmployee(new RemoveEmployeeEvent(result.getCompanyId(), id));
        }

        employeeRepository.deleteById(id);
        employeeRepository.flush();
        return result;
    }

    @Transactional
    public void changeCompany(UUID employeeId, UUID companyId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Employee not found with id: " + employeeId));
        employee.setCompanyId(companyId);
        employeeRepository.saveAndFlush(employee);
    }

    @Transactional
    public void clearCompany(UUID employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Employee not found with id: " + employeeId));
        employee.setCompanyId(null);
        employeeRepository.saveAndFlush(employee);
    }

    @Override
    public Page<? extends Employee> getAllEmployees(Pageable pageable, Boolean extraInfo) {

        Page<EmployeeEntity> page = employeeRepository.findAll(pageable);

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
            return new PageImpl<>(responses, pageable, page.getTotalElements());
        }

        return page.map(employee ->
                new EmployeeResponse(
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getPhone(),
                        employee.getCompanyId()
                )
        );
    }

}
