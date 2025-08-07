package employee.service.kafka;

import employee.service.employee.EmployeeService;
import employee.service.messages.company.ChangeCompanyEvent;
import employee.service.messages.company.ClearCompanyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {
    private final EmployeeService employeeService;

    @Autowired
    public KafkaConsumerService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @KafkaListener(topics = "employee-change-company", groupId = "employee-service")
    public void handleChange(ChangeCompanyEvent event) {
        employeeService.changeCompany(event.getEmployeeId(), event.getCompanyId());
        log.info("Consumed employee-change-company event with parameters: employeeId - {}; companyId - {}.", event.getEmployeeId(), event.getCompanyId());
    }

    @KafkaListener(topics = "employee-clear-company", groupId = "employee-service")
    public void handleClear(ClearCompanyEvent event) {
        employeeService.clearCompany(event.getEmployeeId());
        log.info("Consumed employee-clear-company event with parameters: employeeId - {}.", event.getEmployeeId());
    }

}
