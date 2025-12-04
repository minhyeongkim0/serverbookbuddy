package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * Use Case #1.1 회원가입, #1.2 로그인 기반
 * 
 * 구매자/판매자 구분 없이 "회원"으로 통일
 * - 책을 등록하면 → 판매자
 * - 책을 구매하면 → 구매자
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String loginId;  // 아이디 (4-20자, 영문+숫자)

    @Column(nullable = false)
    private String password;  // 비밀번호 (8-20자, 영문+숫자+특수문자)

    @Column(unique = true, nullable = false)
    private String email;  // 학교 이메일 (@ynu.ac.kr)

    @Column(nullable = false)
    private String department;  // 학과

    @Column(nullable = false)
    private String studentId;  // 학번

    private int loginFailCount;  // 로그인 실패 횟수 (5회 시 잠금)

    private LocalDateTime lockTime;  // 계정 잠금 해제 시간

    @Column(name = "point_balance") // 보유 포인트 (기본 0P)
    private int pointBalance = 0;

    private int reportCount = 0;  // 신고 누적 횟수

    @Enumerated(EnumType.STRING)
    private MemberStatus status;  // 계정 상태 (ACTIVE, LOCKED)

    private LocalDateTime createdAt;  // 가입일


    // === 생성 메서드 === //
    public static Member createMember(String loginId, String password, String email,
                                       String department, String studentId) {
        Member member = new Member();
        member.loginId = loginId;
        member.password = password;
        member.email = email;
        member.department = department;
        member.studentId = studentId;
        member.loginFailCount = 0;
        member.status = MemberStatus.ACTIVE;
        member.createdAt = LocalDateTime.now();
        return member;
    }

    // === 비즈니스 로직 === //

    /**
     * 관리자에 의한 강제 탈퇴 처리
     */
    public void ban() {
        this.status = MemberStatus.BANNED;
    }

    /**
     * 신고 횟수 증가
     */
    public void increaseReportCount() {
        this.reportCount++;
    }

    /**
     * 신고 횟수 리셋 (문제없음 처리 시)
     */
    public void resetReportCount() {
        this.reportCount = 0;
    }

    /**
     * 포인트 충전/적립
     */
    public void addPoints(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("포인트 충전 금액은 0보다 커야 합니다.");
        }
        this.pointBalance += amount;
    }

    /**
     * 포인트 사용/차감
     */
    public void usePoints(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("포인트 사용 금액은 0보다 커야 합니다.");
        }
        if (this.pointBalance < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.pointBalance -= amount;
    }

    /**
     * 로그인 실패 시 실패 횟수 증가
     * 5회 도달 시 계정 잠금 (10분)
     */
    public void increaseLoginFailCount() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.status = MemberStatus.LOCKED;
            this.lockTime = LocalDateTime.now().plusMinutes(10);
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        if (this.status == MemberStatus.LOCKED && isLockTimeExpired()) {
            this.status = MemberStatus.ACTIVE;
            this.lockTime = null;
        }
    }

    /**
     * 잠금 시간 만료 여부 확인
     */
    public boolean isLockTimeExpired() {
        if (this.lockTime == null) return true;
        return LocalDateTime.now().isAfter(this.lockTime);
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isLocked() {
        // 강퇴된 회원은 영구 잠금
        if (this.status == MemberStatus.BANNED) return true;

        if (this.status != MemberStatus.LOCKED) return false;
        if (isLockTimeExpired()) {
            this.status = MemberStatus.ACTIVE;
            this.lockTime = null;
            return false;
        }
        return true;
    }
}
