package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    /**
     * 인증코드 발송
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");

        try {
            emailService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "인증코드가 발송되었습니다. (콘솔 확인)");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 인증코드 확인
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String email = request.get("email");
        String code = request.get("code");

        boolean verified = emailService.verifyCode(email, code);

        if (verified) {
            // 세션에 인증된 이메일 저장
            session.setAttribute("verifiedEmail", email);
            response.put("success", true);
            response.put("message", "인증되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "인증코드가 일치하지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok(response);
    }
}
