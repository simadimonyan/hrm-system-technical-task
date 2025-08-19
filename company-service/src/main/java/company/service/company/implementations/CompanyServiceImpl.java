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
import company.web.dto.response.mappers.CompanyMapper;
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
    private final CompanyMapper companyMapper;


    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository, KafkaProducerService kafkaProducerService, EmployeeClient employeeClient, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.employeeClient = employeeClient;
        this.companyMapper = companyMapper;
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

        CompanyEntity companyEntity = companyMapper.toEntity(id, request);
        log.info("Returning created company: {}", companyEntity);
        return companyRepository.saveAndFlush(companyEntity);
    }

    @Override
    public Company readCompany(UUID id, boolean extraInfo) {

        CompanyEntity company = findCompanyOrThrow(id);

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
            CompanyFullResponse response = companyMapper.toFullResponse(company, employees);
            log.info("Returning company: {}", response);
            return response;
        }
        CompanyResponse response = companyMapper.toResponse(company);
        log.info("Returning company: {}", response);
        return response;
    }

    @Override
    @Transactional
    public CompanyEntity updateCompany(UUID id, CompanyRequest request) {

        CompanyEntity company = findCompanyOrThrow(id);

        // request idempotency check
        if (company.getEmployeeIds() != request.getEmployeeIds() && request.getEmployeeIds() != null) {
            company.getEmployeeIds().forEach(eId ->
                    kafkaProducerService.sendClearCompany(new ClearCompanyEvent(eId))
            );
            request.getEmployeeIds().forEach(eId ->
                    kafkaProducerService.sendChangeCompany(new ChangeCompanyEvent(eId, id))
            );
        }

        CompanyEntity companyEntity = findCompanyOrThrow(id);
        companyEntity.setName(request.getName());
        companyEntity.setBudget(request.getBudget());
        companyEntity.setEmployeeIds(request.getEmployeeIds());
        log.info("Returning updated company: {}", companyEntity);
        return companyRepository.saveAndFlush(companyEntity);
    }

    @Override
    @Transactional
    public CompanyEntity deleteCompany(UUID id) {
        CompanyEntity companyEntity = findCompanyOrThrow(id);
        companyRepository.delete(companyEntity);
        companyRepository.flush();
        log.info("Returning deleted company: {}", companyEntity);
        return companyEntity;
    }

    @Transactional
    public void addCompanyEmployee(UUID companyId, UUID employeeId) {
        CompanyEntity companyEntity = findCompanyOrThrow(companyId);
        List<UUID> ids = companyEntity.getEmployeeIds();
        ids.add(employeeId);
        companyEntity.setEmployeeIds(ids);
        companyRepository.saveAndFlush(companyEntity);
    }

    @Transactional
    public void removeCompanyEmployee(UUID companyId, UUID employeeId) {
        CompanyEntity companyEntity = findCompanyOrThrow(companyId);
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

                companyResponses.add(companyMapper.toFullResponse(company, employees));
            }

            Page<CompanyFullResponse> response = new PageImpl<>(companyResponses, pageable, page.getTotalElements());
            log.info("Returning all companies: {}", response);
            return response;
        }

        Page<CompanyResponse> response = page.map(companyMapper::toResponse);
        log.info("Returning all companies: {}", response);
        return response;
    }

    /**
     * Utility method
     * Finds CompanyEntity or throws CompanyNotFoundException
     * @param id company
     * @return CompanyEntity
     */
    private CompanyEntity findCompanyOrThrow(UUID id) {
        return companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));
    }

}
