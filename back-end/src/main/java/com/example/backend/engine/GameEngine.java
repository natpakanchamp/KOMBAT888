package com.example.backend.engine;
import com.example.backend.model.GameState;
import com.example.backend.model.Unit;

public class GameEngine {

    private GameState gameState ;

    // ถูกเรียกตอน Start game
    public GameEngine(){
        gameState = new GameState() ;

    }

    public void initial() {
        System.out.println("Compiling strategies...");
        // spawn unit เริ่มต้น 2 ตัว
        gameState.getUnits().add(new Unit(1, 0 , 0 )) ;
        gameState.getUnits().add(new Unit(1, 7 , 7)) ;
    }

    public void executeTurn() {
        if (gameState.getTurnCount() > 69) {
            return ;
        }
        System.out.println("========== TURN " + gameState.getTurnCount() + " ==========");
        System.out.println("--- Player 1's Turn ---");
        System.out.println("--- Player 2's Turn ---");
        // เพิ่ม turn
        gameState.nextTurn();
    }
    // คืนค่า game state ปัจจุบัน
    // for Rest API
    public GameState getGameState() {
        return gameState;
    }

    public boolean isGameOver() {
        return gameState.getTurnCount() > 69;
    }

}
