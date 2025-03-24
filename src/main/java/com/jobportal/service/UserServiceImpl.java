package com.jobportal.service;

// import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jobportal.dto.AccountType;
import com.jobportal.dto.ChangePasswordDTO;
import com.jobportal.dto.LoginDTO;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.dto.ResponseDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.Company;
import com.jobportal.entity.OTP;
import com.jobportal.entity.Profile;
import com.jobportal.entity.User;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.OTPRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.utility.Data;
import com.jobportal.utility.Utilities;

import jakarta.mail.internet.MimeMessage;

@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
    private CompanyRepository companyRepository;

	@Autowired
	private OTPRepository otpRepository;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private CompanyService companyProfileService;

	@Override
	public UserDTO registerUser(UserDTO userDTO) throws JobPortalException {
		Optional<User> optional = userRepository.findByEmail(userDTO.getEmail());
		if (optional.isPresent())
			throw new JobPortalException("USER_FOUND");
		userDTO.setId(Utilities.getNextSequenceId("users"));
		userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

		if (userDTO.getAccountType() == AccountType.EMPLOYER) {
			userDTO.setProfileId(companyProfileService.createCompanyProfile(userDTO));
		} else {
			userDTO.setProfileId(profileService.createProfile(userDTO));
		}

		userDTO.setVerified(false);

		User user = userRepository.save(userDTO.toEntity());
		user.setPassword(null);
		return user.toDTO();
	}

	@Override
	public UserDTO loginUser(LoginDTO loginDTO) throws JobPortalException {
		User user = userRepository.findByEmail(loginDTO.getEmail())
				.orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));
		if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword()))
			throw new JobPortalException("INVALID_CREDENTIALS");
		user.setPassword(null);
		return user.toDTO();
	}

	@Override
	public Boolean sendOTP(String email) throws Exception {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));
		MimeMessage mm = mailSender.createMimeMessage();
		MimeMessageHelper message = new MimeMessageHelper(mm, true);
		message.setTo(email);
		message.setSubject("Your OTP Code");
		String generatedOtp = Utilities.generateOTP();
		OTP otp = new OTP(email, generatedOtp, LocalDateTime.now());
		otpRepository.save(otp);
		message.setText(Data.getMessageBody(generatedOtp, user.getName()), true);
		mailSender.send(mm);
		return true;
	}

	@Override
	public Boolean verifyOtp(String email, String otp) throws JobPortalException {
		OTP otpEntity = otpRepository.findById(email).orElseThrow(() -> new JobPortalException("OTP_NOT_FOUND"));
		if (!otpEntity.getOtpCode().equals(otp))
			throw new JobPortalException("OTP_INCORRECT");
		return true;
	}

	@Scheduled(fixedRate = 60000)
	public void removeExpiredOTPs() {
		LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(5);
		List<OTP> expiredOTPs = otpRepository.findByCreationTimeBefore(expiryTime);
		if (!expiredOTPs.isEmpty()) {
			otpRepository.deleteAll(expiredOTPs);
			System.out.println("Removed " + expiredOTPs.size() + " expired OTPs");
		}
	}

	@Override
	public ResponseDTO changePassword(LoginDTO loginDTO) throws JobPortalException {
		User user = userRepository.findByEmail(loginDTO.getEmail())
				.orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));
		user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
		userRepository.save(user);
		NotificationDTO noti = new NotificationDTO();
		noti.setUserId(user.getId());
		noti.setMessage("Password Reset Successfull");
		noti.setAction("Password Reset");
		notificationService.sendNotification(noti);
		return new ResponseDTO("Password changed successfully.");
	}

	// UPDATED HERE
	@Override
	public ResponseDTO changeUsername(UserDTO userDTO, String currentPassword) throws JobPortalException {
		// Retrieve the UserDTO based on email
		UserDTO userDTOFromDb = userRepository.getUserByEmail(userDTO.getEmail());
		if (userDTOFromDb == null) {
			throw new JobPortalException("USER_NOT_FOUND");
		}

		// Convert to User entity
		User user = userDTOFromDb.toEntity();

		// Compare the current password with the stored hashed password
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new JobPortalException("Incorrect current password");
		}

		// Update the username in the User entity
		user.setName(userDTO.getName());
		userRepository.save(user);

		// Change Company name or Profile Name if applicable
		if (user.getAccountType() == AccountType.EMPLOYER) {
        Company company = companyRepository.findById(user.getProfileId())
                .orElseThrow(() -> new JobPortalException("COMPANY_NOT_FOUND"));
        company.setName(user.getName());
		System.out.println("Cp:"+ company);
        companyRepository.save(company); // ✅ Save company changes
    } else if (user.getAccountType() == AccountType.APPLICANT) {
        Profile profile = profileRepository.findById(user.getProfileId())
                .orElseThrow(() -> new JobPortalException("PROFILE_NOT_FOUND"));
        profile.setName(user.getName());
        profileRepository.save(profile); // ✅ Save profile changes
    }

		// Send a notification
		NotificationDTO noti = new NotificationDTO();
		noti.setUserId(user.getId());
		noti.setMessage("Your username has been updated successfully.");
		noti.setAction("Username Change");
		notificationService.sendNotification(noti);

		return new ResponseDTO("Username changed successfully.");
	}


	// UPDATED HERE
	@Override
	public ResponseDTO changePassword(ChangePasswordDTO changePasswordDTO) throws JobPortalException {
		// Validate that the old and new passwords are not null or empty
		if (changePasswordDTO.getOldPassword() == null || changePasswordDTO.getOldPassword().isEmpty()) {
			throw new JobPortalException("Old password is required");
		}
		if (changePasswordDTO.getNewPassword() == null || changePasswordDTO.getNewPassword().isEmpty()) {
			throw new JobPortalException("New password is required");
		}

		// Retrieve the user entity based on email
		User user = userRepository.findByEmail(changePasswordDTO.getEmail())
				.orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));

		// Compare the current password (plain text) with the stored hashed password
		if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
			throw new JobPortalException("Incorrect current password");
		}

		// Ensure the new password is different from the old one
		if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
			throw new JobPortalException("NEW_PASSWORD_CANNOT_BE_SAME_AS_OLD_PASSWORD");
		}

		// Encode and update the password
		user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
		userRepository.save(user);

		// Send a notification
		NotificationDTO noti = new NotificationDTO();
		noti.setUserId(user.getId());
		noti.setMessage("Your password has been updated successfully.");
		noti.setAction("Password Change");
		notificationService.sendNotification(noti);

		// Return a response indicating the password was changed successfully
		return new ResponseDTO("Password changed successfully.");
	}

	@Override
	public UserDTO getUserByEmail(String email) throws JobPortalException {
		return userRepository.findByEmail(email).orElseThrow(() -> new JobPortalException("USER_NOT_FOUND")).toDTO();
	}

}
