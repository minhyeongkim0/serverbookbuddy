package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * 도서 이미지 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private String savedFilename;  // 저장된 파일명 (UUID)
    
    private String originalFilename;  // 원본 파일명

    private int imageOrder;  // 이미지 순서 (1~4)

    // === 생성 메서드 === //
    public static BookImage createBookImage(Book book, String savedFilename, 
                                             String originalFilename, int imageOrder) {
        BookImage bookImage = new BookImage();
        bookImage.book = book;
        bookImage.savedFilename = savedFilename;
        bookImage.originalFilename = originalFilename;
        bookImage.imageOrder = imageOrder;
        return bookImage;
    }
}
