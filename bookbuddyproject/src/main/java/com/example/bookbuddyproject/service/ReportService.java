package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Report;
import com.example.bookbuddyproject.domain.ReportReason;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    /**
     * 신고하기
     */
    @Transactional
    public Long report(Long reporterId, String reportedLoginId, ReportReason reason) {
        // 신고자 조회
        Member reporter = memberRepository.findOne(reporterId);
        if (reporter == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // 피신고자 조회
        Member reported = memberRepository.findByLoginId(reportedLoginId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 아이디입니다."));

        // 자기 자신 신고 방지
        if (reporter.getId().equals(reported.getId())) {
            throw new IllegalStateException("자기 자신을 신고할 수 없습니다.");
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndReported(reporterId, reported.getId())) {
            throw new IllegalStateException("이미 신고한 사용자입니다.");
        }

        // 신고 생성 및 저장
        Report report = Report.createReport(reporter, reported, reason);
        reportRepository.save(report);

        // 피신고자 신고 횟수 증가
        reported.increaseReportCount();

        return report.getId();
    }

    /**
     * 신고 3회 이상인 회원 목록 (관리자용)
     */
    public List<Member> findReportedMembers() {
        return memberRepository.findReportedMembers();
    }

    /**
     * 강퇴 처리
     */
    @Transactional
    public void banMember(Long memberId) {
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalStateException("존재하지 않는 회원입니다.");
        }
        member.ban();
    }

    /**
     * 문제없음 처리 (신고 횟수 리셋)
     */
    @Transactional
    public void clearReport(Long memberId) {
        Member member = memberRepository.findOne(memberId);
        if (member == null) {
            throw new IllegalStateException("존재하지 않는 회원입니다.");
        }
        member.resetReportCount();
    }
}
