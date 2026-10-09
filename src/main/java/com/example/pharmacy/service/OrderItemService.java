package com.example.pharmacy.service;

import com.example.pharmacy.entity.Medicine;
import com.example.pharmacy.entity.OrderItem;
import com.example.pharmacy.repository.MedicineRepository;
import com.example.pharmacy.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final MedicineRepository medicineRepository;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            MedicineRepository medicineRepository) {
        this.orderItemRepository = orderItemRepository;
        this.medicineRepository = medicineRepository;
    }

    public OrderItem addOrderItem(OrderItem orderItem) {

        Medicine medicine = medicineRepository.findById(orderItem.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if (medicine.getQuantity() < orderItem.getQuantity()) {
            throw new RuntimeException("Not enough medicine stock");
        }

        medicine.setQuantity(
                medicine.getQuantity() - orderItem.getQuantity()
        );

        medicineRepository.save(medicine);

        return orderItemRepository.save(orderItem);
    }

    public List<OrderItem> getItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}