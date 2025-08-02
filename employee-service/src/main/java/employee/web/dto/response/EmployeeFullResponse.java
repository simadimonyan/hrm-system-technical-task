package employee.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeFullResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private CompanyResponse company;
}
