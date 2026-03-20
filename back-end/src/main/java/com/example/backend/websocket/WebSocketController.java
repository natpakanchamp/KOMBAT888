package com.example.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * รับ STOMP message จาก frontend เพื่อ register session กับ room/player
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketEventListener eventListener;

    /**
     * Frontend ส่ง { roomId, playerId } มาที่ /app/room/register
     * เพื่อผูก WebSocket session กับผู้เล่น → ตรวจจับ disconnect ได้
     */
    @MessageMapping("/room/register")
    public void registerPlayer(Map<String, String> payload, StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String roomId = payload.get("roomId");
        String playerId = payload.get("playerId");
        eventListener.registerSession(sessionId, roomId, playerId);
    }
}
