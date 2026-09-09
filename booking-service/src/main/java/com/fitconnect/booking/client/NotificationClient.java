package com.fitconnect.booking.client; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; @FeignClient(name="notification-service") public interface NotificationClient {@PostMapping("/api/notifications") void send(NotificationRequest request); record NotificationRequest(Long userId,String email,String type,String subject,String content){}
}
