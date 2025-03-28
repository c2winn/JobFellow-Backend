package com.jobportal.jwt;

import lombok.Data;

/**
 * AuthenticationResponse represents a response object that contains a JWT (JSON Web Token).
 * This class is used to send the JWT back to the client after successful authentication.
 */
@Data
public class AuthenticationResponse {

	/**
     * Constructor to initialize the AuthenticationResponse with a JWT.
     * @param jwt The JSON Web Token to be returned in the response.
     */
	public AuthenticationResponse(String jwt) {
		this.jwt=jwt;
	}

	// The JWT token that will be returned to the client upon successful authentication.
	private final String jwt;
}
