package com.example.food_order_system.controller;

import com.example.food_order_system.model.FoodOrder;
import com.example.food_order_system.service.FoodOrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class FoodOrderController {

    private final FoodOrderService foodOrderService;

    public FoodOrderController(FoodOrderService foodOrderService) {
        this.foodOrderService = foodOrderService;
    }

    @GetMapping
    public List<FoodOrder> getAllOrders() {
        return foodOrderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<FoodOrder> getOrderById(@PathVariable int orderId) {
        FoodOrder order = foodOrderService.getOrderById(orderId);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<FoodOrder> addOrder(@Valid @RequestBody FoodOrder order) {
        FoodOrder savedOrder = foodOrderService.addOrder(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<FoodOrder> updateOrder(
            @PathVariable int orderId,
            @Valid @RequestBody FoodOrder order) {

        FoodOrder updatedOrder =
                foodOrderService.updateOrder(orderId, order);

        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<FoodOrder> updateQuantity(
            @PathVariable int orderId,
            @RequestParam int quantity) {

        if (quantity < 1) {
            return ResponseEntity.badRequest().build();
        }

        FoodOrder updatedOrder =
                foodOrderService.updateQuantity(orderId, quantity);

        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int orderId) {
        boolean deleted = foodOrderService.deleteOrder(orderId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
