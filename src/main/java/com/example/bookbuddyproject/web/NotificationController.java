package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Notification;
import com.example.bookbuddyproject.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 페이지
     */
    @GetMapping("/notifications")
    public String notificationList(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Notification> notifications = notificationService.findMyNotifications(loginMember.getId());
        model.addAttribute("notifications", notifications);

        // 모든 알림 읽음 처리
        notificationService.markAllAsRead(loginMember.getId());

        return "notifications/notificationList";
    }

    /**
     * 안 읽은 알림 개수 (AJAX용)
     */
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public Long getUnreadCount(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return 0L;
        }
        return notificationService.countUnread(loginMember.getId());
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/notifications/{id}/read")
    public String markAsRead(@PathVariable Long id, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }
}
