package com.example.product.dto;

import com.example.product.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long id, String name, String description, BigDecimal price,
                              int quantity, Instant createdAt) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getQuantity(), p.getCreatedAt());
    }
}
