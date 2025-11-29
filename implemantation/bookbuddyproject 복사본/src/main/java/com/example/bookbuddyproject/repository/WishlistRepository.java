package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Wishlist;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WishlistRepository {

    private final EntityManager em;

    public void save(Wishlist wishlist) {
        em.persist(wishlist);
    }

    public Wishlist findOne(Long id) {
        return em.find(Wishlist.class, id);
    }

    /**
     * 회원의 찜 목록 조회
     */
    public List<Wishlist> findByMember(Long memberId) {
        return em.createQuery(
                "select w from Wishlist w join fetch w.book where w.member.id = :memberId " +
                "order by w.createdAt desc", Wishlist.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    /**
     * 특정 도서의 찜 개수 조회
     */
    public long countByBook(Long bookId) {
        return em.createQuery(
                "select count(w) from Wishlist w where w.book.id = :bookId", Long.class)
                .setParameter("bookId", bookId)
                .getSingleResult();
    }

    /**
     * 회원이 특정 도서를 찜했는지 확인
     */
    public Optional<Wishlist> findByMemberAndBook(Long memberId, Long bookId) {
        List<Wishlist> result = em.createQuery(
                "select w from Wishlist w where w.member.id = :memberId and w.book.id = :bookId", Wishlist.class)
                .setParameter("memberId", memberId)
                .setParameter("bookId", bookId)
                .getResultList();
        return result.stream().findFirst();
    }

    /**
     * 찜 삭제
     */
    public void delete(Wishlist wishlist) {
        em.remove(wishlist);
    }
}
