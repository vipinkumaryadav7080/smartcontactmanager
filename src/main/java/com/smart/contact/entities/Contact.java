package com.smart.contact.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="CONTACT")
public class Contact {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cId;
	
	@NotBlank(message = "Name field is required!!")
	@Size(min =2,max=20,message = "Name is bitween 2-20 characterare allowed!!")
	@Pattern(regexp = "^[A-Za-z][a-zA-Z\\s]*$", message = "Name must start with a letter and contain only alphabets & spaces")
    private String name;
	
	@NotBlank(message = "Name field is required!!")
	@Size(min =2,max=20,message = "Name is bitween 2-20 characterare allowed!!")
	@Pattern(regexp = "^[A-Za-z][a-zA-Z\\s]*$", message = "Name must start with a letter and contain only alphabets & spaces")
    private String secondName;
	
	@NotBlank(message = "Work field cannot be empty")
    @Size(min = 2, max = 50, message = "Work must be between 2 and 50 characters")
	private String work;
	
	@NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
	private String email;
	
	@NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
	private String phone;
	
	
	private String image;
	
	@Size(max = 500, message = "Description cannot be more than 500 characters")
    @Column(length = 500)
	private String description;
	
	@ManyToOne
	@JsonIgnore
	private User user;
	
	public Contact() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getcId() {
		return cId;
	}
	public void setcId(int cId) {
		this.cId = cId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getWork() {
		return work;
	}
	public void setWork(String work) {
		this.work = work;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
//	@Override
//	public String toString() {
//		return "Contact [cId=" + cId + ", name=" + name + ", secondName=" + secondName + ", work=" + work + ", email="
//				+ email + ", phone=" + phone + ", image=" + image + ", description=" + description + ", user=" + user
//				+ "]";
//	}
	
	

}
