package company.web.controllers;

import company.service.CompanyService;
import company.web.dto.request.CompanyRequest;
import company.web.dto.response.CompanyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    private ResponseEntity<String> createCompany(@RequestBody CompanyRequest request) {
        companyService.createCompany(request);
        return ResponseEntity.ok("Successfully created!");
    }

    @GetMapping("/{id}")
    private CompanyResponse getCompany(@PathVariable Long id) {
        var result = companyService.readCompany(id);
        return new CompanyResponse(
            result.getId(),
            result.getName(),
            result.getBudget(),
            result.getEmployeeIds()
        );
    }

    @PutMapping("/{id}")
    private ResponseEntity<String> updateCompany(@PathVariable Long id, @RequestBody CompanyRequest request) {
        companyService.updateCompany(id, request);
        return ResponseEntity.ok("Successfully updated!");
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<String> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok("Successfully deleted!");
    }

}
