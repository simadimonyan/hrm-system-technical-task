package employee.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeFullResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private CompanyResponse company;
}
