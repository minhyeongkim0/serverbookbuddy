package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
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

    /**
     * 회원가입 폼
     */
    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * 회원가입 처리
     * Use Case #1.1
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        if (result.hasErrors()) {
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
