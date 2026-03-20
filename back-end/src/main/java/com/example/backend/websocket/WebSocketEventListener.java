package com.example.backend.websocket;

import com.example.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ตรวจจับ WebSocket connect/disconnect
 * เมื่อ client disconnect → ลบผู้เล่นออกจากห้องอัตโนมัติ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final RoomService roomService;

    // เก็บ mapping: sessionId → { roomId, playerId }
    private final Map<String, PlayerSession> sessions = new ConcurrentHashMap<>();

    public record PlayerSession(String roomId, String playerId) {}

    /**
     * เมื่อ client register ตัวเอง (ส่ง roomId + playerId มา)
     * ถูกเรียกจาก WebSocketController
     */
    public void registerSession(String sessionId, String roomId, String playerId) {
        if (sessionId != null && roomId != null && playerId != null && !playerId.isBlank()) {
            sessions.put(sessionId, new PlayerSession(roomId, playerId));
            log.info("Registered session {} → room={}, player={}", sessionId, roomId, playerId);
        }
    }

    /**
     * เมื่อ STOMP client connect — ลอง register จาก header ด้วย
     */
    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String roomId = accessor.getFirstNativeHeader("roomId");
        String playerId = accessor.getFirstNativeHeader("playerId");

        if (roomId != null && playerId != null && !playerId.isBlank()) {
            sessions.put(sessionId, new PlayerSession(roomId, playerId));
            log.info("Connect: session={}, room={}, player={}", sessionId, roomId, playerId);
        }
    }

    /**
     * เมื่อ STOMP client disconnect → ลบผู้เล่นออกจากห้อง
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        PlayerSession ps = sessions.remove(sessionId);

        if (ps != null) {
            log.info("Disconnect: session={}, room={}, player={} → removing from room",
                    sessionId, ps.roomId(), ps.playerId());
            roomService.leaveRoom(ps.roomId(), ps.playerId());
        }
    }
}
