package com.example.bookbuddyproject.domain;

public enum PointRequestStatus {
    PENDING,   // 대기중
    APPROVED,  // 승인(지급/환급 완료)
    REJECTED   // 거절 (선택사항, 나중에 사용할 수 있음)
}
