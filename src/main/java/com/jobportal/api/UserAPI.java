package com.jobportal.api;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
 
import com.jobportal.dto.ChangePasswordDTO;
import com.jobportal.dto.LoginDTO;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.dto.ResponseDTO;
import com.jobportal.dto.UpdateUsernameDTO;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.User;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.EmailService;
import com.jobportal.service.NotificationService;
import com.jobportal.service.UserService;
 
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
 
@RestController
@CrossOrigin
@RequestMapping("/users")
@Validated
public class UserAPI {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
 
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid UserDTO userDTO) throws JobPortalException {
        return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.CREATED);
    }
 
    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@RequestBody @Valid LoginDTO loginDTO) throws JobPortalException {
        return new ResponseEntity<>(userService.loginUser(loginDTO), HttpStatus.OK);
    }
 
    @PostMapping("/changePass")
    public ResponseEntity<ResponseDTO> changePassword(@RequestBody @Valid LoginDTO loginDTO) throws JobPortalException {
        return new ResponseEntity<>(userService.changePassword(loginDTO), HttpStatus.OK);
    }
 
    @PutMapping("/updateUsername/{email}")
    public ResponseEntity<ResponseDTO> updateUsername(@PathVariable String email,
            @Valid @RequestBody UpdateUsernameDTO updateUsernameDTO)
            throws JobPortalException {
        // Create ProfileDTO with new username and email
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setName(updateUsernameDTO.getNewUsername());
 
        // Call the service method to update username
        ResponseDTO response = userService.changeUsername(userDTO, updateUsernameDTO.getCurrentPassword());
 
        return ResponseEntity.ok(response);
    }
 
    @PostMapping("/sendOtp/{email}")
    public ResponseEntity<ResponseDTO> sendOtp(@PathVariable @Email(message = "{user.email.invalid}") String email)
            throws Exception {
        userService.sendOTP(email);
        ResponseDTO response = new ResponseDTO("OTP sent successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
 
    @GetMapping("/verifyOtp/{email}/{otp}")
    public ResponseEntity<ResponseDTO> verifyOtp(
            @PathVariable @NotBlank(message = "{user.email.absent}") @Email(message = "{user.email.invalid}") String email,
            @PathVariable @Pattern(regexp = "^[0-9]{6}$", message = "{otp.invalid}") String otp)
            throws JobPortalException {
        userService.verifyOtp(email, otp);
        return new ResponseEntity<>(new ResponseDTO("OTP has been verified."), HttpStatus.ACCEPTED);
    }
 
    @PutMapping("/change-password/{email}")
    public ResponseEntity<ResponseDTO> changePassword(@PathVariable String email,
            @RequestBody ChangePasswordDTO changePasswordDTO)
            throws JobPortalException {
        changePasswordDTO.setEmail(email);
 
        // Log to check if 'oldPassword' and 'email' are correctly received
        System.out.println("Old Password: " + changePasswordDTO.getOldPassword());
        System.out.println("Looking for user with email: " + changePasswordDTO.getEmail());
 
        // Proceed with the rest of the logic
        User user = userRepository.findByEmail(changePasswordDTO.getEmail())
                .orElseThrow(() -> new JobPortalException("USER_NOT_FOUND"));
 
        // Validate the old password
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new JobPortalException("Incorrect current password");
        }
 
        // Check if the new password is different from the old one
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new JobPortalException("NEW_PASSWORD_CANNOT_BE_SAME_AS_OLD_PASSWORD");
        }
 
        // Encode and save the new password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
 
        // Send a notification
        NotificationDTO noti = new NotificationDTO();
        noti.setUserId(user.getId());
        noti.setMessage("Your password has been updated successfully.");
        noti.setAction("Password Change");
        notificationService.sendNotification(noti);
 
        // Return a response indicating success
        return ResponseEntity.ok(new ResponseDTO("Password changed successfully."));
    }
 
    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            @RequestParam("certificate") MultipartFile certificate) {
        try {
            emailService.sendVerificationEmail(name, address, phone, certificate);
            return ResponseEntity.ok("Email sent successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }

    @PostMapping("/send-invitation-email")
    public ResponseEntity<String> sendInvitationEmail(
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("location") String location,
            @RequestParam("companyEmail") String companyEmail,
            @RequestParam("jobDetail") String jobDetail,
            @RequestParam("receiverEmail") String receiverEmail) {
        try {
            emailService.sendMessage(jobTitle, location, companyEmail, jobDetail,receiverEmail);
            return ResponseEntity.ok("Email sent successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }
 
}