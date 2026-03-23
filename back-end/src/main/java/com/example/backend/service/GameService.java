package com.example.backend.service;

import com.example.backend.dto.GameSummaryDto;
import com.example.backend.dto.RoomDtos;
import com.example.backend.engine.GameEngine;
import com.example.backend.model.engine.GameState;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {

    private final SimpMessagingTemplate messagingTemplate;

    // แยก GameEngine ตาม roomId
    private final Map<String, GameEngine> engines = new ConcurrentHashMap<>();

    // เริ่มเกมของห้องนั้นๆ โดยรับ minion ที่แต่ละ player เลือก
    public void startGame(String roomId, Map<Integer, List<RoomDtos.MinionDto>> playerMinions) {
        GameEngine engine = new GameEngine();
        engine.initial(playerMinions);
        engines.put(roomId, engine);
        broadcastState(roomId);
    }

    // execute turn ถัดไป
    public void nextTurn(String roomId) {
        GameEngine engine = mustGetEngine(roomId);
        engine.executeTurn();
        broadcastState(roomId);
    }

    // ดึง state ปัจจุบัน
    public GameState getState(String roomId) {
        return mustGetEngine(roomId).getGameState();
    }

    // เช็คว่าเกมจบหรือยัง
    public boolean isGameOver(String roomId) {
        return mustGetEngine(roomId).isGameOver();
    }

    // ลบเกมของห้อง (เมื่อเกมจบ หรือห้องถูกลบ)
    public void removeGame(String roomId) {
        engines.remove(roomId);
    }

    // broadcast สถานะเกมไปที่ /topic/game/{roomId}
    private void broadcastState(String roomId) {
        GameEngine engine = engines.get(roomId);
        if (engine != null) {
            messagingTemplate.convertAndSend("/topic/game/" + roomId, engine.getGameState());
        }
    }

    private GameEngine mustGetEngine(String roomId) {
        GameEngine engine = engines.get(roomId);
        if (engine == null) {
            throw new NoSuchElementException("Game not found for room: " + roomId);
        }
        return engine;
    }

    public GameSummaryDto getSummary(String roomId) {

        GameEngine engine = mustGetEngine(roomId);
        return engine.createSummary();
    }

        // API หลังทำเสร็จ
        //GET /api/game/{roomId}/summary
    }

