package company.service.messages.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEmployeeEvent {
    private UUID companyId;
    private UUID employeeId;
}

