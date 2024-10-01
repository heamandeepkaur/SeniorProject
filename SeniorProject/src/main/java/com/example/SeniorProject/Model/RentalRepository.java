package com.example.SeniorProject.Model;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface RentalRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p.name FROM Product p")
    List<String> findAllProductNames();

    // New search method for keyword
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // New search method for type
    Page<Product> findByTypeIgnoreCase(String type, Pageable pageable);

    // New search method
    Page<Product> findByNameContainingIgnoreCaseAndTypeIgnoreCase(String keyword, String type, Pageable pageable);
}