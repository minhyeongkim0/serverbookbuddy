package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 알림 받는 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;  // 관련 거래

    @Enumerated(EnumType.STRING)
    private NotificationType type;  // 알림 유형

    private String message;  // 알림 메시지

    private boolean isRead;  // 읽음 여부

    private LocalDateTime createdAt;

    // === 생성 메서드 === //
    public static Notification createNotification(Member member, Transaction transaction, 
                                                   NotificationType type, String message) {
        Notification notification = new Notification();
        notification.member = member;
        notification.transaction = transaction;
        notification.type = type;
        notification.message = message;
        notification.isRead = false;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }

    // === 비즈니스 로직 === //
    public void markAsRead() {
        this.isRead = true;
    }
}
