package company.service.company.implementations;

import company.repository.CompanyRepository;
import company.repository.entities.CompanyEntity;
import company.service.company.contracts.CompanyService;
import company.service.employee.EmployeeClient;
import company.service.kafka.KafkaProducerService;
import company.service.messages.company.ChangeCompanyEvent;
import company.service.messages.company.ClearCompanyEvent;
import company.web.controllers.exceptions.CompanyAlreadyRegisteredException;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.CompanyFullResponse;
import company.web.dto.response.CompanyResponse;
import company.web.dto.response.EmployeeResponse;
import company.web.dto.response.contracts.Company;
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
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final KafkaProducerService kafkaProducerService;

    private final EmployeeClient employeeClient;

    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository, KafkaProducerService kafkaProducerService, EmployeeClient employeeClient) {
        this.companyRepository = companyRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.employeeClient = employeeClient;
    }

    @Override
    @Transactional
    public CompanyEntity createCompany(CompanyRequest request) {

        UUID id = UUID.randomUUID();

        // company employees
        if (!(request.getEmployeeIds().isEmpty())) {
            request.getEmployeeIds().forEach(eId ->
                kafkaProducerService.sendChangeCompany(new ChangeCompanyEvent(eId, id))
            );
        }

        // company existence check
        if (companyRepository.findByName(request.getName()).isPresent()) {
            throw new CompanyAlreadyRegisteredException(request.getName());
        }

        CompanyEntity companyEntity = new CompanyEntity(
            id,
            request.getName(),
            request.getBudget(),
            request.getEmployeeIds()
        );

        return companyRepository.saveAndFlush(companyEntity);
    }

    @Override
    public Company readCompany(UUID id, boolean extraInfo) {

        CompanyEntity company = companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));

        // extraInfo - employees data
        if (extraInfo) {
            List<EmployeeResponse> employees = new ArrayList<>();

            // employee ids requests
            for (UUID eId : company.getEmployeeIds()) {
                try {
                    EmployeeResponse employee = employeeClient.getEmployee(eId);
                    employees.add(employee);
                } catch (Exception ignored) {}
            }

            return new CompanyFullResponse(
                    company.getId(),
                    company.getName(),
                    company.getBudget(),
                    employees
                    );
        }

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getBudget(),
                company.getEmployeeIds()
        );
    }

    @Override
    @Transactional
    public CompanyEntity updateCompany(UUID id, CompanyRequest request) {

        CompanyEntity company = companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));

        log.info("--- {} {} {}", request.getName(), request.getBudget(), request.getEmployeeIds());

        // request idempotency check
        if (company.getEmployeeIds() != request.getEmployeeIds() && request.getEmployeeIds() != null) {
            company.getEmployeeIds().forEach(eId ->
                    kafkaProducerService.sendClearCompany(new ClearCompanyEvent(eId))
            );
            request.getEmployeeIds().forEach(eId ->
                    kafkaProducerService.sendChangeCompany(new ChangeCompanyEvent(eId, id))
            );
        }

        CompanyEntity companyEntity = companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));
        companyEntity.setName(request.getName());
        companyEntity.setBudget(request.getBudget());
        companyEntity.setEmployeeIds(request.getEmployeeIds());
        return companyRepository.saveAndFlush(companyEntity);
    }

    @Override
    @Transactional
    public CompanyEntity deleteCompany(UUID id) {
        CompanyEntity companyEntity = companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));
        companyRepository.delete(companyEntity);
        companyRepository.flush();
        return companyEntity;
    }

    @Transactional
    public void addCompanyEmployee(UUID companyId, UUID employeeId) {
        CompanyEntity companyEntity = companyRepository.findById(companyId).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + companyId));
        List<UUID> ids = companyEntity.getEmployeeIds();
        ids.add(employeeId);
        companyEntity.setEmployeeIds(ids);
        companyRepository.saveAndFlush(companyEntity);
    }

    @Transactional
    public void removeCompanyEmployee(UUID companyId, UUID employeeId) {
        CompanyEntity companyEntity = companyRepository.findById(companyId).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + companyId));
        List<UUID> ids = companyEntity.getEmployeeIds();
        ids.remove(employeeId);
        companyEntity.setEmployeeIds(ids);
        companyRepository.saveAndFlush(companyEntity);
    }

    @Override
    public Page<? extends Company> getAllCompanies(Pageable pageable, boolean extraInfo) {

        Page<CompanyEntity> page = companyRepository.findAll(pageable);

        if (extraInfo) {
            List<CompanyFullResponse> companyResponses = new ArrayList<>();

            for (CompanyEntity company : page) {
                List<EmployeeResponse> employees = new ArrayList<>();

                for (UUID eId : company.getEmployeeIds()) {

                    try {
                        EmployeeResponse employee = employeeClient.getEmployee(eId);
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
            return response;
        }

        Page<CompanyResponse> response = page.map(company ->
                new CompanyResponse(
                        company.getId(),
                        company.getName(),
                        company.getBudget(),
                        company.getEmployeeIds()
                ));
        return response;
    }

}
