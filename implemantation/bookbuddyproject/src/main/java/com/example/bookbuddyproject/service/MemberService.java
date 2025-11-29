package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원가입
     * Use Case #1.1
     */
    @Transactional
    public Long join(String loginId, String password, String email,
                     String department, String studentId) {
        // 이메일 도메인 검증
        validateEmailDomain(email);
        // 아이디 중복 검증
        validateDuplicateLoginId(loginId);
        // 이메일 중복 검증
        validateDuplicateEmail(email);

        Member member = Member.createMember(loginId, password, email, department, studentId);
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 로그인
     * Use Case #1.2
     */
    @Transactional
    public Member login(String loginId, String password) {
        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);

        if (optionalMember.isEmpty()) {
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        Member member = optionalMember.get();

        // 계정 잠금 확인
        if (member.isLocked()) {
            throw new IllegalStateException("로그인 시도 횟수 초과. 잠시 후 다시 시도해주세요.");
        }

        // 비밀번호 확인 (실제로는 암호화된 비밀번호 비교 필요)
        if (!member.getPassword().equals(password)) {
            member.increaseLoginFailCount();
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 시 실패 횟수 초기화
        member.resetLoginFailCount();
        return member;
    }

    /**
     * 회원 조회
     */
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // === 검증 메서드 === //

    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 등록된 이메일입니다.");
        }
    }

    private void validateEmailDomain(String email) {
        if (!email.endsWith("@yu.ac.kr") && !email.endsWith("@ynu.ac.kr")) {
            throw new IllegalStateException("영남대학교 메일로만 가입 가능합니다. (@yu.ac.kr 또는 @ynu.ac.kr)");
        }
    }
}
