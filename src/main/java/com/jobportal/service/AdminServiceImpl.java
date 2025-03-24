package com.jobportal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.dto.AccountType;
import com.jobportal.dto.JobStatus;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.NotificationRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.repository.UserRepository;

@Service("adminService")
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public List<UserDTO> getAllUsers() throws JobPortalException {
        return userRepository.findAll().stream().map((x) -> x.toDTO()).toList();
    }

    @Override
    public boolean deleteUser(Long id) throws JobPortalException {
        // Fetch user details
        User user = userRepository.findById(id)
                .orElseThrow(() -> new JobPortalException("User not found"));

        Long profileId = user.getProfileId();
        AccountType accountType = user.getAccountType();

        // Delete jobs if the user is an employer
        if (accountType == AccountType.EMPLOYER) {
            jobRepository.deleteByPostedBy(profileId);
            companyRepository.deleteById(profileId); // Delete company profile
        } else {
            profileRepository.deleteById(profileId); // Delete general profile (Applicant/Admin)
        }

        // Delete notifications
        notificationRepository.deleteByUserId(id);

        // Finally, delete the user
        userRepository.deleteById(id);

        return true;
    }

    @Override
    public UserDTO getProfile(Long id) throws JobPortalException {
        return userRepository.findById(id).orElseThrow(() -> new JobPortalException("PROFILE_NOT_FOUND")).toDTO();
    }

    @Override
    public boolean verifyUser(Long id) throws JobPortalException {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!user.isVerified()) { // Check if already verified
                user.setVerified(true);
                userRepository.save(user); // Save the updated user
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean unverifyUser(Long id) throws JobPortalException {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.isVerified()) { // Check if already verified
                user.setVerified(false);
                userRepository.save(user); // Save the updated user

                // Close all jobs posted by this user
                List<Job> jobs = jobRepository.findAllByPostedBy(user.getProfileId());
                for (Job job : jobs) {
                    job.setJobStatus(JobStatus.CLOSED);
                }
                jobRepository.saveAll(jobs); // Save updated jobs

                return true;
            }
        }
        return false;
    }

}
