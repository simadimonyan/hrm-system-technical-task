package company.web.controllers.exceptions;

public class CompanyAlreadyRegisteredException extends RuntimeException {
    public CompanyAlreadyRegisteredException(String name) {
        super("Company name: \"" + name + "\" already registered");
    }
}
