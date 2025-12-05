package com.example.bookbuddyproject.domain;

/**
 * 알림 유형
 */
public enum NotificationType {
    TRANSACTION_REQUEST("거래 신청"),
    TRANSACTION_ACCEPTED("거래 수락"),
    TRANSACTION_REJECTED("거래 거절"),
    TRANSACTION_PAID("결제 완료"),
    TRANSACTION_COMPLETED("거래 완료");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
