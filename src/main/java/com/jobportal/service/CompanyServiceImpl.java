package com.jobportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.dto.CompanyDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.Company;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.utility.Utilities;

@Service("companyService")
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    //Creates a new company profile based on the provided user details.
    @Override
    public Long createCompanyProfile(UserDTO userDTO) throws JobPortalException {
        Company company = new Company();

        // Set the company's ID (using a utility function to get the next sequence ID)
        company.setId(Utilities.getNextSequenceId("companies"));
        company.setEmail(userDTO.getEmail());
        company.setName(userDTO.getName());
        company.setHeadquarters(new ArrayList<>());
        company.setIndustries(new ArrayList<>());
        company.setSpecialities(new ArrayList<>());
        companyRepository.save(company);
        return company.getId();
    }

    //Retrieves the company profile by its ID.
    @Override
    public CompanyDTO getCompanyProfile(Long id) throws JobPortalException {
        return companyRepository.findById(id).orElseThrow(() -> new JobPortalException("COMPANY_NOT_FOUND")).toDTO();
    }

    //Updates an existing company profile.
    @Override
    public CompanyDTO updateCompanyProfile(CompanyDTO companyDTO) throws JobPortalException {
        companyRepository.findById(companyDTO.getId()).orElseThrow(() -> new JobPortalException("COMPANY_NOT_FOUND"));
        
        // Save the updated company entity to the repository
        companyRepository.save(companyDTO.toEntity());
        return companyDTO;
    }

    //Retrieves a list of all company profiles.
    @Override
    public List<CompanyDTO> getAllCompanyProfiles() throws JobPortalException {
        // Retrieve all companies from the repository, convert each entity to a DTO, and return the list
        return companyRepository.findAll().stream().map((x) -> x.toDTO()).toList();
    }

}
