package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.PointChargeRequest;
import com.example.bookbuddyproject.domain.PointRefundRequest;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.repository.PointChargeRepository;
import com.example.bookbuddyproject.repository.PointRefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointChargeRepository pointChargeRepository;
    private final PointRefundRepository pointRefundRepository;

    // ==== 관리자 화면에서 사용할 메서드들 ==== //

    // 대기중 환급 리스트
    public List<PointRefundRequest> findPendingRefundRequests() {
        return pointRefundRepository.findPendingAll();
    }

    // 대기중 충전 리스트
    public List<PointChargeRequest> findPendingChargeRequests() {
        return pointChargeRepository.findPendingAll();
    }

    // 환급 수락(포인트 차감 + 상태 변경)
    @Transactional
    public void approveRefund(Long requestId) {
        PointRefundRequest request = pointRefundRepository.findOne(requestId);
        if (request == null || !request.isPending()) {
            throw new IllegalStateException("유효하지 않은 환급 요청입니다.");
        }

        Member member = request.getMember();
        member.usePoints(request.getAmount()); // Member 엔티티에 만들어둔 메서드

        request.approve(); // 상태 PENDING -> APPROVED
    }

    // 충전 지급(포인트 지급 + 상태 변경)
    @Transactional
    public void approveCharge(Long requestId) {
        PointChargeRequest request = pointChargeRepository.findOne(requestId);
        if (request == null || !request.isPending()) {
            throw new IllegalStateException("유효하지 않은 충전 요청입니다.");
        }

        Member member = request.getMember();
        member.addPoints(request.getAmount()); // 포인트 추가

        request.approve();
    }

    // ==== 유저 쪽에서 충전/환급 신청하는 메서드 ==== //

    // 충전 신청
    @Transactional
    public Long requestCharge(Long memberId, int amount) {
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 포인트는 0보다 커야 합니다.");
        }

        PointChargeRequest request = PointChargeRequest.create(member, amount);
        pointChargeRepository.save(request);

        return request.getId();
    }

    // 환급 신청
    @Transactional
    public Long requestRefund(Long memberId, int amount, String bankName, String accountNumber) {
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("환급 포인트는 0보다 커야 합니다.");
        }
        if (member.getPointBalance() < amount) {
            throw new IllegalStateException("보유 포인트보다 큰 금액은 환급 신청할 수 없습니다.");
        }

        PointRefundRequest request =
                PointRefundRequest.create(member, amount, bankName, accountNumber);
        pointRefundRepository.save(request);

        return request.getId();
    }
}

