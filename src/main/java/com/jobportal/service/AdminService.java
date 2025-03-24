package com.jobportal.service;

import java.util.List;

import com.jobportal.dto.UserDTO;
import com.jobportal.exception.JobPortalException;

public interface AdminService {
    public List<UserDTO> getAllUsers() throws JobPortalException;

    public boolean deleteUser(Long id) throws JobPortalException;

    public UserDTO getProfile(Long id) throws JobPortalException;

    public boolean verifyUser(Long id) throws JobPortalException;

    public boolean unverifyUser(Long id) throws JobPortalException;
}
