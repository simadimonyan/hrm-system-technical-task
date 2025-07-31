package company.web.dto;

import java.util.List;

public class CompanyResponse {
    private Long id;
    private String name;
    private String budget;
    private List<EmployeeResponse> employees;
}
