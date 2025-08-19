package company.web.controllers.exceptions;

import java.util.UUID;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(UUID id) {
        super("Company with id " + id + " not found");
    }
}
