package employee.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone is required") // +7 (xxx) xxx-xx-xx
    @Pattern(regexp = "^\\+\\d{1,3}\\s?(\\(\\d+\\))?[\\d\\s\\-]{4,}$", message = "Phone must be a valid number")
    private String phone;

    private UUID companyId;
}
