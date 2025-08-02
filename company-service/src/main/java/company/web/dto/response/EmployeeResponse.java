package company.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeResponse {
    private String firstName;
    private String lastName;
    private String phone;
    private UUID companyId;
}
