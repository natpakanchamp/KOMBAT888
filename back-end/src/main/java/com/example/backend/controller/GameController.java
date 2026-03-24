package com.example.backend.controller;

import com.example.backend.dto.GameSummaryDto;
import com.example.backend.dto.RoomDtos;
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

    // start ถูกเรียกผ่าน RoomController → RoomService.startGame() แทนแล้ว
    // endpoint นี้ไม่ใช้แล้ว

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


    @PostMapping("/buy-hex")
    public ResponseEntity<?> buyHex(@PathVariable String roomId, @RequestBody RoomDtos.BuyHexRequest req) {
        boolean ok = gameService.buyHex(roomId, req.player(), req.row(), req.col());
        if (!ok) return ResponseEntity.badRequest().body(new ErrorBody("Cannot buy hex: insufficient budget or invalid position"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/spawn")
    public ResponseEntity<?> spawnUnit(@PathVariable String roomId, @RequestBody RoomDtos.SpawnUnitRequest req) {
        gameService.spawnUnit(roomId, req.player(), req.minionType(), req.row(), req.col());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary")
    public GameSummaryDto getSummary(@PathVariable String roomId) {
        return gameService.getSummary(roomId) ;
    }
    // ---- error mapping ----
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(new ErrorBody(e.getMessage()));
    }

    public record ErrorBody(String message) {}
}
