package com.jobportal.jwt;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jobportal.dto.UserDTO;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.UserService;

/**
 * MyUserDetailsService is a custom implementation of UserDetailsService.
 * It retrieves user details from the database using the user's email.
 * This service is used by Spring Security for authentication.
 */
@Service
public class MyUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		try {
			// Fetch user details using the email
			UserDTO dto=userService.getUserByEmail(email);

			// Convert UserDTO into CustomUserDetails, which implements UserDetails
			return new CustomUserDetails(dto.getId(), email,dto.getName(), dto.getPassword(),dto.getProfileId(), dto.getAccountType(), new ArrayList<>());
		} catch (JobPortalException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
