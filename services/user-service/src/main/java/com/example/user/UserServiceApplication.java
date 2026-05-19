package com.example.user;

import com.example.user.model.User;
import com.example.user.enums.Role;
import com.example.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                User admin = new User("admin", encoder.encode("admin123"), "admin@example.com", Role.ADMIN);
                repo.save(admin);
                System.out.println("Default ADMIN user created: username=admin, password=admin123");
            }
        };
    }
}
