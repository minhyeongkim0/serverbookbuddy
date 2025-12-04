package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.ChatRoom;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.domain.Message;
import com.example.bookbuddyproject.repository.BookRepository;
import com.example.bookbuddyproject.repository.ChatRoomRepository;
import com.example.bookbuddyproject.repository.MemberRepository;
import com.example.bookbuddyproject.repository.MessageRepository;
import com.example.bookbuddyproject.web.ChatController.ChatMessageDto;
import com.example.bookbuddyproject.web.ChatController.ChatRoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public Long createOrGetChatRoom(Long senderId, Long receiverId, Long bookId) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findExistingRoom(senderId, receiverId, bookId);
        if (existingRoom.isPresent()) {
            return existingRoom.get().getId();
        }

        Member sender = memberRepository.findOne(senderId);
        Member receiver = memberRepository.findOne(receiverId);
        Book book = bookRepository.findOne(bookId);

        ChatRoom chatRoom = ChatRoom.createRoom(sender, receiver, book);
        chatRoomRepository.save(chatRoom);
        
        return chatRoom.getId();
    }

    public ChatMessageDto saveMessage(ChatMessageDto dto) {
        Member sender = memberRepository.findOne(dto.getSenderId());
        
        Message message = Message.createMessage(dto.getRoomId(), sender, dto.getContent());
        messageRepository.save(message);

        dto.setSentAt(message.getSentAt().toString());
        dto.setSenderName(sender.getLoginId());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDto> getMyChatRooms(Long memberId) {
        List<ChatRoom> rooms = chatRoomRepository.findMyChatRooms(memberId);
        List<ChatRoomDto> dtos = new ArrayList<>();

        for (ChatRoom room : rooms) {
            String roomId = String.valueOf(room.getId());
            Member partner = room.getMember1().getId().equals(memberId) ? room.getMember2() : room.getMember1();

            String lastMsg = "대화를 시작해보세요.";
            List<Message> msgs = messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
            if (!msgs.isEmpty()) {
                lastMsg = msgs.get(msgs.size() - 1).getContent();
            }

            String roomName = partner.getLoginId();
            if (room.getBook() != null) {
                roomName += " (" + room.getBook().getTitle() + ")";
            }

            dtos.add(new ChatRoomDto(roomId, roomName, lastMsg, "대화중", partner.getId()));
        }
        return dtos;
    }
}