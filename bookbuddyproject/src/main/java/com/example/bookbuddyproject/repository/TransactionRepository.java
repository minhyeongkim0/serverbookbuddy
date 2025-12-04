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
        try {
            return em.createQuery(
                    "select t from Transaction t " +
                    "join fetch t.book b " +
                    "join fetch t.seller s " +
                    "join fetch t.buyer bu " +
                    "where t.id = :id", Transaction.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Transaction> findAll() {
        return em.createQuery("select t from Transaction t order by t.requestedAt desc", Transaction.class)
                .getResultList();
    }

    public List<Transaction> findByBuyer(Long buyerId) {
        return em.createQuery(
                "select t from Transaction t " +
                "join fetch t.book " +
                "join fetch t.seller " +
                "where t.buyer.id = :buyerId order by t.requestedAt desc", Transaction.class)
                .setParameter("buyerId", buyerId)
                .getResultList();
    }

    public List<Transaction> findBySeller(Long sellerId) {
        return em.createQuery(
                "select t from Transaction t " +
                "join fetch t.book " +
                "join fetch t.buyer " +
                "where t.seller.id = :sellerId order by t.requestedAt desc", Transaction.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    public List<Transaction> findByBook(Long bookId) {
        return em.createQuery(
                "select t from Transaction t where t.book.id = :bookId order by t.requestedAt desc", Transaction.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    public List<Transaction> findByMember(Long memberId) {
        return em.createQuery(
                "select t from Transaction t " +
                "join fetch t.book " +
                "join fetch t.seller " +
                "join fetch t.buyer " +
                "where t.buyer.id = :memberId or t.seller.id = :memberId " +
                "order by t.requestedAt desc", Transaction.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    public List<Transaction> findByStatus(TransactionStatus status) {
        return em.createQuery(
                "select t from Transaction t where t.status = :status order by t.requestedAt desc", Transaction.class)
                .setParameter("status", status)
                .getResultList();
    }

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

    public void delete(Transaction transaction) {
        em.remove(transaction);
    }
}