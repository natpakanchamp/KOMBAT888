// for test
package com.example.backend.service;

import com.example.backend.engine.GameEngine;
import com.example.backend.model.GameState;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service // business logic layer
@RequiredArgsConstructor //  for create Constructor
public class GameService {
    // for test
//    public String startgame() {
//        return "Game start" ;
//    }

    // ใช้ส่งข้อมูลผ่าน  Websocket
    private final SimpMessagingTemplate messagingTemplate;
    private  GameEngine engine  ;
    // start new game(){
    public void startGame() {
        engine = new GameEngine() ;
        engine.initial();
        // broadcast สถานะเกมให้ client ทันที
        broadcastState();

    }
    public void nextTurn() {
        if (engine == null) {
            throw new IllegalStateException("Game has not started yet.");
        }

        engine.executeTurn();
        broadcastState();
    }
        // send game state to /topic/game-state
        // for only client subscribe
    private void broadcastState() {
        messagingTemplate.convertAndSend("/topic/game-state", engine.getGameState());
    }
    // get status game , for Rest GET/state
    public GameState getState() {
        return engine.getGameState();
    }

    public boolean isGameOver() {
        return engine.isGameOver();
    }

}
