package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.BookImage;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookImageRepository {

    private final EntityManager em;

    public void save(BookImage bookImage) {
        em.persist(bookImage);
    }

    public List<BookImage> findByBook(Long bookId) {
        return em.createQuery(
                "select bi from BookImage bi where bi.book.id = :bookId order by bi.imageOrder", 
                BookImage.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    public void deleteByBook(Long bookId) {
        em.createQuery("delete from BookImage bi where bi.book.id = :bookId")
                .setParameter("bookId", bookId)
                .executeUpdate();
    }
}
