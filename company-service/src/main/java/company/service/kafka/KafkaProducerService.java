package company.service.kafka;

import company.service.messages.company.ChangeCompanyEvent;
import company.service.messages.company.ClearCompanyEvent;
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

    public void sendChangeCompany(ChangeCompanyEvent event) {
        kafkaTemplate.send("employee-change-company", event);
    }

    public void sendClearCompany(ClearCompanyEvent event) {
        kafkaTemplate.send("employee-clear-company", event);
    }

}
