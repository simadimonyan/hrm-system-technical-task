package company.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompanyFullResponse {
    private Long id;
    private String name;
    private String budget;
    private List<EmployeeResponse> employees;
}
