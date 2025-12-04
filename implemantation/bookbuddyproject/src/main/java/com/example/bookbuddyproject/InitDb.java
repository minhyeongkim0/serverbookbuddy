package com.example.bookbuddyproject;

import com.example.bookbuddyproject.domain.Admin;
import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.BookCondition;
import com.example.bookbuddyproject.domain.Member;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테스트용 초기 데이터
 */
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit() {
            // 마스터 관리자
            Admin master = Admin.createAdmin("master", "1234", true);
            em.persist(master);

            // 테스트 회원 1
            Member member1 = Member.createMember(
                    "test1", "test1234!", "test1@ynu.ac.kr",
                    "컴퓨터공학과", "20231001"
            );
            em.persist(member1);

            // 테스트 회원 2
            Member member2 = Member.createMember(
                    "test2", "test1234!", "test2@ynu.ac.kr",
                    "전자공학과", "20231002"
            );
            em.persist(member2);

            // 테스트 도서들
            Book book1 = Book.createBook(
                    member1, "자바의 정석", "남궁성", "도우출판",
                    15000, "컴퓨터공학과", BookCondition.GOOD,
                    "필기 약간 있음, 전체적으로 깨끗", null
            );
            em.persist(book1);

            Book book2 = Book.createBook(
                    member1, "스프링 부트와 AWS로 혼자 구현하는 웹 서비스", "이동욱", "프리렉",
                    12000, "컴퓨터공학과", BookCondition.BEST,
                    "새 책 수준, 필기 없음", null
            );
            em.persist(book2);

            Book book3 = Book.createBook(
                    member2, "회로이론", "김영호", "한빛아카데미",
                    18000, "전자공학과", BookCondition.FAIR,
                    "형광펜 밑줄 있음", null
            );
            em.persist(book3);

            Book book4 = Book.createBook(
                    member2, "데이터베이스 시스템", "이석호", "정익사",
                    20000, "컴퓨터공학과", BookCondition.GOOD,
                    "연필 필기 약간", null
            );
            em.persist(book4);
        }
    }
}
