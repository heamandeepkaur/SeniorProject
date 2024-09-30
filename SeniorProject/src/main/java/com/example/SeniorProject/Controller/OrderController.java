package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Email.EmailDetails;
import com.example.SeniorProject.Model.*;
import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Service.EmailService;
import com.fasterxml.jackson.databind.*;
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
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/create")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@RequestParam(name = "id") int id, @RequestBody OrderDTO orderDTO) {
        // Check if the order already exists
        if (orderRepository.existsById(orderDTO.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR!!! - Order with this ID already exists.");
        }

        // Find the customer by ID
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - Customer not found");
        }

        // Create the new order
        Order order = new Order(orderDTO.getDate(), orderDTO.getRentalTime(), orderDTO.isPaid(), orderDTO.getStatus());
        order.setId(generateUniqueOrderId()); // Use unique ID generation
        // Save the order to the database
        order = orderRepository.save(order);

        double totalPrice = 0;

        // Process the products in the order
        for (OrderProductDTO orderProductDTO : orderDTO.getOrderProducts()) {
            ProductDTO productDTO = orderProductDTO.getProduct();
            Product product = productRepository.findById(productDTO.getId()).orElse(null);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR!!! - Product not found");
            }

            int quantity = orderProductDTO.getQuantity();
            if (quantity > product.getQuantity()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR!!! - Insufficient product quantity.");
            }

            // Update product quantity and calculate total price
            totalPrice += product.getPrice() * quantity;
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // Save the OrderProduct to link the order and product
            OrderProduct orderProduct = new OrderProduct(order, product, quantity);
            orderProductRepository.save(orderProduct);
        }

        // Set the total price for the order and save it again
        order.setPrice(totalPrice);
        orderRepository.save(order);

        // Send notification to admin for new order
        sendAdminNotification(
                "New Order Placed",
                "A new order has been placed by " + customer.getFirstName() + " " + customer.getLastName(),
                order
        );

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

            // Send notification to admin for order cancellation
            sendAdminNotification(
                    "Order Cancelled",
                    "Order ID " + order.getId() + " has been cancelled by " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                    order
            );

            return new ResponseEntity<>("Order cancelled successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error cancelling order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetching the current orders for a customer (status = 'active')
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

    // Return an order
    @PostMapping(path="/return")
    public @ResponseBody String returnOrder(@RequestBody String orderJson) {
        try {
            Order order = objectMapper.readValue(orderJson, Order.class);

            // Update the order status to "Returned"
            order.setStatus("Returned");

            // Save the updated order
            orderRepository.save(order);

            // Send notification to both admin and customer for successful return
            sendAdminNotification("Order Returned",
                    "Order ID " + order.getId() + " has been successfully returned by "
                            + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                    order);

            sendCustomerReturnNotification(order);

            return "Order returned successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error returning order";
        }
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

    // Helper method to send email notifications to the admin
    private void sendAdminNotification(String subject, String messageBody, Order order) {
        EmailDetails adminEmailDetails = new EmailDetails();
        adminEmailDetails.setRecipient("190project2024@gmail.com"); //email of admin
        adminEmailDetails.setSubject(subject);

        String emailBody = messageBody +
                "\nOrder ID: " + order.getId() +
                "\nCustomer: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() +
                "\nTotal Amount: $" + order.getPrice();

        adminEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(adminEmailDetails);
    }

    // Helper method to send email notifications to the customer when the order is returned
    private void sendCustomerReturnNotification(Order order) {
        EmailDetails customerEmailDetails = new EmailDetails();
        customerEmailDetails.setRecipient(order.getCustomer().getEmail());
        customerEmailDetails.setSubject("Order Returned Successfully");

        String emailBody = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + "," +
                "\n\nYour order with ID: " + order.getId() + " has been successfully returned." +
                "\n\nThank you for shopping with us!";

        customerEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(customerEmailDetails);
    }

    // Method to generate a unique order ID
    private int generateUniqueOrderId() {
        Random random = new Random();
        int orderId;
        boolean exists;
        // Keep generating a random number until you find a unique one
        do {
            orderId = random.nextInt(999999);
            exists = orderRepository.existsById(orderId);
        } while (exists);

        return orderId;
    }
}
