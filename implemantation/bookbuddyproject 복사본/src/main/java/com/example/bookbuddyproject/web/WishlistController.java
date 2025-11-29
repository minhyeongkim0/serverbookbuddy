package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Wishlist;
import com.example.bookbuddyproject.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 찜하기 토글
     * Use Case #3.1
     */
    @PostMapping("/toggle")
    public String toggle(@RequestParam Long bookId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            wishlistService.toggleWishlist(loginMember.getId(), bookId);
        } catch (IllegalStateException e) {
            // 본인 도서 찜 시도 등 에러 처리
        }

        return "redirect:/books/" + bookId;
    }

    /**
     * 내 찜 목록 조회
     */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Wishlist> wishlists = wishlistService.findMyWishlist(loginMember.getId());
        model.addAttribute("wishlists", wishlists);

        return "wishlist/wishlistList";
    }
}
