package com.jobportal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.service.CompanyService;
import com.jobportal.dto.CompanyDTO;
import com.jobportal.exception.JobPortalException;

@RestController
@CrossOrigin
@RequestMapping("/companies")
@Validated
public class CompanyAPI {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/get/{id}")
    public ResponseEntity<CompanyDTO> getCompanyProfile(@PathVariable Long id) throws JobPortalException {
        return new ResponseEntity<>(companyService.getCompanyProfile(id), HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<CompanyDTO>> getAllCompanyProfiles() throws JobPortalException {
        return new ResponseEntity<>(companyService.getAllCompanyProfiles(), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<CompanyDTO> updateCompanyProfile(@RequestBody CompanyDTO companyDTO)
            throws JobPortalException {
        return new ResponseEntity<>(companyService.updateCompanyProfile(companyDTO), HttpStatus.OK);
    }
}
