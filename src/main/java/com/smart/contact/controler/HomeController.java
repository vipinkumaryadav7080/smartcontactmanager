package com.smart.contact.controler;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.contact.dao.UserRepository;
import com.smart.contact.entities.User;
import com.smart.contact.helper.Message;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/test")
	@ResponseBody
	public String test() {
		User user = new User();
		user.setName("Ram Ram");
		user.setEmail("ram@gmail.com");

		userRepository.save(user);
		return "working";
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home -Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About -Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model, HttpSession session) {
		model.addAttribute("title", "Signup -Smart Contact Manager");
		model.addAttribute("user", new User());
		// remove session mesage here
		Object msg = session.getAttribute("message");
		if (msg != null) {
			model.addAttribute("message", msg);
			session.removeAttribute("message"); // yahan clear hoga
		}
		// en remove mesage here

		return "signup";
	}

	// handler for registering user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) throws Exception {
		if (!agreement) {
			System.out.println("You have not agreed the terms and conditions");
			throw new Exception("You have not agreed the terms and conditions");

		}

		if (result1.hasErrors()) {
			System.out.println("ERROR " + result1.toString());
			model.addAttribute("user", user);
			return "signup";
		}

		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("ramram.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		System.out.println("Agrement " + agreement);
		System.out.println("User " + user);

		try {
			User result = this.userRepository.save(user);

			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully Registered!!", "alert-success"));
			return "signup";

		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong" + e.getMessage(), "alert-danger"));

			return "signup";

		}

	}

	// handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "/login";
	}

	// error handler login-error
	@GetMapping("/login-fail")
	public String loginFailPage() {
		return "login-fail";
	}

}
