package company.service;

import company.repository.CompanyRepository;
import company.repository.entities.CompanyEntity;
import company.web.dto.request.CompanyRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public void createCompany(CompanyRequest request) {
        CompanyEntity companyEntity = new CompanyEntity(
            request.getName(),
            request.getBudget(),
            request.getEmployeeIds()

        );
        companyRepository.saveAndFlush(companyEntity);
    }

    public CompanyEntity readCompany(Long id) {
        return companyRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException("Company not found with id: " + id));
    }

    @Transactional
    public void updateCompany(Long id, CompanyRequest request) {
        CompanyEntity companyEntity = companyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Company not found with id: " + id));
        companyEntity.setName(request.getName());
        companyEntity.setBudget(request.getBudget());
        companyEntity.setEmployeeIds(request.getEmployeeIds());
        companyRepository.saveAndFlush(companyEntity);
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
        companyRepository.flush();
    }

}
