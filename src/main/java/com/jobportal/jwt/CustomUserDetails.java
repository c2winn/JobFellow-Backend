package com.jobportal.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jobportal.dto.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {
	
	// Ensures compatibility during serialization
	private static final long serialVersionUID = 1L;
	private Long id;
	private String username;
	private String name;
	private String password;
	private Long profileId;
	private AccountType accountType;

	/**
     * Authorities represent the roles or permissions assigned to the user.
     * These are required by Spring Security for authorization.
     */
	private Collection<?extends GrantedAuthority>authorities;

}
