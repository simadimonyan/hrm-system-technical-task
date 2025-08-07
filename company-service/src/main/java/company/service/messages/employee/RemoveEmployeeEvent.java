package company.service.messages.employee;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RemoveEmployeeEvent {
    private UUID companyId;
    private UUID employeeId;
}