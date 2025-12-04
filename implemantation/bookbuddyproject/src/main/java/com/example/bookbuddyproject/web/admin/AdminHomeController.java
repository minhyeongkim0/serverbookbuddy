package com.example.bookbuddyproject.web.admin;

import com.example.bookbuddyproject.domain.Admin;
import com.example.bookbuddyproject.service.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminHomeController {

    private final AdminService adminService;

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

        try {
            Admin admin = adminService.login(form.getAdminId(), form.getPassword());
            session.setAttribute("admin", admin.getAdminId());
        } catch (IllegalStateException e) {
            result.reject("loginFailed", e.getMessage());
            return "admin/loginForm";
        }

        return "redirect:/admin";
    }

    /**
     * 관리자 메인 페이지
     */
    @GetMapping
    public String home(HttpSession session, Model model) {
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

    /**
     * 관리자 추가 페이지
     */
    @GetMapping("/admins")
    public String adminList(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        List<Admin> admins = adminService.findAll();
        model.addAttribute("admins", admins);
        return "admin/adminList";
    }

    /**
     * 관리자 추가 처리
     */
    @PostMapping("/admins")
    public String addAdmin(@RequestParam String newAdminId,
                           @RequestParam String newPassword,
                           @RequestParam String masterPassword,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        try {
            adminService.addAdmin(newAdminId, newPassword, masterPassword);
            redirectAttributes.addFlashAttribute("message", "관리자가 추가되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/admins";
    }
}
