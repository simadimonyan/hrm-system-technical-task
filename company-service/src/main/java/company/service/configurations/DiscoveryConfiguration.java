package company.service.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class DiscoveryConfiguration {

    @Value("${discovery.service.employee-service-uri}")
    private String employeeService;

}
