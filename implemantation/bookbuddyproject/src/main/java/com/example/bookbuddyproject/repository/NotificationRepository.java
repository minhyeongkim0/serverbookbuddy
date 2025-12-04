package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Notification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private final EntityManager em;

    public void save(Notification notification) {
        em.persist(notification);
    }

    public Notification findOne(Long id) {
        return em.find(Notification.class, id);
    }

    /**
     * 특정 회원의 모든 알림 조회 (최신순)
     */
    public List<Notification> findByMember(Long memberId) {
        return em.createQuery(
                "select n from Notification n where n.member.id = :memberId order by n.createdAt desc", 
                Notification.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    /**
     * 특정 회원의 안 읽은 알림 개수
     */
    public Long countUnread(Long memberId) {
        return em.createQuery(
                "select count(n) from Notification n where n.member.id = :memberId and n.isRead = false", 
                Long.class)
                .setParameter("memberId", memberId)
                .getSingleResult();
    }

    /**
     * 특정 회원의 안 읽은 알림 목록
     */
    public List<Notification> findUnreadByMember(Long memberId) {
        return em.createQuery(
                "select n from Notification n where n.member.id = :memberId and n.isRead = false order by n.createdAt desc", 
                Notification.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }
}
