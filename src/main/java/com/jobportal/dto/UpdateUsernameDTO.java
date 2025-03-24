package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameDTO {

    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @NotBlank(message = "Username cannot be blank")
    private String newUsername;

    // public String getNewUsername() {
    // return newUsername;
    // }

    // public String getCurrentPassword() {
    // return currentPassword;
    // }

    // public void setNewUsername(String newUsername) {
    // this.newUsername = newUsername;
    // }

    // public void setCurrentPassword(String currentPassword) {
    // this.currentPassword = currentPassword;
    // }
}
