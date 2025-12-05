package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Notification;
import com.example.bookbuddyproject.domain.NotificationType;
import com.example.bookbuddyproject.domain.Transaction;
import com.example.bookbuddyproject.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     */
    @Transactional
    public Long createNotification(Member member, Transaction transaction, 
                                    NotificationType type, String message) {
        Notification notification = Notification.createNotification(member, transaction, type, message);
        notificationRepository.save(notification);
        return notification.getId();
    }

    /**
     * 내 알림 목록 조회
     */
    public List<Notification> findMyNotifications(Long memberId) {
        return notificationRepository.findByMember(memberId);
    }

    /**
     * 안 읽은 알림 개수
     */
    public Long countUnread(Long memberId) {
        return notificationRepository.countUnread(memberId);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findOne(notificationId);
        if (notification != null) {
            notification.markAsRead();
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long memberId) {
        List<Notification> notifications = notificationRepository.findUnreadByMember(memberId);
        for (Notification notification : notifications) {
            notification.markAsRead();
        }
    }
}
