package company.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CompanyResponse {
    private UUID id;
    private String name;
    private String budget;
    private List<UUID> employeeIds;
}
