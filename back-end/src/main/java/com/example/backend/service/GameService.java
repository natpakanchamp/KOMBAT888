package com.example.backend.service;

import com.example.backend.dto.GameSummaryDto;
import com.example.backend.dto.RoomDtos;
import com.example.backend.engine.GameEngine;
import com.example.backend.model.engine.GameConfig;
import com.example.backend.model.engine.Unit;
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
    public void nextTurn(String roomId, int player) {
        GameEngine engine = mustGetEngine(roomId);

        if (engine.getGameState().getCurrentPlayer() != player ){
            throw  new IllegalStateException("Not your turn") ;
        }
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

    // ซื้อ hex และ broadcast (ดึงค่าจาก config)
    public boolean buyHex(String roomId, int player, int row, int col) {
        GameEngine engine = mustGetEngine(roomId);
        GameState state = engine.getGameState() ;

        if (player != state.getCurrentPlayer()) {
            return false ;
        }

        long cost = engine.getConfig().getHexPurchaseCost();
        boolean ok = engine.getGameState().buyHex(row, col, player, cost);
        if (ok) broadcastState(roomId);
        return ok;
    }

    // spawn unit, หักเงิน และ broadcast
    public boolean spawnUnit(String roomId, int player, String minionType, int row, int col) {
        GameEngine engine = mustGetEngine(roomId);
        GameState state  =  engine.getGameState() ;
        if (player != state.getCurrentPlayer()) {
            return false  ;  // ซื้อไม่ได้จ้าไม่ใช้ตามึง
        }
        long cost = engine.getConfig().getSpawnCost();
       // GameState state = engine.getGameState();

        // เช็คและหักเงิน
        if (player == 1 && state.getP1Budget() >= cost) {
            state.setP1BudgetExact(state.getP1BudgetExact() - cost);
            state.setP1Budget((long) state.getP1BudgetExact());
        } else if (player == 2 && state.getP2Budget() >= cost) {
            state.setP2BudgetExact(state.getP2BudgetExact() - cost);
            state.setP2Budget((long) state.getP2BudgetExact());
        } else {
            return false; // เงินไม่พอ
        }

        int type = engine.mapType(minionType);
        Unit unit = new Unit(1L, player, type, row, col);
        state.addUnit(unit);
        broadcastState(roomId);
        return true;
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

    // ดึง config ของเกม
    public GameConfig getConfig(String roomId) {
        return mustGetEngine(roomId).getConfig();
    }

    public GameSummaryDto getSummary(String roomId) {
        GameEngine engine = mustGetEngine(roomId);
        return engine.createSummary();
    }

        // API หลังทำเสร็จ
        //GET /api/game/{roomId}/summary
    }

