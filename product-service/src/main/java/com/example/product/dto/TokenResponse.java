package com.example.product.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, String role) {
}
