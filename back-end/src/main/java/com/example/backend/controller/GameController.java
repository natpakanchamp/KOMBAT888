package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.model.GameState;
import com.example.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// send data is JSON
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor

public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public String startGame(){
        gameService.startGame();
        return "Game Started";
    }

    @PostMapping("/next-turn")
        public String nextTurn(){
        gameService.nextTurn();
        return "Turn executed" ;
    }
    // return game_sate is JSON
    @GetMapping("/state")
    public GameState getGState(){
        return  gameService.getState();
    }

    @GetMapping("/game-over")
    public boolean gameOver(){
        return gameService.isGameOver() ;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return gameService.login(request.getUsername());
    }
}

