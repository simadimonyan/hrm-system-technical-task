package employee.web.dto.request;

import lombok.Data;

@Data
public class EmployeeRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private String companyId;

    public EmployeeRequest(String firstName, String lastName, String phone, String companyId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.companyId = companyId;
    }

}
