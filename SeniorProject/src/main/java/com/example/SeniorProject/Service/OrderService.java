package com.example.SeniorProject.Service;

import com.example.SeniorProject.DTOs.*;
import com.example.SeniorProject.Email.*;
import com.example.SeniorProject.Model.*;
import jakarta.transaction.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.server.*;

import java.util.*;
import java.util.stream.*;

@Service
public class OrderService
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

    @Autowired
    private PdfService pdfService;

    @Autowired
    private googleMapService googleMapService;
    @Autowired
    private PaymentService paymentService;

    @Transactional
    public OrderDTO createOrder(int id, OrderDTO orderDTO)
    {
        // Check if the order already exists
        int orderId = generateUniqueOrderId();
        boolean hasDeliveryOnlyProduct = false;

        // Find the customer by ID
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Customer not found");
        }

        // Create the new order
        Order order = new Order(orderId, orderDTO.getRentalTime(), orderDTO.isPaid(), orderDTO.getAddress());
        order.setCustomer(customer);
        // Save the order to the database
        order = orderRepository.save(order);

        //initialize totalPrice
        double price = 0;
        double deposit = 0;
        double tax = 0;
        double deliveryFee = 0;
        double subtotal = 0;

        // Process the products in the order
        for (OrderProductDTO orderProductDTO : orderDTO.getOrderProducts())
        {
            ProductDTO productDTO = orderProductDTO.getProduct();
            Product product = productRepository.findById(productDTO.getId()).orElse(null);
            if (product == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Product not found");
            }

            if(product.isDeliverOnly())
            {
                hasDeliveryOnlyProduct = true;
            }
            int quantity = orderProductDTO.getQuantity();
            if (quantity > product.getQuantity())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Insufficient product quantity.");
            }

            price += product.getPrice() * quantity;
            deposit += product.getDeposit() * quantity;
            tax += price * 0.0725;

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // Save the OrderProduct to link the order and product
            OrderProduct orderProduct = new OrderProduct(order, product, quantity);
            order.getOrderProducts().add(orderProduct);
        }

        if (hasDeliveryOnlyProduct)
        {
            String placeId = googleMapService.getPlaceId(order.getAddress());
            deliveryFee = googleMapService.calculateDeliveryFee(placeId);
        }

        subtotal = price + deposit + tax + deliveryFee;
        order.setPrice(Math.round(price * 100.0) / 100.0);
        order.setTax(Math.round(tax * 100.0) / 100.0);
        order.setDeliveryFee(deliveryFee);
        order.setDeposit(Math.round(deposit * 100.0) / 100.0);
        order.setSubtotal(Math.round(subtotal * 100.0) / 100.0);
        orderRepository.save(order);
        orderProductRepository.saveAll(order.getOrderProducts());

        generateOrderInvoice(orderId);

        // Send notification to admin for new order
        sendAdminNotification(
                "New Order Placed",
                "A new order has been placed by " + customer.getFirstName() + " " + customer.getLastName(),
                order
        );

        //Send notification to Customer about creation of new order, and pick up
        emailService.sendCxPickupNotification(order);

        return mapToOrderDTO(order);
    }

    public void orderCancelledByCustomer(int orderId)
    {
        cancelOrder(orderId);

        try
        {
            paymentService.refundAllExceptDeposit(orderId);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR!!! - something went wrong when processing return payment");
        }
    }

    public void orderCancelledByAdmin(int orderId)
    {
        cancelOrder(orderId);
        try
        {
            paymentService.refundAll(orderId);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR!!! - something went wrong when processing return payment");
        }

    }

    // Cancel an order
    public void cancelOrder(int orderId)
    {
        // Find the order by ID
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Order not found");
        }

        // Check if the order is already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Order is already cancelled");
        }

        // Set the order status to 'cancelled'
        order.setStatus(OrderStatus.CANCELLED);

        // Save the order with the updated status
        orderRepository.save(order);

        putProductBackToInventory(order);

        // Send notification to admin for order cancellation
        sendAdminNotification(
                "Order Cancelled",
                "Order ID " + order.getId() + " has been cancelled by " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order
        );

        //Send notification to customer about order cancellation
        emailService.sendCxCanceledNotification(
                "Order canceled by Customer", order);
    }

    // Fetching the current orders for a customer (status = 'active')
    public List<OrderDTO> getCurrentOrders(int customerId)
    {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Customer not found");
        }
        List<Order> currentOrders = orderRepository.findCurrentOrdersByCustomerId(customerId);
        if (currentOrders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No current orders found for the customer.");
        }

        return currentOrders.stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    // Fetch past orders for a customer (status = 'completed')
    public List<OrderDTO> getPastOrders(int customerId)
    {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Customer not found");
        }
        List<Order> pastOrders = orderRepository.findPastOrdersByCustomerId(customerId);
        if (pastOrders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No past orders found for the customer.");
        }

        return pastOrders.stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders()
    {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found.");
        }
        return orders.stream().map(this::mapToOrderDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderById(int id)
    {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return mapToOrderDTO(order);
    }

    public void updateOrderStatus(int orderId, OrderDTO orderDTO)
    {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Order not found");
        }

        if (orderDTO.getStatus() != null)
        {
            if (!orderDTO.getStatus().equals(order.getStatus().toString()))
            {
                try
                {
                    OrderStatus.fromString(orderDTO.getStatus());
                }
                catch (IllegalArgumentException exception)
                {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Invalid order status");
                }
                if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED || order.getStatus() == OrderStatus.REFUNDED || order.getStatus() == OrderStatus.COMPLETED)
                {
                    String message = "";
                    if (order.getStatus() == OrderStatus.CANCELLED)
                    {
                        message = "Order has already been cancelled";
                    }
                    else if (order.getStatus() == OrderStatus.RETURNED)
                    {
                        message = "Order has already been returned";
                    }
                    else if (order.getStatus() == OrderStatus.REFUNDED)
                    {
                        message = "Order has already been refunded";
                    }
                    else if (order.getStatus() == OrderStatus.COMPLETED)
                    {
                        message = "Order has already been completed";
                    }
                    throw new ResponseStatusException(HttpStatus.CONFLICT, message);
                }
                order.setStatus(OrderStatus.fromString(orderDTO.getStatus()));
                orderRepository.save(order);
            }
            else
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Order already has the status you want to updated to");
            }
        }
        else
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - trying to updated order status to null status");
        }
    }

    // Delete an order
    /*public void deleteOrder(int id)
    {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error!!! - Order associated with this id is not found in the database");
        }
        orderRepository.deleteById(id);
    }*/

    // Return an order
    public void returnOrder(int orderId)
    {
        //checking to see if order exists
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error!!! - Order not found");
        }

        // Update the order status to "Returned"
        order.setStatus(OrderStatus.RETURNED);

        // Save the updated order
        orderRepository.save(order);

        putProductBackToInventory(order);


        // Send notification to both admin and customer for successful return
        sendAdminNotification("Order Returned",
                "Order ID " + order.getId() + " has been successfully returned by "
                        + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order);

        sendCustomerReturnNotification(order);
    }

    public List<OrderDTO> getOrderByCustomerId(int id)
    {
        Customer customer = customerRepository.findById(id).orElse(null);

        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }

        List<Order> orders = orderRepository.findOrderByCustomerId(customer.getId());
        if (orders.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders associated with this customer found");
        }
        return orders.stream().map(this::mapToOrderDTO).toList();
    }

    public CustomerDTO getCustomerByOrderId(int orderId)
    {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        Customer customer = order.getCustomer();
        if (customer == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return new CustomerDTO(customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone());
    }

    private OrderDTO mapToOrderDTO(Order order)
    {
        Set<OrderProductDTO> orderProductDTOs = order.getOrderProducts().stream()
                .map(orderProduct -> new OrderProductDTO(orderProduct.getQuantity(), new ProductDTO(orderProduct.getProduct().getId(), orderProduct.getProduct().getName(), orderProduct.getProduct().getPrice(), orderProduct.getProduct().getType())))
                .collect(Collectors.toSet());
        OrderDTO orderDTO = new OrderDTO(order.getCreationDate(), order.getRentalTime(), order.isPaid(), order.getStatus(), order.getAddress(), order.getDeposit(), order.getTax(), order.getDeliveryFee(), order.getPrice(), order.getSubtotal());
        orderDTO.setPrice(order.getPrice());
        orderDTO.setId(order.getId());
        orderDTO.setOrderProducts(orderProductDTOs);
        return orderDTO;
    }

    // Helper method to send email notifications to the admin
    private void sendAdminNotification(String subject, String messageBody, Order order)
    {
        EmailDetails adminEmailDetails = new EmailDetails();
        adminEmailDetails.setRecipient("zhijunli7799@gmail.com"); //email of admin
        adminEmailDetails.setSubject(subject);

        String emailBody = messageBody +
                "\nOrder ID: " + order.getId() +
                "\nCustomer: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() +
                "\nTotal Amount: $" + order.getPrice();

        adminEmailDetails.setMessageBody(emailBody);
        emailService.sendSimpleEmail(adminEmailDetails);
    }

    // Helper method to send email notifications to the customer when the order is returned
    private void sendCustomerReturnNotification(Order order)
    {
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
    private int generateUniqueOrderId()
    {
        Random random = new Random();
        int orderId;
        boolean exists;
        do
        {
            orderId = 1000000000 + random.nextInt(1147483648);
            exists = orderRepository.existsById(orderId);
        }
        while (exists);

        return orderId;
    }

    //Function that checks orders for when they should be returned
    //Takes in all orders that are set at status RECEIVED and compares there
    //order pick up date against the order rental time requested
    private void OrderDueCheck(/*Order dbtable goes here*/)
    {
        /*TODO: for loop that goes through the dbtable checking for all orders
         * that are set as RECEIVED, if they are RECEIVED then they should
         * compare pick up date against the order rental time by checking the
         * date that it would be after the rental time as passed.
         * ie if order pick up is 10/25/24 and rental time is 2 days then
         * pick up should 10/27/24
         * this should at end of day every day.
         */

    }

    private void orderDueCheck()
    {
        List<Order> list = orderRepository.findReturnOrders();
        for (Order order : list) {
            EmailDetails customerEmailDetails = new EmailDetails();
            customerEmailDetails.setRecipient(order.getCustomer().getEmail());
            customerEmailDetails.setSubject("Order Return");

            String emailBody = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + "," +
                    "\n\nYour order with ID: " + order.getId() + " needs to be returned by tomorrow." +
                    "\n\nThank you for shopping with us!";

            customerEmailDetails.setMessageBody(emailBody);
            emailService.sendSimpleEmail(customerEmailDetails);
        }
    }

    public void deleteOrder(int orderId)
    {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error!!! - Order not found");
        }

        // Returning the products within the order
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            // Checking to make sure product exists before updating it
            Product product = orderProduct.getProduct();
            int quantity = orderProduct.getQuantity();

            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Product not found");
            }
            if (quantity == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Insufficient product quantity in order, check to see if it hasn't been processed already.");
            }
            if (order.getStatus() == OrderStatus.RETURNED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Check to see if it hasn't been processed already.");
            }

            // Update product quantity and save
            product.setQuantity(product.getQuantity() + quantity);
            productRepository.save(product);
        }

        try {

            if (order.getCustomer() != null) {
                order.setCustomer(null); // Remove the reference to account
                orderRepository.save(order);  // Save changes to persist them
            }

            // Delete the order products first
            // Deletes each related OrderProduct
            orderProductRepository.deleteAll(order.getOrderProducts());

            // Now delete the Order (this should not fail due to foreign key constraints)
            orderRepository.delete(order);

        } catch (DataIntegrityViolationException e) {
            // Handle the foreign key constraint violation
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete order due to foreign key constraints. Ensure all related entities are properly handled.");
        }
    }

    public void generateOrderInvoice(int orderId)
    {
        // Fetch the order by ID
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Order not found");
        }

        // Fetch the customer associated with the order
        Customer customer = order.getCustomer();

        // Populate model for invoice
        Map<String, Object> model = new HashMap<>();
        model.put("order", order);
        model.put("customer", customer);

        // Generate the PDF using the PdfService
        pdfService.generateInvoicePDF(model);
    }

    private void deleteOrderNotPayed()
    {
        orderRepository.deleteReceivedOrdersBeforeToday();;
    }

    private void putProductBackToInventory(Order order)
    {
        for (OrderProduct orderProduct : order.getOrderProducts())
        {
            // Checking to make sure product exists before updating it
            Product product = orderProduct.getProduct();
            int quantity = orderProduct.getQuantity();

            if (product == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ERROR!!! - Product not found");
            }
            if (quantity == 0)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERROR!!! - Insufficient product quantity in order, check to see if it hasn't been processed already.");
            }

            // Update product quantity and save
            product.setQuantity(product.getQuantity() + quantity);
            productRepository.save(product);
        }
    }
}