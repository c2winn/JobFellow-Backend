package com.jobportal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.UserDTO;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.AdminService;

@RestController
@Validated
@CrossOrigin
@RequestMapping("/admin")
public class AdminAPI {
    @Autowired
    private AdminService adminService;

    @GetMapping("/getUsers/{id}")
    public ResponseEntity<UserDTO> getProfile(@PathVariable Long id) throws JobPortalException {
        return new ResponseEntity<>(adminService.getProfile(id), HttpStatus.OK);
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<UserDTO>> getAllUsers() throws JobPortalException {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }

    @DeleteMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable Long id) throws JobPortalException {
        boolean isDeleted = adminService.deleteUser(id);

        if (isDeleted) {
            return "User deleted successfully.";
        } else {
            return "User not found.";
        }
    }

    @PutMapping("/verify/{id}")
    public String verifyUser(@PathVariable Long id) throws JobPortalException {
        boolean verified = adminService.verifyUser(id);

        if (verified) {
            return "User verified successfully.";
        } else {
            return "User not found or already verified.";
        }
    }

    @PutMapping("/unverify/{id}")
    public String unverifyUser(@PathVariable Long id) throws JobPortalException {
        boolean unverified = adminService.unverifyUser(id);

        if (unverified) {
            return "User unverified successfully.";
        } else {
            return "User not found or already unverified.";
        }
    }
}
