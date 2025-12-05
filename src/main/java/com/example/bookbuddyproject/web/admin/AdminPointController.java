package com.example.bookbuddyproject.web.admin;

import com.example.bookbuddyproject.domain.PointChargeRequest;
import com.example.bookbuddyproject.domain.PointRefundRequest;
import com.example.bookbuddyproject.service.PointService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPointController {

    private final PointService pointService;

    // 이미 AdminHomeController 에서도 쓰는 개념이지만,
    // 여기서는 간단히 세션에 "admin"이 있는지만 확인하도록 함.
    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("admin") != null;
    }

    /**
     * 포인트 환급 신청 목록 페이지
     * URL: GET /admin/point-refund
     */
    @GetMapping("/point-refund")
    public String refundList(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        // 대기중(PENDING)인 환급 신청 리스트 가져오기
        List<PointRefundRequest> refundRequests = pointService.findPendingRefundRequests();

        // 뷰에서 사용할 이름으로 Model에 담음
        model.addAttribute("refundRequests", refundRequests);

        // templates/admin/pointRefundList.html 렌더링
        return "admin/pointRefundList";
    }

    /**
     * 포인트 환급 수락 버튼 처리
     * URL: POST /admin/point-refund/{id}/approve
     */
    @PostMapping("/point-refund/{id}/approve")
    public String approveRefund(@PathVariable("id") Long refundId, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        // 환급 승인 (포인트 차감 + 상태 변경)
        pointService.approveRefund(refundId);

        // 다시 환급 목록 페이지로 이동
        return "redirect:/admin/point-refund";
    }

    /**
     * 포인트 충전 신청 목록 페이지
     * URL: GET /admin/point-charge
     */
    @GetMapping("/point-charge")
    public String chargeList(HttpSession session, Model model) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        // 대기중(PENDING)인 충전 신청 리스트 가져오기
        List<PointChargeRequest> chargeRequests = pointService.findPendingChargeRequests();

        // 뷰에서 사용할 이름으로 Model에 담음
        model.addAttribute("chargeRequests", chargeRequests);

        // templates/admin/pointChargeList.html 렌더링
        return "admin/pointChargeList";
    }

    /**
     * 포인트 충전 지급 버튼 처리
     * URL: POST /admin/point-charge/{id}/approve
     */
    @PostMapping("/point-charge/{id}/approve")
    public String approveCharge(@PathVariable("id") Long chargeId, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        // 충전 승인 (포인트 지급 + 상태 변경)
        pointService.approveCharge(chargeId);

        // 다시 충전 목록 페이지로 이동
        return "redirect:/admin/point-charge";
    }
}
