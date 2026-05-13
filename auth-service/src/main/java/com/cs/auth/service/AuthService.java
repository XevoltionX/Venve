package com.cs.auth.service;

import com.cs.auth.dto.LoginRequest;
import com.cs.auth.dto.RegisterRequest;
import com.cs.auth.entity.User;
import com.cs.auth.repository.UserRepository;
import com.cs.common.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (userRepository.existsByUserName(req.getUserName())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUserName(req.getUserName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        if (userRepository.count() == 0) {
            user.setRole("ADMIN");
        }
        userRepository.save(user);

        String token = JwtUtil.generate(user.getId(), user.getUserName(), user.getRole());

        return Map.of(
                "userId", user.getId(),
                "userName", user.getUserName(),
                "role", user.getRole(),
                "token", token
        );
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = JwtUtil.generate(user.getId(), user.getUserName(), user.getRole());

        return Map.of(
                "userId", user.getId(),
                "userName", user.getUserName(),
                "role", user.getRole(),
                "token", token
        );
    }
}
