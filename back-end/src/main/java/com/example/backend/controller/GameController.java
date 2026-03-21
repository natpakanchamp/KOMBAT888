package com.example.backend.controller;

import com.example.backend.model.engine.GameState;
import com.example.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/game/{roomId}")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public String startGame(@PathVariable String roomId) {
        gameService.startGame(roomId);
        return "Game Started";
    }

    @PostMapping("/next-turn")
    public String nextTurn(@PathVariable String roomId) {
        gameService.nextTurn(roomId);
        return "Turn executed";
    }



    @GetMapping("/state")
    public GameState getState(@PathVariable String roomId) {
        return gameService.getState(roomId);
    }

    @GetMapping("/game-over")
    public boolean gameOver(@PathVariable String roomId) {
        return gameService.isGameOver(roomId);
    }

    // ---- error mapping ----
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(new ErrorBody(e.getMessage()));
    }

    public record ErrorBody(String message) {}
}
