package company.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CompanyRequest {
    private String name;
    private String budget;
    private List<UUID> employeeIds;
}
