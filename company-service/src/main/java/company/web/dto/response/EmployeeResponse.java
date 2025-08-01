package company.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
}
