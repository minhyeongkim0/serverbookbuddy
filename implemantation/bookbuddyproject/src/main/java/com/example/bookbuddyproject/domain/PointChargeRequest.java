package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointChargeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_charge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 신청 포인트(=입금 금액과 1:1이라고 가정)
    private int amount;

    @Enumerated(EnumType.STRING)
    private PointRequestStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    // 입금 계좌는 고정이므로 DB에는 굳이 안 넣고 화면에서 하드코딩해도 됨

    // == 생성 메서드 == //
    public static PointChargeRequest create(Member member, int amount) {
        PointChargeRequest request = new PointChargeRequest();
        request.member = member;
        request.amount = amount;
        request.status = PointRequestStatus.PENDING;
        request.requestedAt = LocalDateTime.now();
        return request;
    }

    // == 비즈니스 로직 == //
    public void approve() {
        if (this.status != PointRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 충전 요청입니다.");
        }
        this.status = PointRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == PointRequestStatus.PENDING;
    }
}
