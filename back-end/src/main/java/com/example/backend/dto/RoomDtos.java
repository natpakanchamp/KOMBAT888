package com.example.backend.dto;

import java.util.List;

public class RoomDtos {

    public record CreateRoomRequest(String name) {}
    public record JoinRoomRequest(String name) {}
    public record PlayerActionRequest(String playerId) {}

    public record PlayerDto(
            String id,
            String name,
            List<String> minions,
            boolean isHost,
            boolean isReady
    ) {}

    public record GameSettingsDto(String map, String mode) {}

    public record YouDto(String id) {}

    public record RoomStateDto(
            String roomId,
            String state, // waiting|starting|in_game|closed
            int maxPlayers,
            List<PlayerDto> players,
            GameSettingsDto gameSettings,
            YouDto you // may be null
    ) {}
}