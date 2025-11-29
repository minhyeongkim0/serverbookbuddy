package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.service.EmailService;
import com.example.bookbuddyproject.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    /**
     * 회원가입 폼
     */
    @GetMapping("/members/new")
    public String createForm(Model model, HttpSession session) {
        // 회원가입 페이지 들어올 때마다 인증 초기화
        session.removeAttribute("verifiedEmail");
        model.addAttribute("memberForm", new MemberForm());
        model.addAttribute("verifiedEmail", null);
        return "members/createMemberForm";
    }

    /**
     * 회원가입 처리
     * Use Case #1.1
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result, HttpSession session, Model model) {
        // 세션에서 인증된 이메일 확인
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        model.addAttribute("verifiedEmail", verifiedEmail);

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        // 이메일 인증 확인 (세션 기반)
        if (verifiedEmail == null || !verifiedEmail.equals(form.getEmail())) {
            result.reject("joinFailed", "이메일 인증을 완료해주세요.");
            return "members/createMemberForm";
        }

        try {
            memberService.join(
                    form.getLoginId(),
                    form.getPassword(),
                    form.getEmail(),
                    form.getDepartment(),
                    form.getStudentId()
            );
            // 회원가입 성공 시 세션에서 인증 이메일 제거
            session.removeAttribute("verifiedEmail");
        } catch (IllegalStateException e) {
            result.reject("joinFailed", e.getMessage());
            return "members/createMemberForm";
        }

        return "redirect:/login";
    }

    /**
     * 로그인 폼
     */
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "members/loginForm";
    }

    /**
     * 로그인 처리
     * Use Case #1.2
     */
    @PostMapping("/login")
    public String login(@Valid LoginForm form, BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return "members/loginForm";
        }

        try {
            Member member = memberService.login(form.getLoginId(), form.getPassword());
            session.setAttribute("loginMember", member);
        } catch (IllegalStateException e) {
            result.reject("loginFailed", e.getMessage());
            return "members/loginForm";
        }

        return "redirect:/";
    }

    /**
     * 로그아웃
     * Use Case #1.3
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
