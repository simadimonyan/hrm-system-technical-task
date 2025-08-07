package company.service.company;

import company.repository.CompanyRepository;
import company.repository.entities.CompanyEntity;
import company.web.dto.request.CompanyRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public void createCompany(UUID id, CompanyRequest request) throws Exception {

        // company existence check
        if (companyRepository.findByName(request.getName()).isPresent()) {
            throw new Exception("Company name: \"" + request.getName() + "\" already registered");
        }

        CompanyEntity companyEntity = new CompanyEntity(
            id,
            request.getName(),
            request.getBudget(),
            request.getEmployeeIds()
        );
        companyRepository.saveAndFlush(companyEntity);
    }

    public CompanyEntity readCompany(UUID id) {
        return companyRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Company not found with id: " + id));
    }

    @Transactional
    public void updateCompany(UUID id, CompanyRequest request) {
        CompanyEntity companyEntity = companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));
        companyEntity.setName(request.getName());
        companyEntity.setBudget(request.getBudget());
        companyEntity.setEmployeeIds(request.getEmployeeIds());
        companyRepository.saveAndFlush(companyEntity);
    }

    @Transactional
    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
        companyRepository.flush();
    }

    @Transactional
    public void addCompanyEmployee(UUID companyId, UUID employeeId) {
        CompanyEntity companyEntity = companyRepository.findById(companyId).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + companyId));
        var ids = companyEntity.getEmployeeIds();
        ids.add(employeeId);
        companyEntity.setEmployeeIds(ids);
        companyRepository.saveAndFlush(companyEntity);
    }

    @Transactional
    public void removeCompanyEmployee(UUID companyId, UUID employeeId) {
        CompanyEntity companyEntity = companyRepository.findById(companyId).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + companyId));
        var ids = companyEntity.getEmployeeIds();
        ids.remove(employeeId);
        companyEntity.setEmployeeIds(ids);
        companyRepository.saveAndFlush(companyEntity);
    }

    public Page<CompanyEntity> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

}
