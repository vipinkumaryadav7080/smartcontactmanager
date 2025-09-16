package com.smart.contact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.contact.entities.Contact;
import com.smart.contact.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
//pegination......
	

	//fetch data curent user login and return all added contact display by user_Id
	@Query("from Contact as c where c.user.id =:userId")
    
	//current-page
	//contact per page
	
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);	
	
	//only single user data fetch by user
	Contact findFirstByUser(User user);
	
	//only single user data search by user
	List<Contact> findByNameContainingAndUser(String name, User user);

		
	
	
	
}
