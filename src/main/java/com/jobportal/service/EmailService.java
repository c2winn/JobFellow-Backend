package com.jobportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.utility.Data;

import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
 
    public void sendVerificationEmail(String name, String address, String phone, MultipartFile certificate)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
 
        helper.setTo("sithuwin1892001@gmail.com");
        helper.setSubject("New Verification Request");
        helper.setText("Username: " + name + "\nEmail Address: " + address + "\nPhone: " + phone);
 
        // Attach the file
        if (certificate != null && !certificate.isEmpty()) {
            helper.addAttachment(certificate.getOriginalFilename(), certificate);
        }
 
        mailSender.send(message);
    }
    public void sendMessage(String jobTitle,String location,String companyEmail,String jobDetail,String receiverEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
 
        helper.setTo(receiverEmail);
        helper.setCc(companyEmail);
        helper.setSubject("Invitation for"+jobTitle);
        helper.setText(Data.getIvitationMessageBody(jobTitle, location, companyEmail, jobDetail),true);
        mailSender.send(message);
    }
}

