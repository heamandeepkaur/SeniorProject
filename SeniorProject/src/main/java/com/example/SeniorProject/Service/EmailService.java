package com.example.SeniorProject.Service;

import com.example.SeniorProject.Email.EmailDetails;
import com.example.SeniorProject.Model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.mail.*;
import org.springframework.mail.javamail.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class EmailService
{
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PdfService pdfService;

    @Value("${spring.mail.username}")
    private String sender;

    public void setSender(String sender) {
        this.sender = sender;
    }

    private HashMap<String,String> emailMap = new HashMap<>();

    public HashMap<String, String> getEmailMap() {
        return emailMap;
    }


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public String sendSimpleEmail(EmailDetails details)
    {
        try
        {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(details.getRecipient());
            message.setSubject(details.getSubject());
            message.setText(details.getMessageBody());
            javaMailSender.send(message);
        }
        catch (MailSendException exception)
        {
            exception.printStackTrace();
            return "Error occur while sending the message!!!";
        }
        catch (MailAuthenticationException exception)
        {
            exception.printStackTrace();
            return "authentication failed!!!";
        }
        catch (MailException exception)
        {
            exception.printStackTrace();
            return"";
        }
        return "Message sent successfully";
    }

    public String sendEmailWithAttachment(EmailDetails details)
    {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try
        {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMessageBody());
            mimeMessageHelper.setSubject(details.getSubject());

            FileSystemResource file = new FileSystemResource(details.getAttachment());
            mimeMessageHelper.addAttachment(file.getFilename(), file);
            javaMailSender.send(mimeMessage);
            return "Message sent successfully";
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public void sendOrderInvoice(EmailDetails details, int orderId)
    {
        try
        {
            // Create a MimeMessage
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setSubject(details.getSubject());
            mimeMessageHelper.setText(details.getMessageBody());

            // Attach the PDF file
            String fileName = "invoice_" + orderId + ".pdf";
            File pdfFile = pdfService.downloadPDFFromCLoudFront(fileName);
            FileSystemResource fileResource = new FileSystemResource(pdfFile);
            mimeMessageHelper.addAttachment(fileResource.getFilename(), fileResource);

            // Send the email
            javaMailSender.send(mimeMessage);

        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }
    }

    public boolean verifyEmail( String token)
    {
        if (emailMap.containsKey(token))
        {
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found");
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public void sendVerificationEmail(String email, HttpServletRequest request)
    {
        // Generating a verification token
        String token = generateVerificationToken(email);
        if(!emailMap.containsKey(token))
        {
            emailMap.put(token,email);
        }
        else if(emailMap.containsValue(email) && (!emailMap.get(token).equals(email)))
        {
            emailMap.remove(token);
            emailMap.put(token,email);
        }

        String verificationUrl = "https://" + request.getServerName() + ":" + request.getServerPort()  + "/email/verify-email?token=" + token;

        // Creating the email details
        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Email Verification");
        details.setMessageBody("Please click the link to verify your email: " + verificationUrl);

        // Sending the email
        sendSimpleEmail(details);
    }

    // User/CX Email notifications
    public void sendCxPickupNotification ( Order order)
    {
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject("Wedding Rental Pickup Reminder");

        //filling the email body
        String emailBody = "Thank you for coming to us for your rental needs!\n"
                + "Here is an important reminder for your rental pickup.\n"
                + order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getId() + " pickup is on, " + order.getPickupDate();

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(CxEmailDetails);
    }//Pickup

    public void sendCxReadyNotification ( Order order) {
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject("Your Wedding Rental Pickup Is Ready");

        //filling the email body
        String emailBody = "Thank you for coming to us for your rental needs!\n"
                + "\n Your Order, " + order.getId() + " is ready for pick up!";
        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(CxEmailDetails);
    }//Ready

        //Note that sendCxCanceledNotification requires a CanceledReason to be
    //passed into the function for the email to be complete.
    public void sendCxCanceledNotification ( String CanceledReason, Order order){
        //setting the up the email
        EmailDetails CxEmailDetails = new EmailDetails();
        CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        CxEmailDetails.setSubject(" Important Wedding Rental Order Update");

        //filling the email body
        String emailBody = order.getCustomer().getFirstName() + " "
                + order.getCustomer().getLastName() + " your Order, "
                + order.getId() + " has been canceled for, " + CanceledReason;

        //sending email
        CxEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(CxEmailDetails);

    }//Canceled

    public void sendOrderConfirmation(Order order)
    {
        EmailDetails CxEmailDetails = new EmailDetails();
        if (order.getCustomer() != null) {
            CxEmailDetails.setRecipient(order.getCustomer().getEmail());
        } else {
            throw new IllegalArgumentException("Order has no associated customer. Cannot send order confirmation.");
        }
        CxEmailDetails.setSubject("Order Confirmation");
        CxEmailDetails.setMessageBody("Attached is your order invoice.");
        sendOrderInvoice(CxEmailDetails, order.getId());
    }

    private String generateVerificationToken(String email)
    {
        // Generating a random verification token logic goes here
        return passwordEncoder.encode(email);
    }

    public void sendAdminNotification(String subject, String messageBody, Order order)
    {
        EmailDetails adminEmailDetails = new EmailDetails();
        adminEmailDetails.setRecipient("lpr.luxpartyrents@gmail.com"); //email of admin
        adminEmailDetails.setSubject(subject);

        String emailBody = messageBody +
                "\nOrder ID: " + order.getId() +
                "\nCustomer: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() +
                "\nTotal Amount: $" + order.getPrice();

        adminEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(adminEmailDetails);
    }

    public void sendCustomerReturnNotification(Order order)
    {
        EmailDetails customerEmailDetails = new EmailDetails();
        customerEmailDetails.setRecipient(order.getCustomer().getEmail());
        customerEmailDetails.setSubject("Order Returned Successfully");

        String emailBody = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + "," +
                "\n\nYour order with ID: " + order.getId() + " has been successfully returned." +
                "\n\nThank you for shopping with us!";

        customerEmailDetails.setMessageBody(emailBody);
        sendSimpleEmail(customerEmailDetails);
    }
}