package com.example.food_order_system.service;

import com.example.food_order_system.model.FoodOrder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FoodOrderService {

    private final List<FoodOrder> orders = new ArrayList<>();

    public List<FoodOrder> getAllOrders() {
        return orders;
    }

    public FoodOrder getOrderById(int orderId) {
        return orders.stream()
                .filter(order -> order.getOrderId() == orderId)
                .findFirst()
                .orElse(null);
    }

    public FoodOrder addOrder(FoodOrder order) {
        orders.add(order);
        return order;
    }

    public FoodOrder updateOrder(int orderId, FoodOrder updatedOrder) {
        FoodOrder existingOrder = getOrderById(orderId);

        if (existingOrder == null) {
            return null;
        }

        existingOrder.setCustomerName(updatedOrder.getCustomerName());
        existingOrder.setFoodItem(updatedOrder.getFoodItem());
        existingOrder.setQuantity(updatedOrder.getQuantity());
        existingOrder.setPrice(updatedOrder.getPrice());

        return existingOrder;
    }

    public FoodOrder updateQuantity(int orderId, int quantity) {
        FoodOrder existingOrder = getOrderById(orderId);

        if (existingOrder == null) {
            return null;
        }

        existingOrder.setQuantity(quantity);
        return existingOrder;
    }

    public boolean deleteOrder(int orderId) {
        return orders.removeIf(order -> order.getOrderId() == orderId);
    }
}
