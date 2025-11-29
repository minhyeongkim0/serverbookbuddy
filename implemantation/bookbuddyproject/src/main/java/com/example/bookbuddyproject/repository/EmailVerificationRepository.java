package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.EmailVerification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository {

    private final EntityManager em;

    public void save(EmailVerification ev) {
        em.persist(ev);
    }

    public Optional<EmailVerification> findByEmail(String email) {
        List<EmailVerification> result = em.createQuery(
                "select e from EmailVerification e where e.email = :email order by e.id desc", 
                EmailVerification.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();
        return result.stream().findFirst();
    }

    public void delete(EmailVerification ev) {
        em.remove(ev);
    }
}
