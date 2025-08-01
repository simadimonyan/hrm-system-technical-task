package company.repository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "company_table")
public class CompanyEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "company_name")
    private String name;

    @Column(name = "budget")
    private String budget;

    @ElementCollection
    @CollectionTable(
            name = "company_employee_ids",
            joinColumns = @JoinColumn(name = "company_id")
    )
    @Column(name = "employee_id")
    private List<Long> employeeIds;

    public CompanyEntity(String name, String budget, List<Long> employeeIds) {
        this.name = name;
        this.budget = budget;
        this.employeeIds = employeeIds;
    }

}
