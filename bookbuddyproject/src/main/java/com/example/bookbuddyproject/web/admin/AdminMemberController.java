package com.example.bookbuddyproject.web.admin;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    // 관리자 세션 체크
    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("admin") != null;
    }

    /**
     * 회원 목록 조회 및 검색
     * URL: GET /admin/members
     */
    @GetMapping("/members")
    public String memberList(@RequestParam(name = "keyword", required = false) String keyword, HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        // 키워드가 있으면 검색, 없으면 전체 최신순 조회
        List<Member> members = memberService.findMembers(keyword);
        
        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);

        return "admin/memberList";
    }

    /**
     * 회원 강제 탈퇴 처리
     * URL: POST /admin/members/{id}/ban
     */
    @PostMapping("/members/{id}/ban")
    // @PathVariable(name = "id") 처럼 name을 명시해야 함
    public String banMember(@PathVariable(name = "id") Long memberId, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        try {
            memberService.banMember(memberId);
        } catch (IllegalStateException e) {
            // 에러 발생 시 처리 (필요시 flash attribute 사용)
        }

        return "redirect:/admin/members";
    }
}