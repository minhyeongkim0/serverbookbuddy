package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * 거래 엔티티
 * Use Case #3.2 거래 신청, #3.3 거래 상황 조회, #3.4 결제하기, #3.5 거래 완료 기반
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;  // 거래 도서

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Member buyer;  // 구매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Member seller;  // 판매자

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;  // 거래 상태

    private int price;  // 거래 금액

    private String depositorName;  // 입금자명 (결제 시)

    private LocalDateTime requestedAt;  // 신청일

    private LocalDateTime completedAt;  // 완료일

    // === 생성 메서드 === //
    public static Transaction createTransaction(Book book, Member buyer) {
        Transaction transaction = new Transaction();
        transaction.book = book;
        transaction.buyer = buyer;
        transaction.seller = book.getSeller();
        transaction.price = book.getPrice();
        transaction.status = TransactionStatus.REQUESTED;
        transaction.requestedAt = LocalDateTime.now();
        return transaction;
    }

    // === 비즈니스 로직 === //

    /**
     * 거래 수락 (판매자)
     */
    public void accept() {
        if (this.status != TransactionStatus.REQUESTED) {
            throw new IllegalStateException("신청 대기 상태의 거래만 수락할 수 있습니다.");
        }
        this.status = TransactionStatus.ACCEPTED;
        this.book.reserve();  // 도서 예약 처리
    }

    /**
     * 거래 거절 (판매자)
     */
    public void reject() {
        if (this.status != TransactionStatus.REQUESTED) {
            throw new IllegalStateException("신청 대기 상태의 거래만 거절할 수 있습니다.");
        }
        this.status = TransactionStatus.REJECTED;
    }

    /**
     * 결제 완료 (구매자)
     */
    public void pay(String depositorName) {
        if (this.status != TransactionStatus.ACCEPTED) {
            throw new IllegalStateException("거래 진행중 상태에서만 결제할 수 있습니다.");
        }
        this.depositorName = depositorName;
        this.status = TransactionStatus.PAID;
    }

    /**
     * 거래 완료 (판매자)
     */
    public void complete() {
        if (this.status != TransactionStatus.ACCEPTED && this.status != TransactionStatus.PAID) {
            throw new IllegalStateException("거래 진행중 또는 결제완료 상태에서만 완료할 수 있습니다.");
        }
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.book.soldOut();  // 도서 판매완료 처리
    }

    /**
     * 거래 취소 (구매자 또는 판매자)
     */
    public void cancel() {
        if (this.status == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("완료된 거래는 취소할 수 없습니다.");
        }
        // 예약된 도서 다시 판매중으로
        if (this.status == TransactionStatus.ACCEPTED || this.status == TransactionStatus.PAID) {
            this.book.cancelReservation();
        }
        this.status = TransactionStatus.CANCELLED;
    }

    /**
     * 구매자 본인 확인
     */
    public boolean isBuyer(Long memberId) {
        return this.buyer.getId().equals(memberId);
    }

    /**
     * 판매자 본인 확인
     */
    public boolean isSeller(Long memberId) {
        return this.seller.getId().equals(memberId);
    }
}
