package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.service.PointService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    private final PointService pointService;
    private final MemberRepository memberRepository;

    private static final String ADMIN_ACCOUNT_NUMBER = "123456789";

    // 세션의 loginMember 기준으로 DB에서 최신 Member 가져오기
    private Member getLoginMember(HttpSession session) {
        Member sessionMember = (Member) session.getAttribute("loginMember");
        if (sessionMember == null) {
            return null;
        }

        // DB에서 최신 상태 조회
        Member freshMember = memberRepository.findOne(sessionMember.getId());

        // 세션도 최신 객체로 덮어쓰기
        session.setAttribute("loginMember", freshMember);

        return freshMember;
    }

    // 포인트 메인 (충전 / 환급 버튼)
    @GetMapping
    public String pointHome(HttpSession session, Model model) {
        Member loginMember = getLoginMember(session); // ⭐ 변경
        if (loginMember == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", loginMember);
        return "points/pointHome";
    }

    // === 충전 === //
    @GetMapping("/charge")
    public String showChargeForm(HttpSession session, Model model) {
        Member loginMember = getLoginMember(session); // ⭐ 변경
        if (loginMember == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", loginMember);
        model.addAttribute("adminAccount", ADMIN_ACCOUNT_NUMBER);
        model.addAttribute("chargeForm", new ChargeForm());
        return "points/chargeForm";
    }

    @PostMapping("/charge")
    public String charge(@Valid @ModelAttribute("chargeForm") ChargeForm form,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        Member loginMember = getLoginMember(session); // ⭐ 변경
        if (loginMember == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", loginMember);
            model.addAttribute("adminAccount", ADMIN_ACCOUNT_NUMBER);
            return "points/chargeForm";
        }

        try {
            pointService.requestCharge(loginMember.getId(), form.getAmount());
        } catch (IllegalArgumentException | IllegalStateException e) {
            bindingResult.reject("chargeError", e.getMessage());
            model.addAttribute("member", loginMember);
            model.addAttribute("adminAccount", ADMIN_ACCOUNT_NUMBER);
            return "points/chargeForm";
        }

        return "redirect:/points";
    }

    // === 환급 === //
    @GetMapping("/refund")
    public String showRefundForm(HttpSession session, Model model) {
        Member loginMember = getLoginMember(session); // ⭐ 변경
        if (loginMember == null) {
            return "redirect:/login";
        }
        model.addAttribute("member", loginMember);
        model.addAttribute("refundForm", new RefundForm());
        return "points/refundForm";
    }

    @PostMapping("/refund")
    public String refund(@Valid @ModelAttribute("refundForm") RefundForm form,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        Member loginMember = getLoginMember(session); // ⭐ 변경
        if (loginMember == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", loginMember);
            return "points/refundForm";
        }

        try {
            pointService.requestRefund(
                    loginMember.getId(),
                    form.getAmount(),
                    form.getBankName(),
                    form.getAccountNumber()
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            bindingResult.reject("refundError", e.getMessage());
            model.addAttribute("member", loginMember);
            return "points/refundForm";
        }

        return "redirect:/points";
    }

    // === 폼 클래스 === //
    @Data
    public static class ChargeForm {
        @NotNull
        @Min(1)
        private Integer amount; // 포인트 = 금액 1:1
    }

    @Data
    public static class RefundForm {
        @NotNull
        @Min(1)
        private Integer amount;

        @NotBlank
        private String bankName;

        @NotBlank
        private String accountNumber;
    }
}
