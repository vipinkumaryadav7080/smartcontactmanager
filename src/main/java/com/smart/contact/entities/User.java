package com.smart.contact.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name="USER")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private boolean enabled;
	
	@NotBlank(message = "Name field is required!!")
	@Size(min =2,max=20,message = "Name is bitween 2-20 characterare allowed!!")
	@Pattern(regexp = "^[A-Za-z][a-zA-Z\\s]*$", message = "Name must start with a letter and contain only alphabets & spaces")
	private String name;
	
	@Column(unique = true)
	@NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 40, message = "Email must not exceed 40 characters")
	private String email;
	
	@NotBlank(message = "Password is required")
    @Size(min = 8, max = 250, message = "Password must be between 8 and 20 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain at least one uppercase, one lowercase, one digit and one special character"
    )
	private String password;
	private String role;
	private String imageUrl;
	@Column(length =500 )
	
	@NotBlank(message = "About section cannot be blank")
    @Size(min = 10, max = 250, message = "About must be between 10 and 250 characters")
    private String about;
	@OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true)
	private List<Contact> contact=new ArrayList<>();
	
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getAbout() {
		return about;
	}
	public void setAbout(String about) {
		this.about = about;
	}
	public List<Contact> getContact() {
		return contact;
	}
	public void setContact(List<Contact> contact) {
		this.contact = contact;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", enabled=" + enabled + ", name=" + name + ", email=" + email + ", password="
				+ password + ", role=" + role + ", imageUrl=" + imageUrl + ", about=" + about + ", contact=" + contact
				+ "]";
	}
	
	
	

}
