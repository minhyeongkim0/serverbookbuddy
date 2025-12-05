package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Transaction;
import com.example.bookbuddyproject.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ⭐ 추가

@Controller
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 거래 신청
     * Use Case #3.2
     */
    @PostMapping("/request")
    public String requestTransaction(@RequestParam Long bookId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.requestTransaction(loginMember.getId(), bookId);
        } catch (IllegalStateException e) {
            // 여기는 책 상세 화면에서 에러를 보여주려면 flash attribute로 바꿔도 됨
            model.addAttribute("error", e.getMessage());
            return "redirect:/books/" + bookId;
        }

        return "redirect:/transactions";
    }

    /**
     * 거래 상황 조회 (내 거래 목록)
     * Use Case #3.3
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "buy") String tab,
                       HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Transaction> transactions;
        if ("sell".equals(tab)) {
            transactions = transactionService.findMySales(loginMember.getId());
        } else {
            transactions = transactionService.findMyPurchases(loginMember.getId());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("tab", tab);

        return "transactions/transactionList";
    }

    /**
     * 거래 상세 조회
     */
    @GetMapping("/{transactionId}")
    public String detail(@PathVariable Long transactionId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        Transaction transaction = transactionService.findOne(transactionId);
        if (transaction == null) {
            return "redirect:/transactions";
        }

        // 거래 당사자만 조회 가능
        if (!transaction.isBuyer(loginMember.getId()) && !transaction.isSeller(loginMember.getId())) {
            return "redirect:/transactions";
        }

        model.addAttribute("transaction", transaction);
        model.addAttribute("isBuyer", transaction.isBuyer(loginMember.getId()));
        model.addAttribute("isSeller", transaction.isSeller(loginMember.getId()));

        return "transactions/transactionDetail";
    }

    /**
     * 거래 수락 (판매자)
     */
    @PostMapping("/{transactionId}/accept")
    public String accept(@PathVariable Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.acceptTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
            // 에러 처리 필요하면 flash attribute 사용 가능
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 거래 거절 (판매자)
     */
    @PostMapping("/{transactionId}/reject")
    public String reject(@PathVariable Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.rejectTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
            // 에러 처리
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 결제하기 (구매자)
     * Use Case #3.4
     */
    @PostMapping("/{transactionId}/pay")
    public String pay(@PathVariable Long transactionId,
                      @RequestParam String depositorName,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) { // ⭐ 변경: RedirectAttributes 추가
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.payTransaction(transactionId, loginMember.getId(), depositorName);
        } catch (IllegalStateException e) {
            // ⭐ 포인트 부족 등 예외 메시지를 flash attribute로 담음
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/" + transactionId;
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 거래 완료 (판매자)
     * Use Case #3.5
     */
    @PostMapping("/{transactionId}/complete")
    public String complete(@PathVariable Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.completeTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
            // 에러 처리
        }

        return "redirect:/transactions/" + transactionId;
    }

    /**
     * 거래 취소
     */
    @PostMapping("/{transactionId}/cancel")
    public String cancel(@PathVariable Long transactionId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            transactionService.cancelTransaction(transactionId, loginMember.getId());
        } catch (IllegalStateException e) {
            // 에러 처리
        }

        return "redirect:/transactions";
    }
}
