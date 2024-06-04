package com.DormitoryBack.domain.group.chat.domain.service;

import com.DormitoryBack.domain.group.chat.domain.entity.Message;
import com.DormitoryBack.domain.group.chat.domain.enums.MessageType;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {
    private final MessageService messageService;
    public void sendSocketMessage(SocketIOClient client, Message message, String room) {
        client.sendEvent("readMessage", message);
    }
    public void saveMessage(SocketIOClient senderClient, Message message) {
        Message storedMessage = messageService.saveMessage(
                Message.builder()
                        .messageType(MessageType.CLIENT)
                        .message(message.getMessage())
                        .room(message.getRoom())
                        .username(message.getUsername())
                        .createdTime(LocalDateTime.now())
                        .build()
        );
        storedMessage.setCreatedTime(null);
        sendSocketMessage(senderClient, storedMessage, message.getRoom());
    }
    public void saveInfoMessage(SocketIOClient senderClient, String message, String room) {
        Message storedMessage = messageService.saveMessage(
                Message.builder()
                        .messageType(MessageType.SERVER)
                        .message(message)
                        .room(room)
                        .createdTime(LocalDateTime.now())
                        .build()
        );
        storedMessage.setCreatedTime(null);
        sendSocketMessage(senderClient, storedMessage, room);
    }
}