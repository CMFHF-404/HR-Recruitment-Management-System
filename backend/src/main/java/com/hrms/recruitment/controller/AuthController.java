package com.hrms.recruitment.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.recruitment.common.ApiResponse;
import com.hrms.recruitment.common.BusinessException;
import com.hrms.recruitment.repository.AdminRepository;
import com.hrms.recruitment.security.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AdminRepository admins, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var admin = admins.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return ApiResponse.ok(new LoginResponse(jwtService.createToken(admin.getUsername(), admin.getRole()),
                admin.getUsername(), admin.getName(), admin.getRole().name()));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, String username, String name, String role) {}
}
