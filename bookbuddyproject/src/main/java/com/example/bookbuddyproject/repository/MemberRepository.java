package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public Optional<Member> findByLoginId(String loginId) {
        List<Member> result = em.createQuery(
                        "select m from Member m where m.loginId = :loginId", Member.class)
                .setParameter("loginId", loginId)
                .getResultList();
        return result.stream().findFirst();
    }

    public Optional<Member> findByEmail(String email) {
        List<Member> result = em.createQuery(
                        "select m from Member m where m.email = :email", Member.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst();
    }

    /**
     * 아이디 중복 체크
     */
    public boolean existsByLoginId(String loginId) {
        return findByLoginId(loginId).isPresent();
    }

    /**
     * 이메일 중복 체크
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
    /**
     * 전체 회원 목록 조회 (가입일 최신순 정렬)
     * 사용처: 관리자 페이지 회원 목록 초기 진입 시
     */
    public List<Member> findAllDesc() {
        return em.createQuery("select m from Member m order by m.createdAt desc", Member.class)
                .getResultList();
    }

    /**
     * 회원 검색 (아이디 포함 검색, 가입일 최신순 정렬)
     * 사용처: 관리자 페이지에서 아이디로 검색 시
     */
    public List<Member> findByLoginIdLike(String keyword) {
        return em.createQuery(
                "select m from Member m where m.loginId like :keyword order by m.createdAt desc", Member.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    /**
     * 이메일이 BANNED 상태인지 확인 (재가입 방지)
     */
    public boolean isEmailBanned(String email) {
        Long count = em.createQuery(
                "select count(m) from Member m where m.email = :email and m.status = 'BANNED'", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    /**
     * 신고 3회 이상인 회원 목록 조회
     */
    public List<Member> findReportedMembers() {
        return em.createQuery(
                "select m from Member m where m.reportCount >= 3 and m.status != 'BANNED' order by m.reportCount desc", Member.class)
                .getResultList();
    }
}
