package employee.web.dto.response;

import employee.web.dto.response.contracts.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmployeeResponse extends Employee {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID companyId;
}
