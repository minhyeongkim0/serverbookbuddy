package com.example.bookbuddyproject.repository;

import com.example.bookbuddyproject.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    @Query("select c from ChatRoom c where c.book.id = :bookId and " +
           "((c.member1.id = :m1 and c.member2.id = :m2) or (c.member1.id = :m2 and c.member2.id = :m1))")
    Optional<ChatRoom> findExistingRoom(@Param("m1") Long memberId1, @Param("m2") Long memberId2, @Param("bookId") Long bookId);

    @Query("select c from ChatRoom c " +
           "join fetch c.book " +
           "join fetch c.member1 " +
           "join fetch c.member2 " +
           "where c.member1.id = :memberId or c.member2.id = :memberId " +
           "order by c.createdAt desc")
    List<ChatRoom> findMyChatRooms(@Param("memberId") Long memberId);
}