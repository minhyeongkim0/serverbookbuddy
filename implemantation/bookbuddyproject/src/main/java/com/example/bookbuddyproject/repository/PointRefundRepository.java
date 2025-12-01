package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.PointRefundRequest;
import com.example.bookbuddyproject.domain.PointRequestStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointRefundRepository {

    private final EntityManager em;

    public void save(PointRefundRequest request) {
        if (request.getId() == null) {
            em.persist(request);
        } else {
            em.merge(request);
        }
    }

    public PointRefundRequest findOne(Long id) {
        return em.find(PointRefundRequest.class, id);
    }

    public List<PointRefundRequest> findPendingAll() {
        return em.createQuery(
                        "select r from PointRefundRequest r " +
                                "where r.status = :status order by r.requestedAt desc",
                        PointRefundRequest.class)
                .setParameter("status", PointRequestStatus.PENDING)
                .getResultList();
    }

    public List<PointRefundRequest> findByMember(Long memberId) {
        return em.createQuery(
                        "select r from PointRefundRequest r " +
                                "where r.member.id = :memberId order by r.requestedAt desc",
                        PointRefundRequest.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }
}
