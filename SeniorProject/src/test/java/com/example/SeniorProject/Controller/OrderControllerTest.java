package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Configuration.*;
import com.example.SeniorProject.DTOs.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.SeniorProject.Service.OrderService;

import java.util.*;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1);
        // Set other necessary fields in testOrderDTO
    }

    @Test
    @WithMockUser
    void createOrder_Success() throws Exception {
        // Arrange
        int customerId = 1;
        OrderDTO inputOrderDTO = new OrderDTO();
        when(orderService.createOrder(eq(customerId), any(OrderDTO.class)))
                .thenReturn(testOrderDTO);

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrderDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "An order has been successfully created, we will send a confirmation email to your email address soon."))
                .andExpect(jsonPath("$.orderId").value(testOrderDTO.getId()));
    }

    @Test
    @WithMockUser
    void createOrder_HandlesResponseStatusException() throws Exception {
        // Arrange
        int customerId = 1;
        OrderDTO inputOrderDTO = new OrderDTO();
        String errorMessage = "Invalid order data";

        when(orderService.createOrder(eq(customerId), any(OrderDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage));

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrderDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    @WithMockUser
    void createOrder_HandlesUnexpectedException() throws Exception {
        // Arrange
        int customerId = 1;
        OrderDTO inputOrderDTO = new OrderDTO();

        when(orderService.createOrder(eq(customerId), any(OrderDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/order/create")
                        .param("id", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrderDTO))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error occurred"));
    }

    @Test
    void cancelOrder_WhenRoleIsAdmin_ShouldCancelOrderSuccessfully() throws Exception {
        // Arrange
        int orderId = 1;
        String role = "admin";

        // Act & Assert
        mockMvc.perform(post("/order/cancel")
                        .param("orderId", String.valueOf(orderId))
                        .param("role", role))
                .andExpect(status().isOk())
                .andExpect(content().string("An order has been successfully cancelled"));

        verify(orderService, times(1)).orderCancelledByAdmin(orderId);
    }

    @Test
    void cancelOrder_WhenRoleIsUser_ShouldCancelOrderSuccessfully() throws Exception {
        // Arrange
        int orderId = 1;
        String role = "user";

        // Act & Assert
        mockMvc.perform(post("/order/cancel")
                        .param("orderId", String.valueOf(orderId))
                        .param("role", role))
                .andExpect(status().isOk())
                .andExpect(content().string("An order has been successfully cancelled"));

        verify(orderService, times(1)).orderCancelledByCustomer(orderId);
    }

    @Test
    void cancelOrder_WhenRoleIsInvalid_ShouldReturnBadRequest() throws Exception {
        // Arrange
        int orderId = 1;
        String role = "invalid_role";

        // Act & Assert
        mockMvc.perform(post("/order/cancel")
                        .param("orderId", String.valueOf(orderId))
                        .param("role", role))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing or unknown role"));

        verify(orderService, never()).orderCancelledByAdmin(anyInt());
        verify(orderService, never()).orderCancelledByCustomer(anyInt());
    }

    @Test
    void cancelOrder_WhenServiceThrowsException_ShouldPropagateException() throws Exception {
        // Arrange
        int orderId = 1;
        String role = "admin";

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"))
                .when(orderService).orderCancelledByAdmin(orderId);

        // Act & Assert
        mockMvc.perform(post("/order/cancel")
                        .param("orderId", String.valueOf(orderId))
                        .param("role", role))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));
    }

    @Test
    void getCurrentOrders_WhenNoOrders_ShouldReturnEmptyList() throws Exception {
        // Arrange
        int customerId = 1;
        List<OrderDTO> emptyOrders = Collections.emptyList();

        when(orderService.getCurrentOrders(customerId)).thenReturn(emptyOrders);

        // Act & Assert
        mockMvc.perform(get("/order/currentOrders")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(orderService, times(1)).getCurrentOrders(customerId);
    }

    @Test
    void getCurrentOrders_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        int customerId = 999;

        when(orderService.getCurrentOrders(customerId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Act & Assert
        mockMvc.perform(get("/order/currentOrders")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));

        verify(orderService, times(1)).getCurrentOrders(customerId);
    }

    @Test
    void getCurrentOrders_WhenServiceThrowsException_ShouldReturnAppropriateStatus() throws Exception {
        // Arrange
        int customerId = 1;

        when(orderService.getCurrentOrders(customerId))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));

        // Act & Assert
        mockMvc.perform(get("/order/currentOrders")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal server error"));

        verify(orderService, times(1)).getCurrentOrders(customerId);
    }

    @Test
    void getPastOrders_WhenNoOrders_ShouldReturnEmptyList() throws Exception {
        // Arrange
        int customerId = 1;
        List<OrderDTO> emptyOrders = Collections.emptyList();

        when(orderService.getPastOrders(customerId)).thenReturn(emptyOrders);

        // Act & Assert
        mockMvc.perform(get("/order/pastOrders")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"))
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService, times(1)).getPastOrders(customerId);
    }

    @Test
    void getPastOrders_WhenCustomerNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        int customerId = 999;
        String errorMessage = "Customer not found";

        when(orderService.getPastOrders(customerId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

        // Act & Assert
        mockMvc.perform(get("/order/pastOrders")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(orderService, times(1)).getPastOrders(customerId);
    }

    @Test
    void getPastOrders_WhenServiceThrowsInternalError_ShouldReturnServerError() throws Exception {
        // Arrange
        int customerId = 1;
        String errorMessage = "Internal server error occurred";

        when(orderService.getPastOrders(customerId))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage));

        // Act & Assert
        mockMvc.perform(get("/order/pastOrders")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(errorMessage));

        verify(orderService, times(1)).getPastOrders(customerId);
    }

    @Test
    @WithMockUser // Simulates an authenticated user
    void getAllOrders_Success() throws Exception
    {
        OrderDTO order1 = new OrderDTO();
        order1.setId(1);

        OrderDTO order2 = new OrderDTO();
        order2.setId(2);
        // Arrange
        when(orderService.getAllOrders()).thenReturn(List.of(order1, order2));

        // Act & Assert
        mockMvc.perform(get("/order/getAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @WithMockUser
    void getAllOrders_EmptyList() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/order/getAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @WithMockUser
    void getAllOrders_ThrowsNotFound() throws Exception {
        // Arrange
        String errorMessage = "No orders found";
        when(orderService.getAllOrders())
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

        // Act & Assert
        mockMvc.perform(get("/order/getAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @WithMockUser
    void getAllOrders_ThrowsInternalServerError() throws Exception {
        // Arrange
        String errorMessage = "Internal server error occurred";
        when(orderService.getAllOrders())
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage));

        // Act & Assert
        mockMvc.perform(get("/order/getAll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(errorMessage));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @WithMockUser
    void testGetOrderById_Success() throws Exception {
        // Arrange
        int orderId = 1;
        OrderDTO mockOrder = new OrderDTO();
        mockOrder.setId(orderId);

        when(orderService.getOrderById(orderId)).thenReturn(mockOrder);

        // Act & Assert
        mockMvc.perform(get("/order/getById")
                        .param("id", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        // Verify interaction
        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    @WithMockUser
    void testGetOrderById_NotFound() throws Exception {
        // Arrange
        int orderId = 999;

        when(orderService.getOrderById(orderId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Act & Assert
        mockMvc.perform(get("/order/getById")
                        .param("id", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));

        // Verify interaction
        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    @WithMockUser
    void testGetOrderById_InternalServerError() throws Exception {
        // Arrange
        int orderId = 500;

        when(orderService.getOrderById(orderId)).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

        // Act & Assert
        mockMvc.perform(get("/order/getById")
                        .param("id", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal error"));

        // Verify interaction
        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    @WithMockUser("ADMIN")
    void testDeleteOrder_Success() throws Exception {
        // Arrange
        int orderId = 1;
        doNothing().when(orderService).deleteOrder(orderId);

        // Act & Assert
        mockMvc.perform(delete("/order/delete")
                        .param("orderId", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted successfully"));

        // Verify interaction
        verify(orderService, times(1)).deleteOrder(orderId);
    }

    @Test
    @WithMockUser("ADMIN")
    void testDeleteOrder_NotFound() throws Exception {
        // Arrange
        int orderId = 999;

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"))
                .when(orderService).deleteOrder(orderId);

        // Act & Assert
        mockMvc.perform(delete("/order/delete")
                        .param("orderId", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));

        // Verify interaction
        verify(orderService, times(1)).deleteOrder(orderId);
    }

    @Test
    @WithMockUser("ADMIN")
    void testDeleteOrder_InternalServerError() throws Exception {
        // Arrange
        int orderId = 500;

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"))
                .when(orderService).deleteOrder(orderId);

        // Act & Assert
        mockMvc.perform(delete("/order/delete")
                        .param("orderId", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal error"));

        // Verify interaction
        verify(orderService, times(1)).deleteOrder(orderId);
    }

    @Test
     void testUpdateOrder_Success() throws Exception {
        // Arrange
        int orderId = 10;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(orderId);
        orderDTO.setStatus("COMPLETED");
        orderDTO.setSubtotal(100.50);

        doNothing().when(orderService).updateOrderStatus(eq(orderId), eq(orderDTO));

        // Act & Assert
        mockMvc.perform(put("/order/update")
                        .param("orderId", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("The order has been successfully updated"));

    }

    @Test
    void testUpdateOrder_NotFound() throws Exception {
        // Arrange
        int orderId = 999;
        OrderDTO orderDTO = new OrderDTO();

        // Create a custom matcher or comparison method
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"))
                .when(orderService).updateOrderStatus(
                        eq(orderId),
                        any(OrderDTO.class)
                );

        // Act & Assert
        mockMvc.perform(put("/order/update")
                        .param("orderId", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));

        // Verify interaction
        verify(orderService, times(1)).updateOrderStatus(
                eq(orderId),
                any(OrderDTO.class)
        );
    }

    @Test
    void returnOrder_ShouldReturnOk_WhenOrderIsReturnedSuccessfully() throws Exception {
        int orderId = 1;

        mockMvc.perform(post("/order/return")
                        .param("orderId", String.valueOf(orderId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Order returned successfully"));

        verify(orderService).returnOrder(orderId);
    }

    @Test
    void returnOrder_ShouldReturnError_WhenResponseStatusExceptionIsThrown() throws Exception {
        int orderId = 1;
        String errorMessage = "Order not found";
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);

        doThrow(exception).when(orderService).returnOrder(orderId);

        mockMvc.perform(post("/order/return")
                        .param("orderId", String.valueOf(orderId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(orderService).returnOrder(orderId);
    }

    @Test
    void sendOrderDueForReturnNotification_ShouldCallOrderDueCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/order/sendOrderDueForReturnNotification"))
                .andExpect(status().isOk()); // Since no response body is returned

        verify(orderService).orderDueCheck();
    }

    @Test
    @WithMockUser()
    void testGetCustomerByOrderId_Success() throws Exception {
        // Arrange
        int orderId = 1;
        CustomerDTO mockCustomer = new CustomerDTO("John", "Doe", "", "");
        mockCustomer.setId(orderId);
        when(orderService.getCustomerByOrderId(orderId)).thenReturn(mockCustomer);

        // Act & Assert
        mockMvc.perform(get("/order/getCustomerByOrderId")
                        .param("orderId", String.valueOf(orderId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.firstName").value("John")
                );

        // Verify interaction
        verify(orderService, times(1)).getCustomerByOrderId(orderId);
    }

    @Test
    @WithMockUser()
    void getCustomerByOrderId_ShouldReturnCustomer_WhenOrderExists() throws Exception {
        int orderId = 1;
        CustomerDTO customerDTO = new CustomerDTO( "John", "Doe", "johndoe@example.com", "");
        customerDTO.setId(orderId);

        when(orderService.getCustomerByOrderId(orderId)).thenReturn(customerDTO);

        mockMvc.perform(get("/order/getCustomerByOrderId")
                        .param("orderId", String.valueOf(orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerDTO.getId()))
                .andExpect(jsonPath("$.firstName").value(customerDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(customerDTO.getLastName()))
                .andExpect(jsonPath("$.email").value(customerDTO.getEmail()));

        verify(orderService).getCustomerByOrderId(orderId);
    }

    @Test
    @WithMockUser
    void getCustomerByOrderId_ShouldReturnError_WhenOrderNotFound() throws Exception {
        int orderId = 1;
        String errorMessage = "Order not found";
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);

        when(orderService.getCustomerByOrderId(orderId)).thenThrow(exception);

        mockMvc.perform(get("/order/getCustomerByOrderId")
                        .param("orderId", String.valueOf(orderId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        verify(orderService).getCustomerByOrderId(orderId);
    }

    @Test
    void getOrderByCustomerId_ShouldReturnOrders_WhenCustomerIdExists() throws Exception {
        // Given
        int customerId = 1;
        List<OrderDTO> orderDTOs = Arrays.asList(
                new OrderDTO(),
                new OrderDTO()
        );

        // Mock the service method
        when(orderService.getOrderByCustomerId(customerId)).thenReturn(orderDTOs);

        // Perform the GET request
        mockMvc.perform(get("/order/getOrderByCustomerId")
                        .param("id", String.valueOf(customerId))
                        .with(user("testUser").roles("USER"))) // Simulate an authenticated user
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(orderDTOs.size()))
                .andExpect(jsonPath("$[0].id").value(orderDTOs.get(0).getId()))
                .andExpect(jsonPath("$[1].id").value(orderDTOs.get(1).getId()));

        // Verify service call
        verify(orderService).getOrderByCustomerId(customerId);
    }

    @Test
    void getOrderByCustomerId_ShouldReturnError_WhenCustomerIdNotFound() throws Exception {
        // Given
        int customerId = 1;
        String errorMessage = "Customer not found";
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);

        // Mock the service method to throw exception
        when(orderService.getOrderByCustomerId(customerId)).thenThrow(exception);

        // Perform the GET request
        mockMvc.perform(get("/order/getOrderByCustomerId")
                        .param("id", String.valueOf(customerId))
                        .with(user("testUser").roles("USER"))) // Simulate an authenticated user
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMessage));

        // Verify service call
        verify(orderService).getOrderByCustomerId(customerId);
    }
}