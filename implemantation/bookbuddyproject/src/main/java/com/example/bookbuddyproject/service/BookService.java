package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.BookCondition;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.repository.BookRepository;
import com.example.bookbuddyproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    /**
     * 도서 등록
     * Use Case #2.1
     */
    @Transactional
    public Long registerBook(Long sellerId, String title, String author,
                              String publisher, int price, String category,
                              BookCondition bookCondition, String description,
                              String imageUrl) {
        // 판매자 조회
        Member seller = memberRepository.findOne(sellerId);
        if (seller == null) {
            throw new IllegalArgumentException("판매자를 찾을 수 없습니다.");
        }

        // 도서 생성
        Book book = Book.createBook(seller, title, author, publisher, price,
                category, bookCondition, description, imageUrl);

        bookRepository.save(book);
        return book.getId();
    }

    /**
     * 도서 조회 (조회수 증가)
     * Use Case #2.3
     */
    @Transactional
    public Book viewBook(Long bookId) {
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }
        book.increaseViewCount();
        return book;
    }

    /**
     * 도서 검색 (키워드)
     * Use Case #2.2
     */
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchByKeyword(keyword);
    }

    /**
     * 도서 복합 검색 (키워드 + 필터)
     * Use Case #2.2
     */
    public List<Book> searchBooks(String keyword, String category,
                                   Integer minPrice, Integer maxPrice,
                                   BookCondition bookCondition) {
        return bookRepository.search(keyword, category, minPrice, maxPrice, bookCondition);
    }

    /**
     * 판매중인 전체 도서 목록
     */
    public List<Book> findBooksOnSale() {
        return bookRepository.findAllOnSale();
    }

    /**
     * 내 판매 도서 목록
     */
    public List<Book> findMyBooks(Long sellerId) {
        return bookRepository.findBySeller(sellerId);
    }

    /**
     * 카테고리별 도서 목록
     */
    public List<Book> findBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    /**
     * 도서 정보 수정
     */
    @Transactional
    public void updateBook(Long bookId, Long sellerId, String title, String author,
                           String publisher, int price, String category,
                           BookCondition bookCondition, String description,
                           String imageUrl) {
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }
        // 판매자 본인 확인
        if (!book.getSeller().getId().equals(sellerId)) {
            throw new IllegalStateException("본인이 등록한 도서만 수정할 수 있습니다.");
        }

        book.update(title, author, publisher, price, category, 
                    bookCondition, description, imageUrl);
    }

    /**
     * 도서 삭제
     */
    @Transactional
    public void deleteBook(Long bookId, Long sellerId) {
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }
        // 판매자 본인 확인
        if (!book.getSeller().getId().equals(sellerId)) {
            throw new IllegalStateException("본인이 등록한 도서만 삭제할 수 있습니다.");
        }
        // 판매중인 도서만 삭제 가능
        if (!book.isOnSale()) {
            throw new IllegalStateException("판매중인 도서만 삭제할 수 있습니다.");
        }

        bookRepository.delete(book);
    }

    /**
     * 도서 단건 조회 (조회수 증가 없음)
     */
    public Book findOne(Long bookId) {
        return bookRepository.findOne(bookId);
    }
}
