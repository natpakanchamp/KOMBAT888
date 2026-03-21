package com.example.backend.engine;

import com.example.backend.dto.RoomDtos;
import com.example.backend.model.engine.GameConfig;
import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;

import java.util.List;
import java.util.Map;

public class GameEngine {

    private GameState gameState;
    private GameConfig config;

    public GameEngine() {
        config = GameConfig.loadFromFile("config.txt");
        gameState = new GameState(10, 10, config);
    }

    /**
     * สร้าง Unit จาก minion ที่แต่ละ player เลือก
     * @param playerMinions  key = owner (1, 2, ...), value = list ของ MinionDto ที่เลือก
     */
    public void initial(Map<Integer, List<RoomDtos.MinionDto>> playerMinions) {
        System.out.println("Compiling strategies...");
        Unit.resetId();

        // ตำแหน่งเริ่มต้นของแต่ละ player
        int[][] startPositions = {
                {0, 0},  // player 1
                {9, 9},  // player 2
        };

        for (Map.Entry<Integer, List<RoomDtos.MinionDto>> entry : playerMinions.entrySet()) {
            int owner = entry.getKey();
            List<RoomDtos.MinionDto> minions = entry.getValue();

            int[] pos = startPositions[Math.min(owner - 1, startPositions.length - 1)];

            for (RoomDtos.MinionDto m : minions) {
                int unitType = mapType(m.type());
                Unit unit = new Unit(1L, owner, unitType, pos[0], pos[1]);
                // TODO: compile m.strategy() แล้ว set ให้ unit ด้วย unit.setStrategy(...)
                gameState.addUnit(unit);
            }
        }
    }

    /** แปลงชื่อ type จาก frontend เป็น Unit constant */
    private int mapType(String typeName) {
        return switch (typeName.toLowerCase()) {
            case "saber"     -> Unit.TYPE_SABER;
            case "archer"    -> Unit.TYPE_ARCHER;
            case "lancer"    -> Unit.TYPE_LANCER;
            case "caster"    -> Unit.TYPE_CASTER;
            case "berserker" -> Unit.TYPE_BERSERKER;
            default          -> Unit.TYPE_SABER;
        };
    }

    public void executeTurn() {
        if (gameState.getCurrentTurn() > gameState.getMaxTurns()) {
            return;
        }
        System.out.println("========== TURN " + gameState.getCurrentTurn() + " ==========");
        System.out.println("--- Player 1's Turn ---");
        System.out.println("--- Player 2's Turn ---");
        gameState.setCurrentTurn(gameState.getCurrentTurn() + 1);
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isGameOver() {
        return gameState.getCurrentTurn() > gameState.getMaxTurns();
    }
}