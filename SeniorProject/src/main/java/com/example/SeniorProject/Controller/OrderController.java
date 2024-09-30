package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.DTOs.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path="/order")
public class OrderController
{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository; // Add CustomerRepository to handle Customer persistence

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @PostMapping("/create")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@RequestParam(name = "id") int id, @RequestBody OrderDTO orderDTO)
    {
        if (orderRepository.existsById(orderDTO.getId()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR!!! - Order with this ID already exists.");
        }
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - Customer not found");
        }
        Order order = new Order(orderDTO.getDate(), orderDTO.getRentalTime(), orderDTO.isPaid(), orderDTO.getStatus());
        order.setId(orderDTO.getId());  // Manually assigning the ID
        order.setCustomer(customer);
        order = orderRepository.save(order);
        double totalPrice = 0;
        for (OrderProductDTO orderProductDTO : orderDTO.getOrderProducts())
        {
            ProductDTO productDTO = orderProductDTO.getProduct();
            Product product = productRepository.findById(productDTO.getId()).orElse(null);
            if (product == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - Product not found");
            }
            int quantity = orderProductDTO.getQuantity();
            if (quantity > product.getQuantity())
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR! - Insufficient product quantity.");
            }
            totalPrice += product.getPrice() * quantity;
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            OrderProduct orderProduct = new OrderProduct(order, product, quantity);
            orderProductRepository.save(orderProduct);
        }
        order.setPrice(totalPrice);
        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.OK).body("Order created successfully.");
    }

    // Cancel an order
    @PutMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestParam int orderId) {
        try {

            // Find the order by ID
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return new ResponseEntity<>("Order not found", HttpStatus.NOT_FOUND);
            }

            // Check if the order is already cancelled
            if ("cancelled".equals(order.getStatus())) {
                return new ResponseEntity<>("Order is already cancelled", HttpStatus.BAD_REQUEST);
            }

            // Set the order status to 'cancelled'
            order.setStatus("cancelled");

            // Save the order with the updated status
            orderRepository.save(order);

            return new ResponseEntity<>("Order cancelled successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error cancelling order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch current orders for a customer (status = 'active')
    @GetMapping(path="/currentOrders")
    public ResponseEntity<?> getCurrentOrders(@RequestParam int customerId) {
        List<Order> currentOrders = orderRepository.findCurrentOrdersByCustomerId(customerId);
            if (currentOrders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No current orders found for the customer.");
            }

        List<OrderDTO> currentOrderDTOs = currentOrders.stream()
            .map(this::mapToOrderDTO)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(currentOrderDTOs);
    }

    // Fetch past orders for a customer (status = 'completed')
    @GetMapping(path="/pastOrders")
    public ResponseEntity<?> getPastOrders(@RequestParam int customerId) {
         List<Order> pastOrders = orderRepository.findPastOrdersByCustomerId(customerId);
            if (pastOrders.isEmpty()) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No past orders found for the customer.");
            }

        List<OrderDTO> pastOrderDTOs = pastOrders.stream()
            .map(this::mapToOrderDTO)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(pastOrderDTOs);
    }


    // Read all orders with pagination
    @GetMapping(path="/getAll")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody
    List<OrderDTO> getAllOrders()
    {
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOs = orders.stream().map(this::mapToOrderDTO).collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs).getBody();
    }

    // Read a single order by ID
    @GetMapping(path="/getById")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderById(@RequestParam int id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        OrderDTO orderDTO = mapToOrderDTO(order);
        return ResponseEntity.status(HttpStatus.OK).body(orderDTO);
    }

    @PutMapping(path ="/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateOrder ( @RequestBody OrderDTO orderDTO)
    {
        Order order = orderRepository.findById(orderDTO.getId()).orElse(null);
        if(order == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - product not found");
        }

        if(orderDTO.getDate() != null)
        {
            order.setDate(orderDTO.getDate());
        }
        if (orderDTO.getRentalTime() == 0)
        {
            order.setRentalTime(orderDTO.getRentalTime());
        }
        if (orderDTO.isPaid() != order.isPaid())
        {
            order.setPaid(orderDTO.isPaid());
        }
        if (!orderDTO.getStatus().equals(order.getStatus()))
        {
            order.setStatus(orderDTO.getStatus());
        }
        orderRepository.save(order);
        return ResponseEntity.ok("Order updated successfully");
    }

    // Delete an order
    @DeleteMapping(path="/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteOrder(@RequestParam int id)
    {
        try
        {
            Order order = orderRepository.findById(id).orElse(null);
            if (order == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! - Order associated with this id is not found in the database");
            }
            orderRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Order deleted successfully");
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting order");
        }
    }

    @GetMapping(path="/getOrderProductsByOrderId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderProductsByOrderId(@RequestParam int id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! - Order not found");
        }
        Set<OrderProductDTO> orderProductDTOs = order.getOrderProducts().stream()
                .map(orderProduct -> new OrderProductDTO(orderProduct.getQuantity(), new ProductDTO(orderProduct.getProduct().getId(), orderProduct.getProduct().getName(), orderProduct.getProduct().getPrice())))
                .collect(Collectors.toSet());
        return ResponseEntity.status(HttpStatus.OK).body(orderProductDTOs);
    }

    @GetMapping(path = "/getOrderByCustomerId")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderByCustomerId(@RequestParam int id)
    {
        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }

        List<Order> orders = orderRepository.findOrderByCustomerId(customer.getId());
        if (orders.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No orders associate with this customer found");
        }

        List<OrderDTO> orderDTOs = orders.stream().map(this::mapToOrderDTO).toList();
        return ResponseEntity.status(HttpStatus.OK).body(orderDTOs);
    }

    private OrderDTO mapToOrderDTO(Order order)
    {
        Set<OrderProductDTO> orderProductDTOs = order.getOrderProducts().stream()
                .map(orderProduct -> new OrderProductDTO(orderProduct.getQuantity(), new ProductDTO(orderProduct.getProduct().getId(), orderProduct.getProduct().getName(), orderProduct.getProduct().getPrice())))
                .collect(Collectors.toSet());
        OrderDTO orderDTO = new OrderDTO(order.getDate(), order.getRentalTime(), order.isPaid(), order.getStatus());
        orderDTO.setPrice(order.getPrice());
        orderDTO.setId(order.getId());
        orderDTO.setOrderProducts(orderProductDTOs);
        return orderDTO;
    }
}