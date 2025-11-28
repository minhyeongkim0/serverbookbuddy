package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Transaction;
import com.example.bookbuddyproject.domain.TransactionStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final EntityManager em;

    public void save(Transaction transaction) {
        em.persist(transaction);
    }

    public Transaction findOne(Long id) {
        return em.find(Transaction.class, id);
    }

    public List<Transaction> findAll() {
        return em.createQuery("select t from Transaction t order by t.requestedAt desc", Transaction.class)
                .getResultList();
    }

    /**
     * 구매자의 거래 목록 조회
     */
    public List<Transaction> findByBuyer(Long buyerId) {
        return em.createQuery(
                "select t from Transaction t where t.buyer.id = :buyerId order by t.requestedAt desc", Transaction.class)
                .setParameter("buyerId", buyerId)
                .getResultList();
    }

    /**
     * 판매자의 거래 목록 조회
     */
    public List<Transaction> findBySeller(Long sellerId) {
        return em.createQuery(
                "select t from Transaction t where t.seller.id = :sellerId order by t.requestedAt desc", Transaction.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    /**
     * 특정 도서의 거래 목록 조회
     */
    public List<Transaction> findByBook(Long bookId) {
        return em.createQuery(
                "select t from Transaction t where t.book.id = :bookId order by t.requestedAt desc", Transaction.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    /**
     * 특정 회원의 모든 거래 조회 (구매자 또는 판매자)
     */
    public List<Transaction> findByMember(Long memberId) {
        return em.createQuery(
                "select t from Transaction t where t.buyer.id = :memberId or t.seller.id = :memberId " +
                "order by t.requestedAt desc", Transaction.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    /**
     * 특정 상태의 거래 조회
     */
    public List<Transaction> findByStatus(TransactionStatus status) {
        return em.createQuery(
                "select t from Transaction t where t.status = :status order by t.requestedAt desc", Transaction.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * 구매자가 특정 도서에 이미 거래 신청했는지 확인
     */
    public Optional<Transaction> findByBuyerAndBook(Long buyerId, Long bookId) {
        List<Transaction> result = em.createQuery(
                "select t from Transaction t where t.buyer.id = :buyerId and t.book.id = :bookId " +
                "and t.status not in (:rejected, :cancelled)", Transaction.class)
                .setParameter("buyerId", buyerId)
                .setParameter("bookId", bookId)
                .setParameter("rejected", TransactionStatus.REJECTED)
                .setParameter("cancelled", TransactionStatus.CANCELLED)
                .getResultList();
        return result.stream().findFirst();
    }

    /**
     * 거래 삭제
     */
    public void delete(Transaction transaction) {
        em.remove(transaction);
    }
}
