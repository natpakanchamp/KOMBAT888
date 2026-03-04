package com.example.backend.model.engine;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    // เก็บข้อมูลห้องทั้งหมด (ปลอดภัยเมื่อมีคนเข้าเล่นพร้อมกัน)
    private final Map<String, GameState> activeRooms = new ConcurrentHashMap<>();

    // สร้างห้องใหม่และคืนค่า รหัสห้อง (Room ID)
    public String createRoom() {
        // 💡 ปรับปรุง: หั่น UUID เอาแค่ 6 ตัวอักษรแรก และทำให้เป็นตัวพิมพ์ใหญ่ทั้งหมด (เช่น A8F9B2)
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 🚨 แก้ไข: ใส่ค่าเริ่มต้นให้กระดาน (แถว=10, คอลัมน์=10, เทิร์นสูงสุด=100, เงินเริ่มต้น=10000)
        // (คุณสามารถปรับตัวเลขพวกนี้ให้ตรงกับดีไซน์เกมของคุณได้เลยครับ)
        GameState newGame = new GameState(10, 10, 100, 10000);

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
        System.out.println("Room deleted: " + roomId);
    }
}