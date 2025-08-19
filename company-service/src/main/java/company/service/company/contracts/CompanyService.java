package company.service.company.contracts;

import company.repository.entities.CompanyEntity;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.contracts.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompanyService {
    CompanyEntity createCompany(CompanyRequest request);
    Company readCompany(UUID id, boolean extraInfo);
    CompanyEntity updateCompany(UUID id, CompanyRequest request);

    CompanyEntity deleteCompany(UUID id);

    Page<? extends Company> getAllCompanies(Pageable pageable, boolean extraInfo);
}
