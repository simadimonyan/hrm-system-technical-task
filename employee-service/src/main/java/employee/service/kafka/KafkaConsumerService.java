package employee.service.kafka;

import employee.service.employee.implementations.EmployeeServiceImpl;
import employee.service.messages.company.ChangeCompanyEvent;
import employee.service.messages.company.ClearCompanyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {
    private final EmployeeServiceImpl employeeServiceImpl;

    @Autowired
    public KafkaConsumerService(EmployeeServiceImpl employeeServiceImpl) {
        this.employeeServiceImpl = employeeServiceImpl;
    }

    @KafkaListener(topics = "employee-change-company", groupId = "employee-service")
    public void handleChange(ChangeCompanyEvent event) {
        employeeServiceImpl.changeCompany(event.getEmployeeId(), event.getCompanyId());
        log.info("Consumed employee-change-company event with parameters: employeeId - {}; companyId - {}.", event.getEmployeeId(), event.getCompanyId());
    }

    @KafkaListener(topics = "employee-clear-company", groupId = "employee-service")
    public void handleClear(ClearCompanyEvent event) {
        employeeServiceImpl.clearCompany(event.getEmployeeId());
        log.info("Consumed employee-clear-company event with parameters: employeeId - {}.", event.getEmployeeId());
    }

}
