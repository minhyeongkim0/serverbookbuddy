package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 신고 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;  // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id")
    private Member reported;  // 피신고자

    @Enumerated(EnumType.STRING)
    private ReportReason reason;  // 신고 사유

    private LocalDateTime createdAt;

    // === 생성 메서드 === //
    public static Report createReport(Member reporter, Member reported, ReportReason reason) {
        Report report = new Report();
        report.reporter = reporter;
        report.reported = reported;
        report.reason = reason;
        report.createdAt = LocalDateTime.now();
        return report;
    }
}
