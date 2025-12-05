package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Report;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final EntityManager em;

    public void save(Report report) {
        em.persist(report);
    }

    public Report findOne(Long id) {
        return em.find(Report.class, id);
    }

    public List<Report> findAll() {
        return em.createQuery("select r from Report r", Report.class)
                .getResultList();
    }

    /**
     * 특정 회원이 신고한 내역
     */
    public List<Report> findByReporter(Long reporterId) {
        return em.createQuery(
                "select r from Report r where r.reporter.id = :reporterId", Report.class)
                .setParameter("reporterId", reporterId)
                .getResultList();
    }

    /**
     * 특정 회원이 신고당한 내역
     */
    public List<Report> findByReported(Long reportedId) {
        return em.createQuery(
                "select r from Report r where r.reported.id = :reportedId", Report.class)
                .setParameter("reportedId", reportedId)
                .getResultList();
    }

    /**
     * 같은 신고자가 같은 피신고자를 이미 신고했는지 확인
     */
    public boolean existsByReporterAndReported(Long reporterId, Long reportedId) {
        Long count = em.createQuery(
                "select count(r) from Report r where r.reporter.id = :reporterId and r.reported.id = :reportedId", Long.class)
                .setParameter("reporterId", reporterId)
                .setParameter("reportedId", reportedId)
                .getSingleResult();
        return count > 0;
    }
}
