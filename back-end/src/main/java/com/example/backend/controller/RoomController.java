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

    /**
     * ออกห้องตอนอยู่ใน Waiting room
     * ถ้าหา player ไม่เจอให้ส่งไปหน้า Bad req "Missing Player ID"
     * @param roomId
     * @param req
     * @return
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leave(@PathVariable String roomId, @RequestBody RoomDtos.PlayerActionRequest req) {
        // กรณีหา Player ไม่ได้
        if(req == null || req.playerId() == null || req.playerId().isBlank()){
            return ResponseEntity.badRequest().body(new ErrorBody("Missing Player ID"));
        }
        roomService.leaveRoom(roomId, req.playerId());
        return ResponseEntity.ok().build();
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

    @PostMapping("/{roomId}/set-minions")
    public RoomDtos.RoomStateDto setMinions(@PathVariable String roomId, @RequestBody RoomDtos.SetMinionsRequest req) {
        if (req == null || req.playerId() == null || req.playerId().isBlank()) {
            throw new IllegalStateException("Missing playerId");
        }
        return roomService.setMinions(roomId, req.playerId(), req.minions());
    }

    @PostMapping("/{roomId}/start")
    public RoomDtos.RoomStateDto start(@PathVariable String roomId, @RequestBody RoomDtos.PlayerActionRequest req) {
        if (req == null || req.playerId() == null || req.playerId().isBlank()) {
            throw new IllegalStateException("Missing playerId");
        }
        return roomService.startGame(roomId, req.playerId());
    }


    @PostMapping("/{roomId}/kick")
    public ResponseEntity<?> kick(@PathVariable String roomId, @RequestBody RoomDtos.KickRequest req) {
        if (req == null || req.hostId() == null || req.hostId().isBlank()
                || req.targetId() == null || req.targetId().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorBody("Missing hostId or targetId"));
        }
        RoomDtos.RoomStateDto dto = roomService.kickPlayer(roomId, req.hostId(), req.targetId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{roomId}/spectate")
    public RoomDtos.RoomStateDto spectate(@PathVariable String roomId, @RequestBody RoomDtos.SpectateRequest req) {
        if (req == null || req.playerId() == null || req.playerId().isBlank()) {
            throw new IllegalStateException("Missing playerId");
        }
        return roomService.toggleSpectator(roomId, req.playerId(), req.isSpectator());
    }

    @PostMapping("/{roomId}/add-bot")
    public ResponseEntity<?> addBot(@PathVariable String roomId) {
        try {
            // ส่งต่อให้ RoomService จัดการ แล้วคืน state ห้องกลับไป
            RoomDtos.RoomStateDto dto = roomService.addBot(roomId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            // ถ้าหาห้องไม่เจอ → 404
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalStateException e) {
            // ถ้าห้องเต็มหรือ state ไม่ใช่ waiting → 400
            return ResponseEntity.status(400).body(e.getMessage());
        }
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