package company.service.messages.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCompanyEvent {
    private UUID employeeId;
    private UUID companyId;
}
