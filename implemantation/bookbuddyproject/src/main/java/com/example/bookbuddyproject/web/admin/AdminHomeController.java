package com.example.bookbuddyproject.web.admin;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    // 관리자 계정 (하드코딩 - 나중에 DB로 변경 가능)
    private static final String ADMIN_ID = "master";
    private static final String ADMIN_PASSWORD = "1234";

    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("adminLoginForm", new AdminLoginForm());
        return "admin/loginForm";
    }

    /**
     * 관리자 로그인 처리
     */
    @PostMapping("/login")
    public String login(@Valid AdminLoginForm form, BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return "admin/loginForm";
        }

        // 관리자 계정 확인
        if (!ADMIN_ID.equals(form.getAdminId()) || !ADMIN_PASSWORD.equals(form.getPassword())) {
            result.reject("loginFailed", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "admin/loginForm";
        }

        // 로그인 성공
        session.setAttribute("admin", form.getAdminId());
        return "redirect:/admin";
    }

    /**
     * 관리자 메인 페이지
     */
    @GetMapping
    public String home(HttpSession session, Model model) {
        // 로그인 체크
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }
        return "admin/home";
    }

    /**
     * 관리자 로그아웃
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("admin");
        return "redirect:/admin/login";
    }
}
