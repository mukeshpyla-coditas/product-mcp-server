package com.example.product.service;

import com.example.product.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository users;

    public CustomUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername()).password(u.getPassword())
                        .roles(u.getRole().name()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
    }
}
