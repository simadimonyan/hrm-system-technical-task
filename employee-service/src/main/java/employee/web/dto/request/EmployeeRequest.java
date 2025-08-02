package employee.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private UUID companyId;
}
