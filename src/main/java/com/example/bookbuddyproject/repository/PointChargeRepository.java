package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.PointChargeRequest;
import com.example.bookbuddyproject.domain.PointRequestStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointChargeRepository {

    private final EntityManager em;

    public void save(PointChargeRequest request) {
        if (request.getId() == null) {
            em.persist(request);
        } else {
            em.merge(request);
        }
    }

    public PointChargeRequest findOne(Long id) {
        return em.find(PointChargeRequest.class, id);
    }

    public List<PointChargeRequest> findPendingAll() {
        return em.createQuery(
                        "select r from PointChargeRequest r " +
                                "where r.status = :status order by r.requestedAt desc",
                        PointChargeRequest.class)
                .setParameter("status", PointRequestStatus.PENDING)
                .getResultList();
    }

    public List<PointChargeRequest> findByMember(Long memberId) {
        return em.createQuery(
                        "select r from PointChargeRequest r " +
                                "where r.member.id = :memberId order by r.requestedAt desc",
                        PointChargeRequest.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }
}
