package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 도서 엔티티
 * Use Case #2.1 도서 등록, #2.2 도서 검색, #2.3 도서 조회 기반
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(nullable = false)
    private String title;  // 도서명

    @Column(nullable = false)
    private String author;  // 저자

    private String publisher;  // 출판사

    @Column(nullable = false)
    private int price;  // 판매 가격

    private String category;  // 카테고리 (학과)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookCondition bookCondition;  // 도서 상태 등급 (최상/상/중/하)

    @Column(length = 1000)
    private String description;  // 도서 설명 (선택)

    private String imageUrl;  // 도서 이미지 URL (선택)

    @Enumerated(EnumType.STRING)
    private BookStatus status;  // 판매 상태 (판매중/예약중/판매완료)

    private int viewCount;  // 조회수

    private LocalDateTime createdAt;  // 등록일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;  // 판매자

    // === 생성 메서드 === //
    public static Book createBook(Member seller, String title, String author,
                                   String publisher, int price, String category,
                                   BookCondition bookCondition, String description,
                                   String imageUrl) {
        Book book = new Book();
        book.seller = seller;
        book.title = title;
        book.author = author;
        book.publisher = publisher;
        book.price = price;
        book.category = category;
        book.bookCondition = bookCondition;
        book.description = description;
        book.imageUrl = imageUrl;
        book.status = BookStatus.ON_SALE;
        book.viewCount = 0;
        book.createdAt = LocalDateTime.now();
        return book;
    }

    // === 비즈니스 로직 === //

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 예약 처리
     */
    public void reserve() {
        if (this.status != BookStatus.ON_SALE) {
            throw new IllegalStateException("판매중인 도서만 예약할 수 있습니다.");
        }
        this.status = BookStatus.RESERVED;
    }

    /**
     * 판매 완료 처리
     */
    public void soldOut() {
        this.status = BookStatus.SOLD_OUT;
    }

    /**
     * 예약 취소 (다시 판매중으로)
     */
    public void cancelReservation() {
        if (this.status != BookStatus.RESERVED) {
            throw new IllegalStateException("예약중인 도서만 예약 취소할 수 있습니다.");
        }
        this.status = BookStatus.ON_SALE;
    }

    /**
     * 도서 정보 수정
     */
    public void update(String title, String author, String publisher,
                       int price, String category, BookCondition bookCondition,
                       String description, String imageUrl) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.category = category;
        this.bookCondition = bookCondition;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    /**
     * 판매중 여부 확인
     */
    public boolean isOnSale() {
        return this.status == BookStatus.ON_SALE;
    }
}
