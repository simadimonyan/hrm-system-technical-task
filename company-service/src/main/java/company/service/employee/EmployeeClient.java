package company.service.employee;

import company.service.configurations.DiscoveryConfiguration;
import company.web.dto.response.EmployeeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class EmployeeClient {

    private final String EMPLOYEE_SERVICE;
    private final WebClient webClient;

    @Autowired
    public EmployeeClient(DiscoveryConfiguration discoveryConfiguration, WebClient.Builder builder) {
        this.EMPLOYEE_SERVICE = discoveryConfiguration.getEmployeeService();
        this.webClient = builder.build();
    }

    public EmployeeResponse getEmployee(UUID id) {
        return webClient.get()
                .uri(EMPLOYEE_SERVICE + "employees/" + id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                                errorBody -> Mono.error(new RuntimeException("CompanyService: " + errorBody))
                        )
                )
                .bodyToMono(EmployeeResponse.class)
                .block();
    }

}
