package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.service.BookService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // 로그인한 회원 정보
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("member", loginMember);

        // 판매중인 도서 목록 (최신순)
        List<Book> books = bookService.findBooksOnSale();
        model.addAttribute("books", books);

        return "home";
    }
}
