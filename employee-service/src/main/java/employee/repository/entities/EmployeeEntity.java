package employee.repository.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employee_table")
public class EmployeeEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phone;

    @Column(name = "company_id")
    private String companyId;

}
