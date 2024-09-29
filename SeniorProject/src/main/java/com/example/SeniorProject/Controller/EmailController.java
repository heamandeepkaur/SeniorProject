package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.Account;
import com.example.SeniorProject.Model.AccountRepository;
import com.example.SeniorProject.Service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping(path="/email")
public class EmailController
{
    @Autowired
    private EmailService emailService;

    private HashMap<String,String> emailMap = new HashMap<>();

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody EmailDetails details)
    {
        return emailService.sendSimpleEmail(details);
    }

    @PostMapping("/sendEmailWithAttachment")
    public String sendMailWithAttachment(@RequestBody EmailDetails details)
    {
        return emailService.sendEmailWithAttachment(details);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        if (emailMap.containsKey(token)) {
            String email = emailMap.get(token);
            emailMap.remove(token); // Remove the token from the map after verification
            Account account = accountRepository.findAccountByEmail(email);
            if(account != null)
            {
                account.setVerified(true);
                accountRepository.save(account);
            }
            else
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account not found");
            }
            return ResponseEntity.ok("Email verified successfully for: " + email);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token. Please try again.");
        }
    }

    @PostMapping("/sendVerificationEmail")
    public String sendVerificationEmail(@RequestParam("email") String email, HttpServletRequest request) {
        // Generating a verification token
        String token = generateVerificationToken(email);
        if(!emailMap.containsKey(token)){
            emailMap.put(token,email);
        } else if(emailMap.containsValue(email) && (!emailMap.get(token).equals(email))){
            emailMap.remove(token);
            emailMap.put(token,email);
        }

        String verificationUrl = "http://" + request.getServerName() + ":" + request.getServerPort()  + "/email/verify-email?token=" + token;

        // Creating the email details
        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Email Verification");
        details.setMessageBody("Please click the link to verify your email: " + verificationUrl);

        // Sending the email
        return emailService.sendSimpleEmail(details);
    }

    private String generateVerificationToken(String email) {
        // Generating a random verification token logic goes here
        return passwordEncoder.encode(email);
    }

}
