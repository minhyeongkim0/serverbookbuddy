package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.EmailVerification;
import com.example.bookbuddyproject.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    /**
     * 이메일 도메인 검증
     */
    public boolean isValidEmailDomain(String email) {
        return email != null && (email.endsWith("@yu.ac.kr") || email.endsWith("@ynu.ac.kr"));
    }

    /**
     * 인증코드 발송 (Gmail SMTP)
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

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[BookBuddy] 이메일 인증코드");
        message.setText("안녕하세요, BookBuddy입니다.\n\n"
                + "이메일 인증코드: " + code + "\n\n"
                + "유효시간: 5분\n\n"
                + "본인이 요청하지 않았다면 이 메일을 무시해주세요.");
        
        mailSender.send(message);
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
