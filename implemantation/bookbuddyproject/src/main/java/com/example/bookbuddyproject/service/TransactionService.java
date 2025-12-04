package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Transaction;
import com.example.bookbuddyproject.domain.NotificationType;
import com.example.bookbuddyproject.domain.Notification;
import com.example.bookbuddyproject.repository.BookRepository;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.repository.TransactionRepository;
import com.example.bookbuddyproject.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 거래 신청
     * Use Case #3.2
     */
    @Transactional
    public Long requestTransaction(Long buyerId, Long bookId) {
        // 구매자 조회
        Member buyer = memberRepository.findOne(buyerId);
        if (buyer == null) {
            throw new IllegalArgumentException("구매자를 찾을 수 없습니다.");
        }

        // 도서 조회
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }

        // 본인 도서 구매 불가
        if (book.getSeller().getId().equals(buyerId)) {
            throw new IllegalStateException("본인이 등록한 도서는 구매할 수 없습니다.");
        }

        // 판매중인 도서만 거래 신청 가능
        if (!book.isOnSale()) {
            throw new IllegalStateException("판매중인 도서만 거래 신청할 수 있습니다.");
        }

        // 이미 거래 신청한 도서인지 확인
        if (transactionRepository.findByBuyerAndBook(buyerId, bookId).isPresent()) {
            throw new IllegalStateException("이미 거래 신청한 도서입니다.");
        }

        // 거래 생성
        Transaction transaction = Transaction.createTransaction(book, buyer);
        transactionRepository.save(transaction);

        // 판매자에게 알림 생성
        Member seller = book.getSeller();
        String message = buyer.getLoginId() + "님이 '" + book.getTitle() + "' 거래를 신청했습니다.";
        Notification notification = Notification.createNotification(seller, transaction, NotificationType.TRANSACTION_REQUEST, message);
        notificationRepository.save(notification);

        return transaction.getId();
    }

    /**
     * 거래 수락 (판매자)
     */
    @Transactional
    public void acceptTransaction(Long transactionId, Long sellerId) {
        Transaction transaction = findTransaction(transactionId);

        // 판매자 본인 확인
        if (!transaction.isSeller(sellerId)) {
            throw new IllegalStateException("판매자만 거래를 수락할 수 있습니다.");
        }

        transaction.accept();

        // 구매자에게 알림 생성
        Member buyer = transaction.getBuyer();
        String message = "'" + transaction.getBook().getTitle() + "' 거래가 수락되었습니다. 결제를 진행해주세요.";
        Notification notification = Notification.createNotification(buyer, transaction, NotificationType.TRANSACTION_ACCEPTED, message);
        notificationRepository.save(notification);
    }

    /**
     * 거래 거절 (판매자)
     */
    @Transactional
    public void rejectTransaction(Long transactionId, Long sellerId) {
        Transaction transaction = findTransaction(transactionId);

        // 판매자 본인 확인
        if (!transaction.isSeller(sellerId)) {
            throw new IllegalStateException("판매자만 거래를 거절할 수 있습니다.");
        }

        transaction.reject();

        // 구매자에게 알림 생성
        Member buyer = transaction.getBuyer();
        String message = "'" + transaction.getBook().getTitle() + "' 거래가 거절되었습니다.";
        Notification notification = Notification.createNotification(buyer, transaction, NotificationType.TRANSACTION_REJECTED, message);
        notificationRepository.save(notification);
    }

    /**
     * 결제하기 (구매자)
     * Use Case #3.4
     *  -> 포인트로 결제: 구매자 포인트 차감
     */
    @Transactional
    public void payTransaction(Long transactionId, Long buyerId, String depositorName) {
        Transaction transaction = findTransaction(transactionId);

        // 구매자 본인 확인
        if (!transaction.isBuyer(buyerId)) {
            throw new IllegalStateException("구매자만 결제할 수 있습니다.");
        }

        // ⭐ 포인트 차감 로직
        Member buyer = transaction.getBuyer();
        int price = transaction.getPrice();   // 거래 금액 = 책 가격(포인트)

        buyer.usePoints(price);              // Member 엔티티에 만든 메서드 (포인트 부족 시 예외)

        // 결제 상태 변경 + 입금자명 저장
        transaction.pay(depositorName);

        // 판매자에게 알림 생성
        Member seller = transaction.getSeller();
        String message = "'" + transaction.getBook().getTitle() + "' 결제가 완료되었습니다. 거래를 완료해주세요.";
        Notification notification = Notification.createNotification(seller, transaction, NotificationType.TRANSACTION_PAID, message);
        notificationRepository.save(notification);
    }

    /**
     * 거래 완료 (판매자)
     * Use Case #3.5
     *  -> 포인트로 정산: 판매자 포인트 지급
     */
    @Transactional
    public void completeTransaction(Long transactionId, Long sellerId) {
        Transaction transaction = findTransaction(transactionId);

        // 판매자 본인 확인
        if (!transaction.isSeller(sellerId)) {
            throw new IllegalStateException("판매자만 거래를 완료할 수 있습니다.");
        }

        // ⭐ 판매자 포인트 지급
        Member seller = transaction.getSeller();
        int price = transaction.getPrice();   // 거래 금액(포인트)

        seller.addPoints(price);             // Member 엔티티에 만든 메서드

        // 거래 상태 완료 처리 (도서 soldOut 포함)
        transaction.complete();

        // 구매자에게 알림 생성
        Member buyer = transaction.getBuyer();
        String message = "'" + transaction.getBook().getTitle() + "' 거래가 완료되었습니다.";
        Notification notification = Notification.createNotification(buyer, transaction, NotificationType.TRANSACTION_COMPLETED, message);
        notificationRepository.save(notification);
    }

    /**
     * 거래 취소
     */
    @Transactional
    public void cancelTransaction(Long transactionId, Long memberId) {
        Transaction transaction = findTransaction(transactionId);

        // 구매자 또는 판매자만 취소 가능
        if (!transaction.isBuyer(memberId) && !transaction.isSeller(memberId)) {
            throw new IllegalStateException("거래 당사자만 취소할 수 있습니다.");
        }

        transaction.cancel();
    }

    /**
     * 거래 단건 조회
     */
    public Transaction findOne(Long transactionId) {
        return transactionRepository.findOne(transactionId);
    }

    /**
     * 내 구매 목록 조회
     * Use Case #3.3
     */
    public List<Transaction> findMyPurchases(Long buyerId) {
        return transactionRepository.findByBuyer(buyerId);
    }

    /**
     * 내 판매 거래 목록 조회
     * Use Case #3.3
     */
    public List<Transaction> findMySales(Long sellerId) {
        return transactionRepository.findBySeller(sellerId);
    }

    /**
     * 내 전체 거래 목록 조회
     */
    public List<Transaction> findMyTransactions(Long memberId) {
        return transactionRepository.findByMember(memberId);
    }

    // === private 메서드 === //

    private Transaction findTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findOne(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("존재하지 않는 거래입니다.");
        }
        return transaction;
    }
}
