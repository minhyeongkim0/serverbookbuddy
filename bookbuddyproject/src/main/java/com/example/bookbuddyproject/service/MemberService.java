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
     */
    @Transactional
    public Long join(String loginId, String password, String email,
                     String department, String studentId) {
        // 이메일 도메인 검증
        validateEmailDomain(email);
        // 강퇴된 이메일 검증
        validateBannedEmail(email);
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
     */
    @Transactional
    public Member login(String loginId, String password) {
        Optional<Member> optionalMember = memberRepository.findByLoginId(loginId);

        if (optionalMember.isEmpty()) {
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        Member member = optionalMember.get();

        // 강퇴된 계정 확인
        if (member.getStatus() == com.example.bookbuddyproject.domain.MemberStatus.BANNED) {
            throw new IllegalStateException("강퇴된 계정입니다. 로그인할 수 없습니다.");
        }

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
    
    /**
     * 회원 관리용 목록 조회 (검색 기능 포함)
     */
    public List<Member> findMembers(String keyword) {
        // 검색어가 있으면 아이디 검색, 없으면 전체 최신순 조회
        if (keyword != null && !keyword.isBlank()) {
            return memberRepository.findByLoginIdLike(keyword);
        }
        return memberRepository.findAllDesc();
    }

    /**
     * 회원 강제 탈퇴 (Ban)
     */
    @Transactional
    public void banMember(Long memberId) {
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalStateException("존재하지 않는 회원입니다.");
        }
        // Member 엔티티의 ban() 메서드 호출 (상태를 BANNED로 변경)
        member.ban();
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

    private void validateBannedEmail(String email) {
        if (memberRepository.isEmailBanned(email)) {
            throw new IllegalStateException("강퇴된 이메일로는 재가입할 수 없습니다.");
        }
    }
}
