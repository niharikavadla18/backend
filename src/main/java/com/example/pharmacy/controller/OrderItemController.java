package com.example.pharmacy.controller;

import com.example.pharmacy.entity.Order;
import com.example.pharmacy.entity.OrderItem;
import com.example.pharmacy.entity.User;
import com.example.pharmacy.repository.UserRepository;
import com.example.pharmacy.service.OrderItemService;
import com.example.pharmacy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/order-item")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderItemController(OrderItemService orderItemService,
                               OrderService orderService,
                               UserRepository userRepository) {
        this.orderItemService = orderItemService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // Items are now created only by /order/checkout, so there is no "add" endpoint here.

    @GetMapping("/order/{orderId}")
    public List<OrderItem> getItemsByOrderId(
            @PathVariable Long orderId,
            Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        Order order = orderService.getOrderById(orderId);

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!admin && !user.getId().equals(order.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot view this order");
        }

        return orderItemService.getItemsByOrderId(orderId);
    }
}
