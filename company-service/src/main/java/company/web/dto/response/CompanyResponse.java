package company.web.dto.response;

import company.web.dto.response.contracts.Company;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompanyResponse extends Company {
    private UUID id;
    private String name;
    private String budget;
    private List<UUID> employeeIds;
}
