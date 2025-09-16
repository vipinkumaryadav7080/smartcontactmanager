package com.smart.contact.controler;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.contact.dao.ContactRepository;
import com.smart.contact.dao.MyOrderRepository;
import com.smart.contact.dao.UserRepository;
import com.smart.contact.entities.Contact;
import com.smart.contact.entities.MyOrder;
import com.smart.contact.entities.User;
import com.smart.contact.helper.Message;
import com.razorpay.*;
@Controller
@RequestMapping("/user")
public class UserControler {
	
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
		// method for adding comman data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {

		String userName = principal.getName();

		System.out.println("USERNAME  " + userName);

		User user = userRepository.getUserByUserName(userName);

		System.out.println("USER " + user);

		m.addAttribute("user", user);

	}

	// dashboard home
	@GetMapping("/index")
	public String dashBoard(Model model, Principal principal, MultipartFile multipartFile) {
		model.addAttribute("title", "user Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form in db

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult result, Model model,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {

		try {

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
//processing file 
			if (file.isEmpty()) {
				System.out.println("File is Empty");
				contact.setImage("contact.png");
				model.addAttribute("contact", contact);
				return "normal/add_contact_form";
			} else {
				// unique file name with date-time
				String timestamp = java.time.LocalDateTime.now()
						.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

				String originalFilename = file.getOriginalFilename();
				String ext = "";
				if (originalFilename != null && originalFilename.contains(".")) {
					ext = originalFilename.substring(originalFilename.lastIndexOf("."));
				}

				String uniqueFileName = timestamp + "_" + originalFilename;

				// set file name in entity
				contact.setImage(uniqueFileName);

				// save file with uniqueFileName, not original name
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + uniqueFileName);

				try (InputStream is = file.getInputStream()) {
					Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
				}
				// Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image uploaded with unique name: " + uniqueFileName);
			}

			contact.setUser(user);

			user.getContact().add(contact);

			this.userRepository.save(user);

			System.out.println("DATA " + contact);
			System.out.println("Added to data base");
			// message success-------
			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR " + e.getMessage());
			// error message
			session.setAttribute("message", new Message("Something went Wrong !! Try again..", "danger"));

		}
		return "normal/add_contact_form";
	}

//show contact handler
	// per page = 5[n]
	// current page =0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {

		m.addAttribute("title", "Show user Contacts");
		// send contact list to display all data
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// current-page
		// contact per page-5
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contactsByUser = this.contactRepository.findContactsByUser(user.getId(), pageable);
		m.addAttribute("contacts", contactsByUser);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contactsByUser.getTotalPages());

		return "normal/show_contacts";

	}

	// showing specific/particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("cId" + cId);
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		// sending only login user contacts
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		} else {
			System.out.println("User not permit to access another user contacts....");
		}

		return "normal/contact_detail";
	}

	// open update form handler

	// STEP 1: Show Update Form (GET)
	@GetMapping("/update-contact/{cId}")
	public String showUpdateForm(@PathVariable("cId") Integer cId, Model m, Principal principal) {
		m.addAttribute("title", "Update Contact");

		// fetch contact and validate ownership
		Contact contact = this.contactRepository.findById(cId)
				.orElseThrow(() -> new RuntimeException("Contact not found"));

		User user = this.userRepository.getUserByUserName(principal.getName());
		if (user.getId() != contact.getUser().getId()) {
			throw new RuntimeException("Unauthorized access");
		}

		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// STEP 2: Process Update (POST)
	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			HttpSession session, Principal principal) {
		try {
			// fetch old contact from DB
			Contact oldContact = this.contactRepository.findById(contact.getcId())
					.orElseThrow(() -> new RuntimeException("Contact not found"));

			// handle image
			if (!file.isEmpty()) {
				// delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File oldFile = new File(deleteFile, oldContact.getImage());
				oldFile.delete();

				// generate unique file name
				String timestamp = java.time.LocalDateTime.now()
						.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
				String originalFilename = file.getOriginalFilename();
				String uniqueFileName = timestamp + "_" + originalFilename;

				// save new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + uniqueFileName);
				try (InputStream is = file.getInputStream()) {
					Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
				}

				contact.setImage(uniqueFileName);
			} else {
				// keep old image
				contact.setImage(oldContact.getImage());
			}

			// attach correct user
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			// save updated contact
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact has been updated", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Update failed: " + e.getMessage(), "danger"));
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// DELETE CONTACT HANDLER
	@GetMapping("/delete-contact/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal, HttpSession session) {
		try {
			// 1. Get the logged-in user from session (Spring Security principal)
			String username = principal.getName();
			User user = this.userRepository.getUserByUserName(username);

			// 2. Fetch contact from DB by ID
			Contact contact = this.contactRepository.findById(cId)
					.orElseThrow(() -> new RuntimeException("Contact not found"));

			// 3. Security check: ensure the contact belongs to the logged-in user
			if (!Objects.equals(contact.getUser().getId(), user.getId())) {
				throw new RuntimeException("Unauthorized access attempt!");
			}

			// 4. Delete profile image from filesystem (if not default)
			if (contact.getImage() != null && !contact.getImage().equals("default.png")) {
				File imgDir = new ClassPathResource("static/img").getFile();
				File oldFile = new File(imgDir, contact.getImage());
				if (oldFile.exists()) {
					oldFile.delete();
					System.out.println("Deleted image: " + contact.getImage());
				}
			}

			// 5. Break relation (important in bi-directional mapping)
			user.getContact().remove(contact);

			// 6. Delete contact from DB
			this.contactRepository.delete(contact);

			// 7. Success message
			session.setAttribute("message", new Message("Contact deleted successfully!", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			// Failure message
			session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "danger"));
		}

		// 8. Redirect to contacts page (page 0)
		return "redirect:/user/show-contacts/0";
	}

	// user profile
	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		// Current logged-in user
		User user = userRepository.getUserByUserName(principal.getName());

		// User ka pehla contact nikaal lo

		Contact contact = contactRepository.findFirstByUser(user);

		model.addAttribute("user", user);
		model.addAttribute("contact", contact); // 👈 ek contact bhej rahe hain

		return "normal/profile";
	}

	// open settings handler
	@GetMapping("/settings")
	public String openSettings() {

		return "normal/settings";
	}

	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPssword,
			@RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword,
			Principal principal, Model model, HttpSession httpSession) {
		System.out.println("Old password " + oldPssword);
		System.out.println("New password " + newPassword);
		System.out.println("confirmNewPassword " + confirmPassword);

		// ✅ Get currently logged-in user (by email/username from Principal)
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		// 1. Validate old password
		if (!this.bCryptPasswordEncoder.matches(oldPssword, currentUser.getPassword())) {
			httpSession.setAttribute("message", new Message("Old password is incorrect!", "danger"));
			model.addAttribute("errorMessage", "Old password is incorrect!");
			return "redirect:/user/settings"; // Thymeleaf template name
		}
		
		
		 // 2. Prevent reusing old password
	    if (this.bCryptPasswordEncoder.matches(newPassword, currentUser.getPassword())) {
	    	httpSession.setAttribute("message", new Message("Old and New password cannot be the same. Please choose a new password for security reasons!!", "danger"));
			model.addAttribute("errorMessage", "Old and New password cannot be the same. Please choose a new password for security reasons!!");
	        return "redirect:/user/settings";
	    }
			

		// 3. Check new and confirm password match
		if(!newPassword.equals(confirmPassword)) {
			httpSession.setAttribute("message",
					new Message("New Password and Confirm Password do not match!", "danger"));
			model.addAttribute("errorMessage", "New Password and Confirm Password do not match!");
			return "redirect:/user/settings";
		}

		// 4. Update password
		currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(currentUser);

		// 5. Success message
		httpSession.setAttribute("message", new Message("Password updated successfully!", "success"));
		model.addAttribute("successMessage", "Password updated successfully!");
		return "redirect:/user/index";
	}

	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public Map createOrder(@RequestBody Map<String, Object> data,Principal principal) throws Exception
	{
		System.out.println("order function exe...Amount :"+data);
		
		int amt=Integer.parseInt(data.get("amount").toString());
		//Razer pay api 
		
		RazorpayClient client=	new RazorpayClient("rzp_test_RF5NdX8bMwnpni", "vWE6485lHqtSndkWFdDCLvgp");
		
		
		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", amt*100); // Amount is in currency subunits. 
		orderRequest.put("currency","INR");
		orderRequest.put("receipt", "txn_" + UUID.randomUUID());
		
		//creating new order
		Order order = client.Orders.create(orderRequest);
        System.out.println("My Order:"+order);
	
        //save order in database
        
        MyOrder myOrder = new MyOrder();
        myOrder.setAmount(order.get("amount")+"");
       // myOrder.setMyOrderId(order.get("order_id"));
        myOrder.setOrderId(order.get("id"));
        myOrder.setPaymentId(null);
        myOrder.setStatus("created");
        myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
        myOrder.setReceipt(order.get("receipt"));
        
        MyOrder save = this.myOrderRepository.save(myOrder);
        System.out.println("Payment Descriptions: "+save);
        
        // Map बनाएँ ताकि JSON बनकर return हो
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.get("id"));
        map.put("amount", order.get("amount"));
        map.put("currency", order.get("currency"));
        map.put("status", order.get("status"));

        return map;
	}

	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data)
	{
		MyOrder myOrderId = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrderId.setPaymentId(data.get("payment_id").toString());
		myOrderId.setStatus(data.get("status").toString());
		MyOrder save = this.myOrderRepository.save(myOrderId);
		
		System.out.println("Previous payment data: "+data);
		System.out.println("New save data: "+save);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}
	
