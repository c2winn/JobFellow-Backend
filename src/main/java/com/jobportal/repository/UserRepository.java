package com.jobportal.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.jobportal.dto.AccountType;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.User;

public interface UserRepository extends MongoRepository<User, Long> {
	public Optional<User> findByEmail(String email);

	Optional<User> findByProfileIdAndAccountType(Long profileId, AccountType accountType);

	UserDTO getUserByEmail(String email);
	
}
