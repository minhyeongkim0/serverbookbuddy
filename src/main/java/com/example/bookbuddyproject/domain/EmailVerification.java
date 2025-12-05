package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;           // 인증 요청 이메일
    private String code;            // 6자리 인증코드
    private LocalDateTime expireTime;   // 만료시간
    private boolean verified;       // 인증 완료 여부

    public static EmailVerification createVerification(String email, String code) {
        EmailVerification ev = new EmailVerification();
        ev.email = email;
        ev.code = code;
        ev.expireTime = LocalDateTime.now().plusMinutes(5);  // 5분 유효
        ev.verified = false;
        return ev;
    }

    // 인증 성공 처리
    public void verify() {
        this.verified = true;
    }

    // 만료 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expireTime);
    }

    // 코드 일치 확인
    public boolean matchCode(String inputCode) {
        return this.code.equals(inputCode);
    }
}
