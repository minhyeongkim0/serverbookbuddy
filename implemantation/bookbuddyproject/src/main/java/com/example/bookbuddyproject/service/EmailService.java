package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.EmailVerification;
import com.example.bookbuddyproject.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;

    /**
     * 이메일 도메인 검증
     */
    public boolean isValidEmailDomain(String email) {
        return email != null && (email.endsWith("@yu.ac.kr") || email.endsWith("@ynu.ac.kr"));
    }

    /**
     * 인증코드 발송 (콘솔 출력)
     */
    @Transactional
    public void sendVerificationCode(String email) {
        // 이메일 도메인 검증
        if (!isValidEmailDomain(email)) {
            throw new IllegalArgumentException("영남대학교 메일로만 인증 가능합니다. (@yu.ac.kr 또는 @ynu.ac.kr)");
        }

        // 6자리 랜덤 코드 생성
        String code = generateCode();

        // DB에 저장
        EmailVerification ev = EmailVerification.createVerification(email, code);
        emailVerificationRepository.save(ev);

        // 콘솔에 출력 (메일 발송 대신)
        System.out.println();
        System.out.println("========================================");
        System.out.println("이메일 인증코드");
        System.out.println("받는사람: " + email);
        System.out.println("인증코드: " + code);
        System.out.println("유효시간: 5분");
        System.out.println("========================================");
        System.out.println();
    }

    /**
     * 인증코드 검증
     */
    @Transactional
    public boolean verifyCode(String email, String inputCode) {
        Optional<EmailVerification> optional = emailVerificationRepository.findByEmail(email);

        if (optional.isEmpty()) {
            return false;  // 인증 요청 없음
        }

        EmailVerification ev = optional.get();

        // 만료 확인
        if (ev.isExpired()) {
            return false;
        }

        // 코드 일치 확인
        if (!ev.matchCode(inputCode)) {
            return false;
        }

        // 인증 성공
        ev.verify();
        return true;
    }

    /**
     * 이메일 인증 완료 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        Optional<EmailVerification> optional = emailVerificationRepository.findByEmail(email);
        return optional.isPresent() && optional.get().isVerified();
    }

    /**
     * 6자리 랜덤 코드 생성
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);  // 100000 ~ 999999
        return String.valueOf(code);
    }
}
