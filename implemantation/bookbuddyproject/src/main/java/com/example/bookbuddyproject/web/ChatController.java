package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.repository.MessageRepository;
import com.example.bookbuddyproject.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MessageRepository messageRepository;

    // 1. 메시지 전송
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto messageDto) {
        try {
            ChatMessageDto savedMessage = chatService.saveMessage(messageDto);
            messagingTemplate.convertAndSend("/sub/chat/room/" + savedMessage.getRoomId(), savedMessage);

            if (messageDto.getReceiverId() != null) {
                messagingTemplate.convertAndSend("/sub/member/" + messageDto.getReceiverId(), savedMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. 방 생성
    @PostMapping("/api/chat/room")
    public Long createRoom(@RequestBody RoomRequestDto request) {
        return chatService.createOrGetChatRoom(request.getSenderId(), request.getReceiverId(), request.getBookId());
    }

    // 3. 목록 조회
    @GetMapping("/api/chat/rooms")
    public List<ChatRoomDto> getMyChatRooms(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return new ArrayList<>();
        
        return chatService.getMyChatRooms(loginMember.getId());
    }

    // 4. 기록 조회
    @GetMapping("/api/chat/history/{roomId}")
    public List<ChatMessageDto> getChatHistory(@PathVariable String roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(m -> new ChatMessageDto(m.getRoomId(), m.getSender().getId(), m.getSender().getLoginId(), m.getContent(), m.getSentAt().toString()))
                .collect(Collectors.toList());
    }

    // --- DTO ---
    @Data
    public static class RoomRequestDto {
        private Long senderId;
        private Long receiverId;
        private Long bookId;
    }

    @Data
    public static class ChatMessageDto {
        private String roomId;
        private Long senderId;
        private String senderName;
        private Long receiverId;
        private String content;
        private String sentAt;
        public ChatMessageDto() {}
        public ChatMessageDto(String roomId, Long senderId, String senderName, String content, String sentAt) {
            this.roomId = roomId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.content = content;
            this.sentAt = sentAt;
        }
    }

    @Data
    public static class ChatRoomDto {
        private String roomId;
        private String roomName;
        private String lastMessage;
        private String status;
        private Long partnerId;
        public ChatRoomDto(String roomId, String roomName, String lastMessage, String status, Long partnerId) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.lastMessage = lastMessage;
            this.status = status;
            this.partnerId = partnerId;
        }
    }
}