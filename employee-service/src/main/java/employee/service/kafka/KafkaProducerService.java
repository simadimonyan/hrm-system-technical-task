package employee.service.kafka;

import employee.service.messages.employee.AddEmployeeEvent;
import employee.service.messages.employee.RemoveEmployeeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAddEmployee(AddEmployeeEvent event) {
        kafkaTemplate.send("company-add-employee", event);
    }

    public void sendRemoveEmployee(RemoveEmployeeEvent event) {
        kafkaTemplate.send("company-remove-employee", event);
    }

}
