package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.BookCondition;
import com.example.bookbuddyproject.domain.BookStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final EntityManager em;

    public void save(Book book) {
        em.persist(book);
    }

    public Book findOne(Long id) {
        return em.find(Book.class, id);
    }

    public List<Book> findAll() {
        return em.createQuery("select b from Book b order by b.createdAt desc", Book.class)
                .getResultList();
    }

    /**
     * 판매중인 도서만 조회
     */
    public List<Book> findAllOnSale() {
        return em.createQuery(
                "select b from Book b where b.status = :status order by b.createdAt desc", Book.class)
                .setParameter("status", BookStatus.ON_SALE)
                .getResultList();
    }

    /**
     * 키워드 검색 (도서명, 저자, 출판사)
     */
    public List<Book> searchByKeyword(String keyword) {
        return em.createQuery(
                "select b from Book b where b.status = :status " +
                "and (b.title like :keyword or b.author like :keyword or b.publisher like :keyword) " +
                "order by b.createdAt desc", Book.class)
                .setParameter("status", BookStatus.ON_SALE)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    /**
     * 복합 검색 (키워드 + 카테고리 + 가격범위 + 상태등급)
     */
    public List<Book> search(String keyword, String category, 
                             Integer minPrice, Integer maxPrice, 
                             BookCondition bookCondition) {
        StringBuilder jpql = new StringBuilder(
                "select b from Book b where b.status = :status");
        
        if (keyword != null && !keyword.isEmpty()) {
            jpql.append(" and (b.title like :keyword or b.author like :keyword or b.publisher like :keyword)");
        }
        if (category != null && !category.isEmpty()) {
            jpql.append(" and b.category = :category");
        }
        if (minPrice != null) {
            jpql.append(" and b.price >= :minPrice");
        }
        if (maxPrice != null) {
            jpql.append(" and b.price <= :maxPrice");
        }
        if (bookCondition != null) {
            jpql.append(" and b.bookCondition = :bookCondition");
        }
        jpql.append(" order by b.createdAt desc");

        TypedQuery<Book> query = em.createQuery(jpql.toString(), Book.class)
                .setParameter("status", BookStatus.ON_SALE);

        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (category != null && !category.isEmpty()) {
            query.setParameter("category", category);
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (bookCondition != null) {
            query.setParameter("bookCondition", bookCondition);
        }

        return query.getResultList();
    }

    /**
     * 판매자의 도서 목록 조회
     */
    public List<Book> findBySeller(Long sellerId) {
        return em.createQuery(
                "select b from Book b where b.seller.id = :sellerId order by b.createdAt desc", Book.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    /**
     * 카테고리별 도서 조회
     */
    public List<Book> findByCategory(String category) {
        return em.createQuery(
                "select b from Book b where b.status = :status and b.category = :category " +
                "order by b.createdAt desc", Book.class)
                .setParameter("status", BookStatus.ON_SALE)
                .setParameter("category", category)
                .getResultList();
    }

    /**
     * 도서 삭제
     */
    public void delete(Book book) {
        em.remove(book);
    }
}
