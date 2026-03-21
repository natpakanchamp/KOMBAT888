package com.example.backend.engine;

import com.example.backend.model.engine.GameConfig;
import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;

public class GameEngine {

    private GameState gameState;
    private GameConfig config;

    public GameEngine() {
        // โหลด config (ใช้ค่า default ถ้าไม่มีไฟล์)
        config = GameConfig.loadFromFile("config.txt");
        gameState = new GameState(10, 10, config);
    }

    public void initial() {
        System.out.println("Compiling strategies...");
        Unit.resetId();
        // Unit(defense, owner, type, row, col)
        gameState.addUnit(new Unit(1L, 1, Unit.TYPE_SABER, 0, 0));
        gameState.addUnit(new Unit(1L, 2, Unit.TYPE_SABER, 9, 9));
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