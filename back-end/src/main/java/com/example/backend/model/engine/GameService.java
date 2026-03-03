package com.example.backend.model.engine;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    // เก็บข้อมูลห้องทั้งหมด (ใช้ ConcurrentHashMap เพื่อความปลอดภัยเมื่อมีคนเข้าเล่นพร้อมกัน)
    private final Map<String, GameState> activeRooms = new ConcurrentHashMap<>();

    // สร้างห้องใหม่และคืนค่า รหัสห้อง (Room ID)
    public String createRoom() {
        String roomId = UUID.randomUUID().toString();
        GameState newGame = new GameState();
        activeRooms.put(roomId, newGame);
        System.out.println("Room created: " + roomId);
        return roomId;
    }

    // ดึงข้อมูลกระดานจากรหัสห้อง
    public GameState getGame(String roomId) {
        return activeRooms.get(roomId);
    }

    // ลบห้องเมื่อเล่นจบ
    public void deleteRoom(String roomId) {
        activeRooms.remove(roomId);
    }
}
