package company.service.messages.company;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ChangeCompanyEvent {
    private UUID employeeId;
    private UUID companyId;
}
