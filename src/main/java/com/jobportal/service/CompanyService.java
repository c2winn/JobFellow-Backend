package com.jobportal.service;

import java.util.List;

import com.jobportal.dto.CompanyDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.exception.JobPortalException;

public interface CompanyService {
    public Long createCompanyProfile(UserDTO userDTO) throws JobPortalException;

    public CompanyDTO getCompanyProfile(Long id) throws JobPortalException;

    public CompanyDTO updateCompanyProfile(CompanyDTO companyDTO) throws JobPortalException;

    public List<CompanyDTO> getAllCompanyProfiles() throws JobPortalException;
}
