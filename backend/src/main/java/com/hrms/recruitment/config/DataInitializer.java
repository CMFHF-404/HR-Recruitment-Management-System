package com.hrms.recruitment.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hrms.recruitment.domain.Admin;
import com.hrms.recruitment.repository.AdminRepository;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!adminRepository.existsByUsername("admin")) {
                adminRepository.save(new Admin("admin", passwordEncoder.encode("admin123"), "HR 管理员"));
            }
        };
    }
}
