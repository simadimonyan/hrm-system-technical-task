package employee.service.messages.company;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ClearCompanyEvent {
    private UUID employeeId;
}
