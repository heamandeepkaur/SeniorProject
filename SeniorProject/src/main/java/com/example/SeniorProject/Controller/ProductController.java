package com.example.SeniorProject.Controller;

import com.example.SeniorProject.DTOs.ProductDTO;
import com.example.SeniorProject.Model.Product;
import com.example.SeniorProject.Model.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path="/product")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

        @PostMapping(path="/addProduct") // Map ONLY POST Requests
        @PreAuthorize("hasAnyRole('ADMIN')")
        public @ResponseBody String addProduct (@RequestBody ProductDTO productDTO)
        {
                Product product = new Product(productDTO.getQuantity(), productDTO.getPrice(), productDTO.getType(), productDTO.getName(), productDTO.getDescription(), productDTO.getLocation());
                productRepository.save(product);
                return "Product added successfully";
        }

    @GetMapping(path="/getAll")
    public @ResponseBody List<ProductDTO> getAllProducts()
    {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductDTO).toList();
    }

    @PostMapping(path ="/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateProduct (@RequestBody ProductDTO productDTO)
    {
        Product product = productRepository.findById(productDTO.getId()).orElse(null);
        System.out.println(productDTO.getId());
        System.out.println(productDTO.getName());
        System.out.println("happened");
        if(product == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - product not found");
        }
        //public ProductDTO(int id, String name, double price, int quantity, String type, String description, String location)
        if(productDTO.getName() != null)
        {
            product.setName(productDTO.getName());
        }
        if (productDTO.getDescription() != null)
        {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getLocation() != null)
        {
            product.setLocation(productDTO.getLocation());
        }
        if (productDTO.getType() != null)
        {
            product.setType(productDTO.getType());
        }
        if (productDTO.getPrice() != 0)
        {
            product.setPrice(productDTO.getPrice());
        }
        if (productDTO.getQuantity() != 0)
        {
            product.setQuantity(productDTO.getQuantity());
        }
        productRepository.save(product);
        System.out.println(product.getId());
        System.out.println("happened");
        return ResponseEntity.ok("Product updated successfully");

    }


    @DeleteMapping (path ="/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deleteProduct (@RequestParam int id)
    {
        try
        {
            Product product = productRepository.findById(id).orElse(null);
            if (product == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! - Order associated with this id is not found in the database");
            }
            productRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting order");
        }
    }

    @GetMapping(path="/getByName")
    public ResponseEntity<?> getProductByName(@RequestParam String name)
    {
        List<Product> products = productRepository.findAll();
        List<Product> result = new ArrayList<>();
        for (Product product : products)
        {
            if(product.getName().contains(name))
            {
                result.add(product);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.stream().map(this::mapToProductDTO).toList());
    }
    @GetMapping(path="/getById")
    public ResponseEntity<?> getProductByName(@RequestParam int id)
    {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null)
        {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        ProductDTO productDTO = mapToProductDTO(product);
        return ResponseEntity.status(HttpStatus.OK).body(productDTO);
    }

    private ProductDTO mapToProductDTO(Product product)
    {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setType(product.getType());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setDescription(product.getDescription());
        productDTO.setLocation(product.getLocation());
        return productDTO;
    }
}