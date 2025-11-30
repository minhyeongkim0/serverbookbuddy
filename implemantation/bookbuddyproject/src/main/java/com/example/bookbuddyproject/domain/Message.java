package com.example.bookbuddyproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    private String roomId; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sentAt;

    public static Message createMessage(String roomId, Member sender, String content) {
        Message message = new Message();
        message.roomId = roomId;
        message.sender = sender;
        message.content = content;
        message.sentAt = LocalDateTime.now();
        return message;
    }
}