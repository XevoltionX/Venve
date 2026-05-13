package com.cs.auth.controller;

import com.cs.auth.entity.User;
import com.cs.auth.repository.UserRepository;
import com.cs.common.constant.AuthConstant;
import com.cs.common.dto.ApiResponse;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserRepository userRepository;

    public UserAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private void checkAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("无管理员权限");
    }

    private Map<String, Object> toMap(User u) {
        var m = new HashMap<String, Object>();
        m.put("id", u.getId());
        m.put("userName", u.getUserName());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole());
        m.put("createdAt", u.getCreatedAt());
        return m;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String role,
            @RequestHeader(AuthConstant.HEADER_USER_ROLE) String adminRole) {
        checkAdmin(adminRole);

        Page<User> result;
        if (userName != null && !userName.isEmpty() && role != null && !role.isEmpty()) {
            result = userRepository.findByUserNameContainingAndRole(userName, role,
                    PageRequest.of(page, size, Sort.by("id").ascending()));
        } else if (userName != null && !userName.isEmpty()) {
            result = userRepository.findByUserNameContaining(userName,
                    PageRequest.of(page, size, Sort.by("id").ascending()));
        } else if (role != null && !role.isEmpty()) {
            result = userRepository.findByRole(role,
                    PageRequest.of(page, size, Sort.by("id").ascending()));
        } else {
            result = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        }

        return ApiResponse.ok(Map.of(
                "content", result.getContent().stream().map(this::toMap).collect(Collectors.toList()),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", result.getNumber()
        ));
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id,
                                       @RequestBody Map<String, Object> body,
                                       @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (body.get("role") != null) user.setRole((String) body.get("role"));
        if (body.get("phone") != null) user.setPhone((String) body.get("phone"));
        userRepository.save(user);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id,
                                       @RequestHeader(AuthConstant.HEADER_USER_ROLE) String role) {
        checkAdmin(role);
        userRepository.deleteById(id);
        return ApiResponse.ok("ok");
    }
}
