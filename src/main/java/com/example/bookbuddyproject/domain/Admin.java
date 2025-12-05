package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 관리자 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", unique = true, nullable = false)
    private String adminId;  // 관리자 아이디

    @Column(nullable = false)
    private String password;  // 비밀번호

    private boolean isMaster;  // 마스터 관리자 여부

    private LocalDateTime createdAt;

    // === 생성 메서드 === //
    public static Admin createAdmin(String adminId, String password, boolean isMaster) {
        Admin admin = new Admin();
        admin.adminId = adminId;
        admin.password = password;
        admin.isMaster = isMaster;
        admin.createdAt = LocalDateTime.now();
        return admin;
    }
}
