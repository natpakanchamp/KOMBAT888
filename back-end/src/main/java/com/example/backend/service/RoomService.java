package com.example.backend.service;

import com.example.backend.dto.RoomDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@RequiredArgsConstructor
@Service
public class RoomService {
    private final GameService gameService;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private int playerSeq = 1;

    // -------- public API --------

    // create room + host join
    public RoomDtos.RoomStateDto createRoom(String hostName) {
        String roomId = genRoomId();
        Room room = new Room(
                roomId,
                "waiting",
                2,
                new ArrayList<>(),
                new RoomDtos.GameSettingsDto("default", "1v1")
        );

        RoomPlayer host = new RoomPlayer(nextPlayerId(), hostName, new ArrayList<>(), true, false);
        room.players.add(host);

        rooms.put(roomId, room);
        return toDto(room, host.id);
    }

    // join existing room
    public RoomDtos.RoomStateDto joinRoom(String roomId, String name) {
        Room room = mustGet(roomId);

        if (!"waiting".equals(room.state)) {
            throw new IllegalStateException("Room not joinable (state=" + room.state + ")");
        }
        if (room.players.size() >= room.maxPlayers) {
            throw new IllegalStateException("Room is full");
        }
        // กันชื่อซ้ำแบบง่าย (เลือกได้)
        boolean nameDup = room.players.stream().anyMatch(p -> p.name.equalsIgnoreCase(name));
        if (nameDup) {
            throw new IllegalStateException("Name already exists in room");
        }

        RoomPlayer p = new RoomPlayer(nextPlayerId(), name, new ArrayList<>(), false, false);
        room.players.add(p);
        return toDto(room, p.id);
    }

    public RoomDtos.RoomStateDto getRoom(String roomId) {
        Room room = mustGet(roomId);
        // หมายเหตุ: ถ้าไม่รู้ youId ให้คืน null
        return toDto(room, null);
    }

    public RoomDtos.RoomStateDto toggleReady(String roomId, String playerId) {
        Room room = mustGet(roomId);
        RoomPlayer p = mustFindPlayer(room, playerId);
        p.isReady = !p.isReady;
        return toDto(room, playerId);
    }

    public RoomDtos.RoomStateDto startGame(String roomId, String playerId) {
        Room room = mustGet(roomId);
        RoomPlayer p = mustFindPlayer(room, playerId);

        if (!p.isHost) throw new IllegalStateException("Only host can start");

        boolean allReady = !room.players.isEmpty()
                && room.players.stream().allMatch(x -> x.isHost || x.isReady);

        if (!allReady) throw new IllegalStateException("Not all players are ready");

        room.state = "in_game";
        gameService.startGame();
        return toDto(room, playerId);
    }

    // -------- helpers --------

    private Room mustGet(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) throw new NoSuchElementException("Room not found");
        return room;
    }

    private RoomPlayer mustFindPlayer(Room room, String playerId) {
        return room.players.stream()
                .filter(x -> x.id.equals(playerId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Player not found"));
    }

    private synchronized String nextPlayerId() {
        return "p_" + (playerSeq++);
    }

    private String genRoomId() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            String id = sb.toString();
            if (!rooms.containsKey(id)) return id;
        }
    }

    private RoomDtos.RoomStateDto toDto(Room room, String youId) {
        List<RoomDtos.PlayerDto> ps = room.players.stream()
                .map(p -> new RoomDtos.PlayerDto(
                        p.id, p.name, p.minions, p.isHost, p.isReady
                ))
                .toList();

        RoomDtos.YouDto you = (youId == null) ? null : new RoomDtos.YouDto(youId);

        return new RoomDtos.RoomStateDto(
                room.roomId,
                room.state,
                room.maxPlayers,
                ps,
                room.gameSettings,
                you
        );
    }

    // -------- internal models --------
    private static class Room {
        String roomId;
        String state; // waiting|starting|in_game|closed
        int maxPlayers;
        List<RoomPlayer> players;
        RoomDtos.GameSettingsDto gameSettings;

        Room(String roomId, String state, int maxPlayers, List<RoomPlayer> players, RoomDtos.GameSettingsDto gameSettings) {
            this.roomId = roomId;
            this.state = state;
            this.maxPlayers = maxPlayers;
            this.players = players;
            this.gameSettings = gameSettings;
        }
    }

    private static class RoomPlayer {
        String id;
        String name;
        List<String> minions;
        boolean isHost;
        boolean isReady;

        RoomPlayer(String id, String name, List<String> minions, boolean isHost, boolean isReady) {
            this.id = id;
            this.name = name;
            this.minions = minions;
            this.isHost = isHost;
            this.isReady = isReady;
        }
    }
}