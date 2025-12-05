package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.ReportReason;
import com.example.bookbuddyproject.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고하기 폼
     */
    @GetMapping("/report")
    public String reportForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        model.addAttribute("reasons", ReportReason.values());
        return "report/reportForm";
    }

    /**
     * 신고 처리
     */
    @PostMapping("/report")
    public String report(@RequestParam String reportedLoginId,
                         @RequestParam ReportReason reason,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            reportService.report(loginMember.getId(), reportedLoginId, reason);
            redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/report";
        }

        return "redirect:/";
    }
}
