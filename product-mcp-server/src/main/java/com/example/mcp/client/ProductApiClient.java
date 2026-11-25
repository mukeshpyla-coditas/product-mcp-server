package com.example.mcp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * HTTP client for product-service. Every call forwards the caller's own JWT,
 * so product-service decides what that role may do.
 */
@Component
public class ProductApiClient {

    private final RestClient client;

    public ProductApiClient(@Value("${product-service.base-url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Object list() {
        return call(() -> client.get().uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, bearer()).retrieve().body(Object.class));
    }

    public Object get(Long id) {
        return call(() -> client.get().uri("/api/products/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer()).retrieve().body(Object.class));
    }

    public Object create(String name, String description, BigDecimal price, int quantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);
        body.put("price", price);
        body.put("quantity", quantity);
        return call(() -> client.post().uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Object.class));
    }

    public Object delete(Long id) {
        return call(() -> {
            client.delete().uri("/api/products/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, bearer()).retrieve().toBodilessEntity();
            return Map.of("deleted", true, "id", id);
        });
    }

    private static String bearer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return "Bearer " + jwt.getToken().getTokenValue();
        }
        throw new IllegalStateException("No authenticated JWT in the current request");
    }

    /** Turns product-service HTTP errors into readable tool results instead of protocol failures. */
    private static Object call(Supplier<Object> action) {
        try {
            return action.get();
        } catch (RestClientResponseException e) {
            String hint = switch (e.getStatusCode().value()) {
                case 401 -> "Unauthorized: token missing, invalid or expired.";
                case 403 -> "Forbidden: your role is not allowed to perform this action (delete requires ADMIN).";
                case 404 -> "Not found.";
                case 400 -> "Bad request: " + e.getResponseBodyAsString();
                default -> "Product service error.";
            };
            return Map.of("error", hint, "status", e.getStatusCode().value());
        }
    }
}
