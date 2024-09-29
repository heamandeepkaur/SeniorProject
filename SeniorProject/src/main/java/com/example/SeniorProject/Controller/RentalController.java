package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.RentalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/rental")
public class RentalController { 
        @Autowired
        private RentalRepository rentalRepository;
        private final ObjectMapper objectMapper = new ObjectMapper();

        
        @GetMapping(path="/getAll")
        public @ResponseBody Page<Product> getAllProducts(Pageable pageable) {
            return rentalRepository.findAll(pageable);
        }
        
}