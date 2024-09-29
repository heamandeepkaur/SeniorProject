package com.example.SeniorProject.Model;

import com.example.SeniorProject.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductId>
{
	
}