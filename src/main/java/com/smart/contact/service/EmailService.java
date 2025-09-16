package com.smart.contact.service;

import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendEmail(String subject, String message, String to) {
        boolean isSent = false;
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("vipinkumaryadavstp1007@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true);
            

            mailSender.send(mimeMessage);
            
            System.out.println("✅ Email sent successfully to " + to);
            isSent = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSent;
    }
}
