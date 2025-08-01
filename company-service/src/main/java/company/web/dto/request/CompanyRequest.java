package company.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompanyRequest {
    private String name;
    private String budget;
    private List<Long> employeeIds;
}
