package com.cs.booking.controller;

import com.cs.booking.entity.Notification;
import com.cs.booking.service.NotificationService;
import com.cs.common.constant.AuthConstant;
import com.cs.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<Notification>> list(@RequestHeader(AuthConstant.HEADER_USER_ID) Long userId) {
        return ApiResponse.ok(notificationService.listByUser(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@RequestHeader(AuthConstant.HEADER_USER_ID) Long userId) {
        return ApiResponse.ok(Map.of("count", notificationService.unreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<String> read(@PathVariable Long id,
                                     @RequestHeader(AuthConstant.HEADER_USER_ID) Long userId) {
        notificationService.markRead(id, userId);
        return ApiResponse.ok("ok");
    }
}
