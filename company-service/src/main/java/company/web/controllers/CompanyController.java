package company.web.controllers;

import company.service.CompanyService;
import company.web.dto.CompanyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    private CompanyResponse createCompany() {
        return new CompanyResponse();
    }

    @GetMapping("/{id}")
    private CompanyResponse getCompany(@PathVariable Long id) {
        return new CompanyResponse();
    }

    @PutMapping("/{id}")
    private CompanyResponse updateCompany(@PathVariable Long id) {
        return new CompanyResponse();
    }

    @DeleteMapping("/{id}")
    private CompanyResponse deleteCompany(@PathVariable Long id) {
        return new CompanyResponse();
    }

    @GetMapping
    private CompanyResponse getCompanies() {
        return new CompanyResponse();
    }

}
