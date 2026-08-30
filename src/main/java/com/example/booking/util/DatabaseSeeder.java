package com.example.booking.util;

import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Seed Admin User
            User admin = new User();
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("AdminPassword123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Seed Standard User
            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("UserPassword123"));
            user.setRole(Role.USER);
            userRepository.save(user);

            System.out.println("Database seeded with test users:");
            System.out.println("Admin: admin@example.com / AdminPassword123");
            System.out.println("User: user@example.com / UserPassword123");
        }
    }
}
