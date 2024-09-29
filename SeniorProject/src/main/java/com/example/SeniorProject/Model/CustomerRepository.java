package com.example.SeniorProject.Model;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

public interface CustomerRepository extends JpaRepository<Customer, Integer>
{
    @Query(" SELECT c FROM Customer c WHERE c.email=?1")
    Customer findCustomersByEmail(String email);

    @Transactional
    @Modifying
    @Query(" DELETE FROM Customer c WHERE c.id=?1")
    void deleteById(int id);

    @Query(" SELECT c FROM Customer c WHERE c.id=?1")
    Customer getCustomerById(int id);
    

    @Query("SELECT c.account FROM Customer c WHERE c.firstName = ?1 AND c.lastName = ?2")
    Account findAccountByCustomerName(String firstName, String lastName);
}
