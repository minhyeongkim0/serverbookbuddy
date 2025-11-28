package com.example.bookbuddyproject.domain;

/**
 * 회원 계정 상태
 */
public enum MemberStatus {
    ACTIVE,     // 활성 상태
    LOCKED      // 잠금 상태 (로그인 5회 실패)
}
