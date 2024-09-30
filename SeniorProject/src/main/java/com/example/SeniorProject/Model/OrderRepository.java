package com.example.SeniorProject.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

         // Fetch current (active) orders for a specific customer
        @Query("SELECT o FROM Order o WHERE o.customer.id = ?1 AND (o.status != 'completed' AND o.status != 'cancelled' )")
        List<Order> findCurrentOrdersByCustomerId(int customerId);

        // Fetch past (completed) orders for a specific customer
        @Query("SELECT o FROM Order o WHERE o.customer.id = ?1 AND ( o.status = 'completed' OR o.status = 'cancelled')")
        List<Order> findPastOrdersByCustomerId(int customerId);
        
        // Custom query to fetch an order by its ID
        @Query("SELECT o FROM Order o WHERE o.id = ?1")
        Order getOrderById(int id);

        // Optional: Custom delete method using a query (though JpaRepository has deleteById already)
        @Transactional
        @Modifying
        @Query("DELETE FROM Order o WHERE o.id = ?1")
        void deleteById(int id);

		List<Order> findOrderByCustomerId(int customerId);
}


