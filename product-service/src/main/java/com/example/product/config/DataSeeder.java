package com.example.product.config;

import com.example.product.entity.Product;
import com.example.product.entity.Role;
import com.example.product.entity.UserAccount;
import com.example.product.repository.ProductRepository;
import com.example.product.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Seeds one USER and one ADMIN account if they don't exist yet. */
@Component
public class DataSeeder implements ApplicationRunner {

    private final UserAccountRepository users;
    private final ProductRepository products;
    private final PasswordEncoder encoder;
    private final String userPassword;
    private final String adminPassword;

    public DataSeeder(UserAccountRepository users, ProductRepository products, PasswordEncoder encoder,
                      @Value("${app.seed.user-password}") String userPassword,
                      @Value("${app.seed.admin-password}") String adminPassword) {
        this.users = users;
        this.products = products;
        this.encoder = encoder;
        this.userPassword = userPassword;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("user", userPassword, Role.USER);
        seed("admin", adminPassword, Role.ADMIN);
        seedProducts();
    }

    /** Adds sample products only when the table is empty, so restarts never duplicate them. */
    private void seedProducts() {
        if (products.count() > 0) {
            return;
        }
        products.saveAll(List.of(
                new Product("Laptop Pro 14", "14-inch laptop, 16GB RAM, 512GB SSD", new BigDecimal("1299.99"), 15),
                new Product("Wireless Mouse", "Ergonomic Bluetooth mouse", new BigDecimal("24.50"), 120),
                new Product("Mechanical Keyboard", "Hot-swappable, tactile switches", new BigDecimal("89.00"), 60),
                new Product("4K Monitor", "27-inch IPS 4K display", new BigDecimal("349.99"), 25),
                new Product("USB-C Hub", "7-in-1 hub with HDMI and card reader", new BigDecimal("39.95"), 80),
                new Product("Noise-Cancelling Headphones", "Over-ear, 30h battery", new BigDecimal("199.00"), 40),
                new Product("Webcam HD", "1080p webcam with built-in mic", new BigDecimal("59.99"), 55),
                new Product("Laptop Stand", "Adjustable aluminium stand", new BigDecimal("32.00"), 90)));
    }

    private void seed(String username, String rawPassword, Role role) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(new UserAccount(username, encoder.encode(rawPassword), role));
        }
    }
}
