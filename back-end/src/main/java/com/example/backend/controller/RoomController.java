package com.example.backend.controller;

import com.example.backend.dto.RoomDtos;
import com.example.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // LoginPage ใช้อันนี้
    @PostMapping
    public RoomDtos.RoomStateDto createRoom(@RequestBody RoomDtos.CreateRoomRequest req) {
        if (req == null || req.name() == null || req.name().trim().length() < 3) {
            throw new IllegalStateException("Invalid name");
        }
        return roomService.createRoom(req.name().trim());
    }

    // WaitingRoomPage ใช้อันนี้
    @PostMapping("/{roomId}/join")
    public RoomDtos.RoomStateDto join(@PathVariable String roomId, @RequestBody RoomDtos.JoinRoomRequest req) {
        if (req == null || req.name() == null || req.name().trim().length() < 3) {
            throw new IllegalStateException("Invalid name");
        }
        return roomService.joinRoom(roomId, req.name().trim());
    }

    @GetMapping("/{roomId}")
    public RoomDtos.RoomStateDto get(@PathVariable String roomId) {
        return roomService.getRoom(roomId);
    }

    @PostMapping("/{roomId}/ready")
    public RoomDtos.RoomStateDto ready(@PathVariable String roomId, @RequestBody RoomDtos.PlayerActionRequest req) {
        if (req == null || req.playerId() == null || req.playerId().isBlank()) {
            throw new IllegalStateException("Missing playerId");
        }
        return roomService.toggleReady(roomId, req.playerId());
    }

    @PostMapping("/{roomId}/start")
    public RoomDtos.RoomStateDto start(@PathVariable String roomId, @RequestBody RoomDtos.PlayerActionRequest req) {
        if (req == null || req.playerId() == null || req.playerId().isBlank()) {
            throw new IllegalStateException("Missing playerId");
        }
        return roomService.startGame(roomId, req.playerId());
    }

    // ---- simple error mapping (ให้ frontend อ่านง่าย) ----
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(new ErrorBody(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> badRequest(IllegalStateException e) {
        return ResponseEntity.status(400).body(new ErrorBody(e.getMessage()));
    }

    public record ErrorBody(String message) {}
}