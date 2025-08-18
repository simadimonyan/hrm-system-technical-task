package employee.web.dto.response.mappers;

import employee.repository.entities.EmployeeEntity;
import employee.web.dto.request.EmployeeRequest;
import employee.web.dto.response.CompanyResponse;
import employee.web.dto.response.EmployeeFullResponse;
import employee.web.dto.response.EmployeeResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeMapper {

    public EmployeeEntity toEntity(UUID id, EmployeeRequest request) {
        return new EmployeeEntity(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getCompanyId()
        );
    }

    public EmployeeResponse toResponse(EmployeeEntity entity) {
        return new EmployeeResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getCompanyId()
        );
    }

    public EmployeeFullResponse toFullResponse(EmployeeEntity entity, CompanyResponse company) {
        return new EmployeeFullResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                company
        );
    }

}
