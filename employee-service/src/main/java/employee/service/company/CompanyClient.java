package employee.service.company;

import employee.service.configurations.DiscoveryConfiguration;
import employee.web.dto.response.CompanyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CompanyClient {

    private final String COMPANY_SERVICE;
    private final WebClient webClient;

    @Autowired
    public CompanyClient(DiscoveryConfiguration discoveryConfiguration, WebClient.Builder builder) {
        COMPANY_SERVICE = discoveryConfiguration.getCompanyService();
        this.webClient = builder.build();
    }

    public CompanyResponse getCompany(UUID companyId) {
        return webClient.get()
                .uri(COMPANY_SERVICE + "companies/" + companyId)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(
                                errorBody -> Mono.error(new RuntimeException("EmployeeService: " + errorBody))
                        )
                )
                .bodyToMono(CompanyResponse.class)
                .block();
    }

}
