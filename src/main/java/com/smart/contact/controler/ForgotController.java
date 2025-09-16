package com.smart.contact.controler;

import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.contact.dao.UserRepository;
import com.smart.contact.entities.User;
import com.smart.contact.service.EmailService;

@Controller
public class ForgotController {
    @Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EmailService emailService;
	// genrate otp
	//Random random = new Random(1000);
	SecureRandom random = new SecureRandom();
	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession httpSession)

	{
		System.out.println("EMAIL " + email);
		// genrating otp 4 digit

		int otp = random.nextInt(999999);
		System.err.println("OTP " + otp);

		// write code for send otp to email
		String subject = "OTP From SCM";
		// String message="OTP SCM ="+otp+"";
		String message = "" + "<div style='border:1px solid #e2e2e2; padding:20px'>" + "<h1>" + "OTP is:-> " + "<b>"
				+ otp + "</b>" + "</h1>" + "</div>";

		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {
			httpSession.setAttribute("myotp", otp);
			httpSession.setAttribute("email", email);
			return "verify_otp";

		} else {

			httpSession.setAttribute("message", "Check your email id !!");

			return "forgot_email_form";
		}

	}

	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {

		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		if (myOtp == otp) {
			// password change form
			User user = this.userRepository.getUserByUserName(email);
			if (user == null) {
				// send error message when user not exist in db
				session.setAttribute("message", "User does not exits with this email !!");
				return "forgot_email_form";
			} else {
				// send change password form

				return "password_change_form";
			}

		} else {

			session.setAttribute("message", "You have entered wrong OTP !!");
			return "verify_otp";
		}

	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session) 
	{
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);

        return "redirect:/signin?change=password change successfully..";
		
		
		
		
		
	}

}
