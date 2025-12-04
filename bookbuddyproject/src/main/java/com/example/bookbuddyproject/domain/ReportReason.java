package com.example.bookbuddyproject.domain;

/**
 * 신고 사유
 */
public enum ReportReason {
    POOR_CONDITION("상태불량"),
    PRICE_NEGOTIATION("가격흥정"),
    NO_RESPONSE("거래잠수");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
