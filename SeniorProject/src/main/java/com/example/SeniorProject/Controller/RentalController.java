package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.RentalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "/rental")
public class RentalController { 
    @Autowired
    private RentalRepository rentalRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // New search method that includes both type and regular search
    @GetMapping(path = "/getAll")
    public @ResponseBody Page<Product> getAllProducts(
            @RequestParam(value = "kw", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            Pageable pageable) {
        if (keyword != null && !keyword.isEmpty() && type != null && !type.isEmpty()) {
            // Apply both keyword and type filters
            return rentalRepository.findByNameContainingIgnoreCaseAndTypeIgnoreCase(keyword, type, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            // Apply keyword filter only
            return rentalRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (type != null && !type.isEmpty()) {
            // Apply type filter only
            return rentalRepository.findByTypeIgnoreCase(type, pageable);
        }
        return rentalRepository.findAll(pageable);
    }

    // New search method
    @GetMapping(path = "/search")
    public @ResponseBody Page<Product> searchProducts(
            @RequestParam(name = "kw", required = false) String keyword,
            @RequestParam(name = "type", required = false) String type,
            Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return rentalRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (type != null && !type.isEmpty()) {
            return rentalRepository.findByTypeIgnoreCase(type, pageable);
        } else {
            return rentalRepository.findAll(pageable);
        }
    }
}