package com.example.SeniorProject.Model;


import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

import java.util.List;



public interface RentalRepository extends JpaRepository<Product, Integer>
{

    @Query("SELECT p.name FROM Product p")
    List<String> findAllProductNames();

}

