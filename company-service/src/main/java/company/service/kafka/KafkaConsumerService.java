package company.service.kafka;

import company.service.company.CompanyService;
import company.service.messages.employee.AddEmployeeEvent;
import company.service.messages.employee.RemoveEmployeeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {
    private final CompanyService companyService;

    @Autowired
    public KafkaConsumerService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @KafkaListener(topics = "company-add-employee", groupId = "employee-service")
    public void handleAdd(AddEmployeeEvent event) {
        companyService.addCompanyEmployee(event.getCompanyId(), event.getEmployeeId());
        log.info("Consumed company-add-employee event with parameters: companyId - {}; employeeId - {}.", event.getCompanyId(), event.getEmployeeId());
    }

    @KafkaListener(topics = "company-remove-employee", groupId = "employee-service")
    public void handleRemove(RemoveEmployeeEvent event) {
        companyService.removeCompanyEmployee(event.getCompanyId(), event.getEmployeeId());
        log.info("Consumed company-remove-employee event with parameters: companyId - {}.", event.getCompanyId());
    }

}

