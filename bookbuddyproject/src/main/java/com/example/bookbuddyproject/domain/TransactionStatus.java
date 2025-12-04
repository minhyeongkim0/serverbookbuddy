package com.example.bookbuddyproject.domain;

/**
 * 거래 상태
 */
public enum TransactionStatus {
    REQUESTED("거래 신청"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    PAID("결제완료"),
    COMPLETED("거래완료"),
    CANCELLED("취소됨");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
