package company.web.controllers;

import company.repository.entities.CompanyEntity;
import company.service.company.contracts.CompanyService;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.contracts.Company;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    private CompanyEntity createCompany(@Valid @RequestBody CompanyRequest request) {
        log.info("Received request to create company: {}", request);
        return companyService.createCompany(request);
    }

    @GetMapping("/{id}")
    private Company getCompany(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean extraInfo) {
        log.info("Received request to get company: {}, {}", id, extraInfo);
        return companyService.readCompany(id, extraInfo);
    }

    @PutMapping("/{id}")
    private CompanyEntity updateCompany(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        log.info("Received request to update company: {}, {}", id, request);
        return companyService.updateCompany(id, request);
    }

    @DeleteMapping("/{id}")
    private CompanyEntity deleteCompany(@PathVariable UUID id) {
        log.info("Received request to delete company: {}", id);
        return companyService.deleteCompany(id);
    }

    @GetMapping("/all")
    public Page<? extends Company> getAllCompanies(Pageable pageable, @RequestParam(defaultValue = "false") boolean extraInfo) {
        log.info("Received request to get all companies: {}, {}", pageable, extraInfo);
        return companyService.getAllCompanies(pageable, extraInfo);
    }

}
