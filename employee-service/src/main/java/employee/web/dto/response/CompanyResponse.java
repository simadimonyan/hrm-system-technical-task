package employee.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String budget;
}
