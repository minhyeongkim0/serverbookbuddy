package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Wishlist;
import com.example.bookbuddyproject.repository.BookRepository;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    /**
     * 찜하기 토글 (추가/제거)
     * Use Case #3.1
     * @return true: 찜 추가됨, false: 찜 제거됨
     */
    @Transactional
    public boolean toggleWishlist(Long memberId, Long bookId) {
        // 회원 조회
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다.");
        }

        // 도서 조회
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }

        // 본인 도서 찜 불가
        if (book.getSeller().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 등록한 도서는 찜할 수 없습니다.");
        }

        // 이미 찜한 도서인지 확인
        Optional<Wishlist> existingWishlist = wishlistRepository.findByMemberAndBook(memberId, bookId);

        if (existingWishlist.isPresent()) {
            // 이미 찜한 도서 → 찜 제거
            wishlistRepository.delete(existingWishlist.get());
            return false;
        } else {
            // 찜하지 않은 도서 → 찜 추가
            Wishlist wishlist = Wishlist.createWishlist(member, book);
            wishlistRepository.save(wishlist);
            return true;
        }
    }

    /**
     * 내 찜 목록 조회
     */
    public List<Wishlist> findMyWishlist(Long memberId) {
        return wishlistRepository.findByMember(memberId);
    }

    /**
     * 특정 도서 찜 여부 확인
     */
    public boolean isWished(Long memberId, Long bookId) {
        return wishlistRepository.findByMemberAndBook(memberId, bookId).isPresent();
    }

    /**
     * 특정 도서의 찜 개수 조회
     */
    public long getWishCount(Long bookId) {
        return wishlistRepository.countByBook(bookId);
    }
}
