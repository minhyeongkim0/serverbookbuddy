package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.BookCondition;
import com.example.bookbuddyproject.domain.BookImage;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.repository.BookRepository;
import com.example.bookbuddyproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    /**
     * 도서 등록 (이미지 포함)
     * Use Case #2.1
     */
    @Transactional
    public Long registerBook(Long sellerId, String title, String author,
                              String publisher, int price, String category,
                              BookCondition bookCondition, String description,
                              List<MultipartFile> images) throws IOException {
        // 판매자 조회
        Member seller = memberRepository.findOne(sellerId);
        if (seller == null) {
            throw new IllegalArgumentException("판매자를 찾을 수 없습니다.");
        }

        // 이미지 검증 (1~4장)
        int imageCount = 0;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                imageCount++;
            }
        }
        if (imageCount < 1) {
            throw new IllegalStateException("이미지는 최소 1장 이상 업로드해야 합니다.");
        }
        if (imageCount > 4) {
            throw new IllegalStateException("이미지는 최대 4장까지 업로드 가능합니다.");
        }

        // 도서 생성
        Book book = Book.createBook(seller, title, author, publisher, price,
                category, bookCondition, description, null);

        // 이미지 저장
        int order = 1;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String[] fileInfo = fileService.saveFile(image);
                BookImage bookImage = BookImage.createBookImage(book, fileInfo[0], fileInfo[1], order++);
                book.addImage(bookImage);
            }
        }

        bookRepository.save(book);
        return book.getId();
    }

    /**
     * 도서 조회 (조회수 증가)
     * Use Case #2.3
     */
    @Transactional
    public Book viewBook(Long bookId) {
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }
        book.increaseViewCount();
        return book;
    }

    /**
     * 도서 검색 (키워드)
     * Use Case #2.2
     */
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchByKeyword(keyword);
    }

    /**
     * 도서 복합 검색 (키워드 + 필터)
     * Use Case #2.2
     */
    public List<Book> searchBooks(String keyword, String category,
                                   Integer minPrice, Integer maxPrice,
                                   BookCondition bookCondition) {
        return bookRepository.search(keyword, category, minPrice, maxPrice, bookCondition);
    }

    /**
     * 판매중인 전체 도서 목록
     */
    public List<Book> findBooksOnSale() {
        return bookRepository.findAllOnSale();
    }

    /**
     * 내 판매 도서 목록
     */
    public List<Book> findMyBooks(Long sellerId) {
        return bookRepository.findBySeller(sellerId);
    }

    /**
     * 카테고리별 도서 목록
     */
    public List<Book> findBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    /**
     * 도서 정보 수정 (이미지 포함)
     */
    @Transactional
    public void updateBook(Long bookId, Long sellerId, String title, String author,
                           String publisher, int price, String category,
                           BookCondition bookCondition, String description,
                           List<MultipartFile> newImages) throws IOException {
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }
        // 판매자 본인 확인
        if (!book.getSeller().getId().equals(sellerId)) {
            throw new IllegalStateException("본인이 등록한 도서만 수정할 수 있습니다.");
        }

        // 기본 정보 수정
        book.update(title, author, publisher, price, category, 
                    bookCondition, description, null);

        // 새 이미지가 있으면 교체
        if (newImages != null && !newImages.isEmpty()) {
            int imageCount = 0;
            for (MultipartFile image : newImages) {
                if (image != null && !image.isEmpty()) {
                    imageCount++;
                }
            }

            if (imageCount > 0) {
                if (imageCount > 4) {
                    throw new IllegalStateException("이미지는 최대 4장까지 업로드 가능합니다.");
                }

                // 기존 이미지 파일 삭제
                for (BookImage oldImage : book.getImages()) {
                    fileService.deleteFile(oldImage.getSavedFilename());
                }
                
                // 기존 이미지 엔티티 삭제
                book.clearImages();

                // 새 이미지 저장
                int order = 1;
                for (MultipartFile image : newImages) {
                    if (image != null && !image.isEmpty()) {
                        String[] fileInfo = fileService.saveFile(image);
                        BookImage bookImage = BookImage.createBookImage(book, fileInfo[0], fileInfo[1], order++);
                        book.addImage(bookImage);
                    }
                }
            }
        }
    }

    /**
     * 도서 삭제
     */
    @Transactional
    public void deleteBook(Long bookId, Long sellerId) {
        Book book = bookRepository.findOne(bookId);
        if (book == null) {
            throw new IllegalArgumentException("존재하지 않는 도서입니다.");
        }
        // 판매자 본인 확인
        if (!book.getSeller().getId().equals(sellerId)) {
            throw new IllegalStateException("본인이 등록한 도서만 삭제할 수 있습니다.");
        }
        // 판매중인 도서만 삭제 가능
        if (!book.isOnSale()) {
            throw new IllegalStateException("판매중인 도서만 삭제할 수 있습니다.");
        }

        // 이미지 파일 삭제
        for (BookImage image : book.getImages()) {
            fileService.deleteFile(image.getSavedFilename());
        }

        bookRepository.delete(book);
    }

    /**
     * 도서 단건 조회 (조회수 증가 없음)
     */
    public Book findOne(Long bookId) {
        return bookRepository.findOne(bookId);
    }
}
