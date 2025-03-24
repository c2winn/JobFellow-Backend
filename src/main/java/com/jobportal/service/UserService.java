package com.jobportal.service;

import com.jobportal.dto.ChangePasswordDTO;
import com.jobportal.dto.LoginDTO;
import com.jobportal.dto.ResponseDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.exception.JobPortalException;

public interface UserService {

	public UserDTO registerUser(UserDTO userDTO) throws JobPortalException;

	public UserDTO getUserByEmail(String email) throws JobPortalException;

	public UserDTO loginUser(LoginDTO loginDTO) throws JobPortalException;

	public Boolean sendOTP(String email) throws Exception;

	public Boolean verifyOtp(String email, String otp) throws JobPortalException;

	public ResponseDTO changePassword(LoginDTO loginDTO) throws JobPortalException;

	public ResponseDTO changeUsername(UserDTO userDTO, String currentPassword) throws JobPortalException;

	public ResponseDTO changePassword(ChangePasswordDTO changePasswordDTO) throws JobPortalException;

}
