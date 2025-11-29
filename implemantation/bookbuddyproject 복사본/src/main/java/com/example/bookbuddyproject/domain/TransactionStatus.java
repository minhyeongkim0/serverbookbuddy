package com.example.bookbuddyproject.domain;

/**
 * 거래 상태
 */
public enum TransactionStatus {
    REQUESTED,   // 신청 대기 (구매자가 거래 신청)
    ACCEPTED,    // 거래 진행중 (판매자가 수락)
    REJECTED,    // 거래 거절 (판매자가 거절)
    PAID,        // 결제완료 (구매자가 결제)
    COMPLETED,   // 거래완료 (판매자가 완료 처리)
    CANCELLED    // 거래취소
}
