package com.example.pharmacy.controller;

import com.example.pharmacy.entity.Order;
import com.example.pharmacy.entity.User;
import com.example.pharmacy.repository.UserRepository;
import com.example.pharmacy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "https://pharmacy-frontend-sb2d.onrender.com")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    // The logged-in user comes from the JWT token (not from the browser)
    private User currentUser(Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isAdmin(Authentication authentication) {

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // What the browser sends: only medicine ids and quantities (no prices)
    public static class CheckoutItem {

        private Long medicineId;
        private int quantity;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class CheckoutRequest {

        private List<CheckoutItem> items;

        public List<CheckoutItem> getItems() { return items; }
        public void setItems(List<CheckoutItem> items) { this.items = items; }
    }

    // The cart sends the whole order here in ONE request
    @PostMapping("/checkout")
    public Order checkout(@RequestBody CheckoutRequest request,
                          Authentication authentication) {

        User user = currentUser(authentication);

        Map<Long, Integer> requested = new LinkedHashMap<>();

        if (request != null && request.getItems() != null) {

            for (CheckoutItem item : request.getItems()) {

                if (item.getMedicineId() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Medicine id missing");
                }

                // same medicine twice = add the quantities
                requested.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
            }
        }

        return orderService.checkout(user.getId(), requested);
    }

    @PostMapping("/place")
    public Order placeOrder(@RequestBody Order order,
                            Authentication authentication) {

        User user = currentUser(authentication);

        order.setId(null);                 // always a brand new order
        order.setUserId(user.getId());     // always the logged-in user

        return orderService.placeOrder(order);
    }

    // ADMIN only (see SecurityConfig)
    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId,
                                       Authentication authentication) {

        User user = currentUser(authentication);

        if (!isAdmin(authentication) && !user.getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot view other users' orders");
        }

        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id,
                              Authentication authentication) {

        User user = currentUser(authentication);

        Order order = orderService.getOrderById(id);

        if (!isAdmin(authentication) && !user.getId().equals(order.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot view this order");
        }

        return order;
    }

    // ADMIN only (see SecurityConfig)
    @PutMapping("/status/{id}")
    public Order updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return orderService.updateStatus(id, status);
    }
}
