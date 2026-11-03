package com.example.food_order_system.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrder {

    private int orderId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Food item is required")
    private String foodItem;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Price is required")
    private BigDecimal price;
}
