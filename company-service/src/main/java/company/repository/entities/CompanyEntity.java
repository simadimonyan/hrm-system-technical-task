package company.repository.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_table")
public class CompanyEntity {

    @Id
    private UUID id;

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
    private List<UUID> employeeIds;

}
