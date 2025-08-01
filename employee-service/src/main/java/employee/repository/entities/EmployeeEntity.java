package employee.repository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

    public EmployeeEntity(String firstName, String lastName, String phone, String companyId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.companyId = companyId;
    }

}
