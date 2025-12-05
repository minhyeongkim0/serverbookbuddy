package com.example.bookbuddyproject.web.admin;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminReportController {

    private final ReportService reportService;

    /**
     * 신고 목록 (3회 이상 신고된 회원)
     */
    @GetMapping("/reports")
    public String reportList(HttpSession session, Model model) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        List<Member> reportedMembers = reportService.findReportedMembers();
        model.addAttribute("members", reportedMembers);
        return "admin/reportList";
    }

    /**
     * 강퇴 처리
     */
    @PostMapping("/reports/{memberId}/ban")
    public String banMember(@PathVariable Long memberId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        try {
            reportService.banMember(memberId);
            redirectAttributes.addFlashAttribute("message", "강퇴 처리되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reports";
    }

    /**
     * 문제없음 처리 (신고 횟수 리셋)
     */
    @PostMapping("/reports/{memberId}/clear")
    public String clearReport(@PathVariable Long memberId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        try {
            reportService.clearReport(memberId);
            redirectAttributes.addFlashAttribute("message", "문제없음 처리되었습니다. 신고 횟수가 초기화되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reports";
    }
}
