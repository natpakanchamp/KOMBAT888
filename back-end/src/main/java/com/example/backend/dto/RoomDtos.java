package com.example.backend.dto;

import java.util.List;

public class RoomDtos {

    public record CreateRoomRequest(String name) {}
    public record JoinRoomRequest(String name) {}
    public record PlayerActionRequest(String playerId) {}
    public record KickRequest(String hostId, String targetId) {}

    public record SetMinionsRequest(String playerId, List<MinionDto> minions) {}
    public record MinionDto(String type, String strategy) {}

    public record SpectateRequest(String playerId, boolean isSpectator) {}

    // Game actions
    public record BuyHexRequest(int player, int row, int col) {}
    public record SpawnUnitRequest(int player, String minionType, int row, int col) {}

    public record PlayerDto(
            String id,
            String name,
            List<MinionDto> minions,
            boolean isHost,
            boolean isReady,
            boolean isSpectator
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