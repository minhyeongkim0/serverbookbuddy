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
        try {
            return em.createQuery(
                    "select b from Book b join fetch b.seller where b.id = :id", Book.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Book> findAll() {
        return em.createQuery("select b from Book b join fetch b.seller order by b.createdAt desc", Book.class)
                .getResultList();
    }

    public List<Book> findAllOnSale() {
        return em.createQuery(
                "select b from Book b join fetch b.seller where b.status = :status order by b.createdAt desc", Book.class)
                .setParameter("status", BookStatus.ON_SALE)
                .getResultList();
    }

    public List<Book> searchByKeyword(String keyword) {
        return em.createQuery(
                "select b from Book b join fetch b.seller where b.status = :status " +
                "and (b.title like :keyword or b.author like :keyword or b.publisher like :keyword) " +
                "order by b.createdAt desc", Book.class)
                .setParameter("status", BookStatus.ON_SALE)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    public List<Book> search(String keyword, String category, 
                             Integer minPrice, Integer maxPrice, 
                             BookCondition bookCondition) {
        StringBuilder jpql = new StringBuilder(
                "select b from Book b join fetch b.seller where b.status = :status");
        
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

    public List<Book> findBySeller(Long sellerId) {
        return em.createQuery(
                "select b from Book b where b.seller.id = :sellerId order by b.createdAt desc", Book.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    public List<Book> findByCategory(String category) {
        return em.createQuery(
                "select b from Book b join fetch b.seller where b.status = :status and b.category = :category " +
                "order by b.createdAt desc", Book.class)
                .setParameter("status", BookStatus.ON_SALE)
                .setParameter("category", category)
                .getResultList();
    }

    public void delete(Book book) {
        em.remove(book);
    }
}