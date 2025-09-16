package com.smart.contact.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.contact.entities.MyOrder;

public interface MyOrderRepository extends JpaRepository<MyOrder, Long> {

public MyOrder findByOrderId(String orderId);
	
}
