package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointRefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_refund_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private int amount;          // 환급 요청 포인트
    private String bankName;     // 사용자가 적는 은행명
    private String accountNumber;// 사용자가 적는 계좌번호

    @Enumerated(EnumType.STRING)
    private PointRequestStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    // == 생성 메서드 == //
    public static PointRefundRequest create(Member member, int amount, String bankName, String accountNumber) {
        PointRefundRequest request = new PointRefundRequest();
        request.member = member;
        request.amount = amount;
        request.bankName = bankName;
        request.accountNumber = accountNumber;
        request.status = PointRequestStatus.PENDING;
        request.requestedAt = LocalDateTime.now();
        return request;
    }

    // == 비즈니스 로직 == //
    public void approve() {
        if (this.status != PointRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 환급 요청입니다.");
        }
        this.status = PointRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == PointRequestStatus.PENDING;
    }
}
