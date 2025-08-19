package company.web.dto.response.mappers;

import company.repository.entities.CompanyEntity;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.CompanyFullResponse;
import company.web.dto.response.CompanyResponse;
import company.web.dto.response.EmployeeResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CompanyMapper {

    public CompanyEntity toEntity(UUID id, CompanyRequest request) {
        return new CompanyEntity(
            id,
            request.getName(),
            request.getBudget(),
            request.getEmployeeIds()
        );
    }

    public CompanyResponse toResponse(CompanyEntity entity) {
        return new CompanyResponse(
            entity.getId(),
            entity.getName(),
            entity.getBudget(),
            entity.getEmployeeIds()
        );
    }

    public CompanyFullResponse toFullResponse(CompanyEntity entity, List<EmployeeResponse> employeeResponses) {
        return new CompanyFullResponse(
                entity.getId(),
                entity.getName(),
                entity.getBudget(),
                employeeResponses
        );
    }

}
