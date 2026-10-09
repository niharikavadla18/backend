package com.example.pharmacy.service;

import com.example.pharmacy.entity.Medicine;
import com.example.pharmacy.entity.Order;
import com.example.pharmacy.entity.OrderItem;
import com.example.pharmacy.repository.MedicineRepository;
import com.example.pharmacy.repository.OrderItemRepository;
import com.example.pharmacy.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    // Allowed order of statuses
    private static final List<String> FLOW =
            List.of("PLACED", "ACCEPTED", "SHIPPED", "DELIVERED");

    private final OrderRepository orderRepository;
    private final MedicineRepository medicineRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        MedicineRepository medicineRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.medicineRepository = medicineRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /*
     * Places a complete order in ONE step.
     * Price and total come from the medicines table (not from the browser),
     * stock is reduced, and if anything fails nothing is saved.
     */
    @Transactional
    public Order checkout(Long userId, Map<Long, Integer> requested) {

        if (requested == null || requested.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PLACED");
        order.setTotalAmount(0);
        order = orderRepository.save(order);

        double total = 0;

        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {

            int quantity = entry.getValue();

            if (quantity <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid quantity");
            }

            Medicine medicine = medicineRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Medicine not found"));

            if (medicine.getQuantity() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        medicine.getName() + " has only "
                                + medicine.getQuantity() + " in stock");
            }

            medicine.setQuantity(medicine.getQuantity() - quantity);
            medicineRepository.save(medicine);

            OrderItem item = new OrderItem(
                    null,
                    order.getId(),
                    medicine.getId(),
                    quantity,
                    medicine.getPrice()
            );

            orderItemRepository.save(item);

            total = total + medicine.getPrice() * quantity;
        }

        order.setTotalAmount(Math.round(total * 100.0) / 100.0);

        return orderRepository.save(order);
    }

    // Old endpoint kept so nothing breaks; it can no longer set a fake total
    public Order placeOrder(Order order) {

        order.setStatus("PLACED");
        order.setTotalAmount(0);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));
    }

    public Order updateStatus(Long id, String status) {

        Order order = getOrderById(id);

        String next = status == null ? "" : status.trim().toUpperCase();

        String current = order.getStatus() == null
                ? "PLACED"
                : order.getStatus().trim().toUpperCase();

        int to = FLOW.indexOf(next);
        int from = FLOW.indexOf(current);

        if (to < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }

        // same status again (for example two admin tabs clicked together)
        if (to == from) {
            return order;
        }

        // only one step forward is allowed
        if (from >= 0 && to != from + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot change from " + current + " to " + next);
        }

        order.setStatus(next);

        return orderRepository.save(order);
    }
}
