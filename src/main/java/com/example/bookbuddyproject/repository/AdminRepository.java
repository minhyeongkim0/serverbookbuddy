package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Admin;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminRepository {

    private final EntityManager em;

    public void save(Admin admin) {
        em.persist(admin);
    }

    public Admin findOne(Long id) {
        return em.find(Admin.class, id);
    }

    public List<Admin> findAll() {
        return em.createQuery("select a from Admin a order by a.createdAt desc", Admin.class)
                .getResultList();
    }

    public Optional<Admin> findByAdminId(String adminId) {
        List<Admin> result = em.createQuery(
                "select a from Admin a where a.adminId = :adminId", Admin.class)
                .setParameter("adminId", adminId)
                .getResultList();
        return result.stream().findFirst();
    }

    public boolean existsByAdminId(String adminId) {
        return findByAdminId(adminId).isPresent();
    }

    /**
     * 마스터 관리자 조회
     */
    public Optional<Admin> findMaster() {
        List<Admin> result = em.createQuery(
                "select a from Admin a where a.isMaster = true", Admin.class)
                .getResultList();
        return result.stream().findFirst();
    }
}
