package com.bookmyshow.service;

import com.bookmyshow.dto.Dto.*;
import com.bookmyshow.entity.Role;
import com.bookmyshow.entity.User;
import com.bookmyshow.exception.ApiException;
import com.bookmyshow.repository.UserRepository;
import com.bookmyshow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthRes register(RegisterReq req) {
        String email = req.email().toLowerCase().trim();
        if (userRepo.existsByEmail(email)) throw ApiException.conflict("Email already registered");
        User u = new User();
        u.setName(req.name());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.USER);
        userRepo.save(u);
        return new AuthRes(jwt.generate(email, u.getRole().name()), u.getName(), email, u.getRole().name());
    }

    public AuthRes login(LoginReq req) {
        String email = req.email().toLowerCase().trim();
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!encoder.matches(req.password(), u.getPassword()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return new AuthRes(jwt.generate(email, u.getRole().name()), u.getName(), email, u.getRole().name());
    }
}
