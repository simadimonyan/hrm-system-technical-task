package company.web.controllers;

import company.repository.entities.CompanyEntity;
import company.service.company.contracts.CompanyService;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.contracts.Company;
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
    private CompanyEntity createCompany(@RequestBody CompanyRequest request) throws Exception {
        return companyService.createCompany(request);
    }

    @GetMapping("/{id}")
    private Company getCompany(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean extraInfo) {
        return companyService.readCompany(id, extraInfo);
    }

    @PutMapping("/{id}")
    private CompanyEntity updateCompany(@PathVariable UUID id, @RequestBody CompanyRequest request) {
        return companyService.updateCompany(id, request);
    }

    @DeleteMapping("/{id}")
    private CompanyEntity deleteCompany(@PathVariable UUID id) {
        return companyService.deleteCompany(id);
    }

    @GetMapping("/all")
    public Page<? extends Company> getAllCompanies(Pageable pageable, @RequestParam(defaultValue = "false") boolean extraInfo) {
        return companyService.getAllCompanies(pageable, extraInfo);
    }

}
