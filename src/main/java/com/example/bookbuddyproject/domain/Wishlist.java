package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 찜하기 엔티티
 * Use Case #3.1 찜하기 기반
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 찜한 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;  // 찜한 도서

    private LocalDateTime createdAt;  // 찜한 시간

    // === 생성 메서드 === //
    public static Wishlist createWishlist(Member member, Book book) {
        Wishlist wishlist = new Wishlist();
        wishlist.member = member;
        wishlist.book = book;
        wishlist.createdAt = LocalDateTime.now();
        return wishlist;
    }
}
