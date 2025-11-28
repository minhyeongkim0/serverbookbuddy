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
}
