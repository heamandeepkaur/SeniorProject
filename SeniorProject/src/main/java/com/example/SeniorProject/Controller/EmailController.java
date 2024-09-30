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

import javax.validation.constraints.Email;
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






    // User/CX Email notifications
    private void sendCxPickupNotification ( Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject("Wedding Rental Pickup Reminder");

        //filling the email body
        String emailBody = "Thank you for coming to us for your rental needs!\n"
                + "Here is an important reminder for your rental pickup.\n"
                + order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getID() + " pickup is on, " + order.getDate();

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(CxEmailDetails);

    }//Pickup

    private void sendCxReturnNotification ( Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject("Wedding Rental Return Reminder");

        //filling the email body
        String emailBody = "I hope your special day was memory worthy!\n"
                + "Below is important return information for your rental\n"
                + order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getID() + " return is on, " + order.getRentalTime();

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(CxEmailDetails);

    }//Return

    //Note that sendCxCanceledNotification requires a CanceledReason to be
    //passed into the function for the email to be complete.
    private void sendCxCanceledNotification ( String CanceledReason, Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject(" Important Wedding Rental Order Update");

        //filling the email body
        String emailBody = order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getID() + " has been canceled for, " + CanceledReason
                + "\n Please reach out to our service number for any questions.";

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(CxEmailDetails);

    }//Canceled

    private String generateVerificationToken(String email) {
        // Generating a random verification token logic goes here
        return passwordEncoder.encode(email);
    }

}
