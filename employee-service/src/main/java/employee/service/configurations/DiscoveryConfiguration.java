package employee.service.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class DiscoveryConfiguration {

    @Value("${discovery.service.company-service-uri}")
    private String companyService;

}
