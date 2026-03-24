package com.example.backend.service;

import com.example.backend.dto.RoomDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@RequiredArgsConstructor
@Service
public class RoomService {
    private final SimpMessagingTemplate broker;
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

    // join existing room (synchronized กัน race condition ป้องกัน join ซ้ำ)
    public synchronized RoomDtos.RoomStateDto joinRoom(String roomId, String name) {
        Room room = mustGet(roomId);

        if (!"waiting".equals(room.state)) {
            throw new IllegalStateException("Room not joinable (state=" + room.state + ")");
        }

        // เช็ค rejoin ก่อน maxPlayers — ถ้าชื่อซ้ำ = คนเดิมกลับมา (ค้นทั้ง players + spectators)
        Optional<RoomPlayer> existing = room.allMembers().stream()
                .filter(p -> p.name.equalsIgnoreCase(name))
                .findFirst();
        if (existing.isPresent()) {
            return toDto(room, existing.get().id);  // rejoin — คืน state เดิม
        }

        if (room.players.size() >= room.maxPlayers) {
            throw new IllegalStateException("Room is full");
        }

        // ถ้าไม่มี host ในห้อง (เช่น host ไป spectate แล้วเหลือแต่ bot) → ตั้งคนใหม่เป็น host
        boolean hasHost = room.players.stream().anyMatch(x -> x.isHost);
        RoomPlayer p = new RoomPlayer(nextPlayerId(), name, new ArrayList<>(), !hasHost, false);
        room.players.add(p);
        RoomDtos.RoomStateDto dto = toDto(room, p.id);
        // Broadcast ให้ทุกคนที่อยู่ใน {roomId} รู้ว่าใคร join
        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
        return dto;
    }

    public RoomDtos.RoomStateDto getRoom(String roomId) {
        Room room = mustGet(roomId);
        // หมายเหตุ: ถ้าไม่รู้ youId ให้คืน null
        return toDto(room, null);
    }

    public RoomDtos.RoomStateDto toggleReady(String roomId, String playerId) {
        Room room = mustGet(roomId);
        RoomPlayer p = mustFindPlayer(room, playerId);
        if (p.isHost) throw new IllegalStateException("Host cannot toggle ready");
        p.isReady = !p.isReady;
        RoomDtos.RoomStateDto dto = toDto(room, p.id);
        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
        return dto;
    }

    // ผู้เล่นส่ง minion ที่เลือกมาเก็บไว้ใน room
    public RoomDtos.RoomStateDto setMinions(String roomId, String playerId, List<RoomDtos.MinionDto> minions) {
        Room room = mustGet(roomId);
        RoomPlayer p = mustFindPlayer(room, playerId);
        p.minions = (minions != null) ? new ArrayList<>(minions) : new ArrayList<>();
        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
        return toDto(room, playerId);
    }

    public RoomDtos.RoomStateDto startGame(String roomId, String playerId) {
        Room room = mustGet(roomId);
        RoomPlayer p = mustFindPlayer(room, playerId);

        if (!p.isHost) throw new IllegalStateException("Only host can start");

        boolean allReady = !room.players.isEmpty()
                && room.players.stream()
                        .filter(x -> !x.isSpectator)
                        .allMatch(x -> x.isHost || x.isReady);

        if (!allReady) throw new IllegalStateException("Not all players are ready");

        // เช็คว่าผู้เล่น (ที่ไม่ใช่ bot และไม่ใช่ spectator) เลือก minion แล้ว
        boolean allHaveMinions = room.players.stream()
                .filter(x -> !x.name.startsWith("natpakanKanthasorn_") && !x.isSpectator)
                .allMatch(x -> x.minions != null && !x.minions.isEmpty());
        if (!allHaveMinions) throw new IllegalStateException("Not all players have selected minions");

        // Bot ใช้ minion เดียวกับ Host (host อาจเป็น spectator)
        RoomPlayer host = room.allMembers().stream().filter(x -> x.isHost).findFirst().orElse(null);
        if (host != null) {
            for (RoomPlayer bot : room.players) {
                if (bot.name.startsWith("natpakanKanthasorn_")) {
                    bot.minions = new ArrayList<>(host.minions);
                }
            }
        }

        room.state = "in_game";

        // รวม minion ของผู้เล่น (ไม่รวม spectator) ส่งให้ GameEngine
        Map<Integer, List<RoomDtos.MinionDto>> playerMinions = new LinkedHashMap<>();
        int ownerIndex = 1;
        for (RoomPlayer rp : room.players) {
            if (rp.isSpectator) continue;
            playerMinions.put(ownerIndex++, rp.minions);
        }
        gameService.startGame(roomId, playerMinions);

        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
        return toDto(room, playerId);
    }

    public RoomDtos.RoomStateDto kickPlayer(String roomId, String hostId, String targetId) {
        Room room = mustGet(roomId);

        if (!"waiting".equals(room.state)) {
            throw new IllegalStateException("Can only kick in waiting state");
        }

        RoomPlayer host = mustFindPlayer(room, hostId);
        if (!host.isHost) {
            throw new IllegalStateException("Only host can kick players");
        }

        if (hostId.equals(targetId)) {
            throw new IllegalStateException("Host cannot kick themselves");
        }

        boolean removed = room.players.removeIf(p -> p.id.equals(targetId));
        if (!removed) {
            // ลองลบจาก spectators
            removed = room.spectators.remove(targetId) != null;
        }
        if (!removed) {
            throw new NoSuchElementException("Target player not found");
        }

        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
        return toDto(room, hostId);
    }

    public synchronized RoomDtos.RoomStateDto toggleSpectator(String roomId, String playerId, boolean isSpectator) {
        Room room = mustGet(roomId);

        if (isSpectator) {
            // ย้าย player → spectator
            RoomPlayer p = room.players.stream()
                    .filter(x -> x.id.equals(playerId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Player not found"));

            // host ยังคงเป็น host แม้จะไป spectate
            room.players.removeIf(x -> x.id.equals(playerId));
            p.isSpectator = true;
            p.isReady = false;
            room.spectators.put(playerId, p);
        } else {
            // ย้าย spectator → player
            RoomPlayer p = room.spectators.remove(playerId);
            if (p == null) throw new NoSuchElementException("Spectator not found");

            long activeCount = room.players.size();
            if (activeCount >= room.maxPlayers) {
                // ห้องเต็ม → ใส่กลับเป็น spectator
                room.spectators.put(playerId, p);
                throw new IllegalStateException("Room is full, cannot rejoin as player");
            }

            p.isSpectator = false;
            p.isReady = false;
            // ถ้าไม่มี host → ตั้งคนนี้เป็น host
            boolean hasHost = room.players.stream().anyMatch(x -> x.isHost);
            if (!hasHost) p.isHost = true;
            room.players.add(p);
        }

        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
        return toDto(room, playerId);
    }

    public void leaveRoom(String roomId, String playerId){
        Room room = rooms.get(roomId);
        if(room == null) return;
        room.players.removeIf(p -> p.id.equals(playerId));
        room.spectators.remove(playerId);
        // ถ้าไม่มีคนอยู่ในห้องเลย (ทั้ง players + spectators) ให้ลบห้อง
        if(room.players.isEmpty() && room.spectators.isEmpty()){
            rooms.remove(roomId);
            return;
        }
        // ถ้า Host ออก → โอนให้ผู้เล่นจริง (ข้าม bot)
        if (!room.players.isEmpty()) {
            boolean hasHost = room.players.stream().anyMatch(p -> p.isHost);
            if (!hasHost) {
                room.players.stream()
                        .filter(x -> !x.name.startsWith("natpakanKanthasorn_"))
                        .findFirst()
                        .ifPresent(x -> x.isHost = true);
            }
        }
        // Broadcast ไปทุกคนที่อยู่ในห้อง
        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));
    }

    // -------- helpers --------

    private Room mustGet(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) throw new NoSuchElementException("Room not found");
        return room;
    }

    private RoomPlayer mustFindPlayer(Room room, String playerId) {
        // ค้นใน players ก่อน
        Optional<RoomPlayer> found = room.players.stream()
                .filter(x -> x.id.equals(playerId))
                .findFirst();
        if (found.isPresent()) return found.get();
        // ค้นใน spectators
        RoomPlayer spec = room.spectators.get(playerId);
        if (spec != null) return spec;
        throw new NoSuchElementException("Player not found");
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
        // รวม players + spectators (มีลำดับ) เป็น list เดียวส่งให้ frontend
        List<RoomDtos.PlayerDto> ps = room.allMembers().stream()
                .map(p -> new RoomDtos.PlayerDto(
                        p.id,
                        p.name.startsWith("natpakanKanthasorn_") ? "BOT" : p.name,
                        p.minions, p.isHost, p.isReady, p.isSpectator
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

    public RoomDtos.RoomStateDto addBot(String roomId) {
        // find room from room id , if dont find thorw excep
        Room room = mustGet(roomId);

        // ต้องอยู่ห้อง waitting ก่อนไม่งั้นเพิ่มบอทไม่ได้
        if (!"waiting".equals(room.state) )
            throw new IllegalStateException("Room state not found");

        // check ว่าห้องยังไม่เต็ม (นับเฉพาะ active players)
        long activeCount = room.players.stream().filter(x -> !x.isSpectator).count();
        if (activeCount >= room.maxPlayers) {
            throw new IllegalStateException("Room full ");
        }

        // create bot
        RoomPlayer bot = new RoomPlayer(
                nextPlayerId() ,
                "natpakanKanthasorn_" + (room.players.size() + 1) ,
                new ArrayList<>() ,
                false,
                true
        );

        room.players.add(bot);
        // broadcast to everyone in this room
        // convertAndSend: ส่ง msg ไป ยัง client แบบ realtime
        broker.convertAndSend("/topic/room/" + roomId, toDto(room, null));


        return toDto(room , bot.id) ;
    }

    // -------- internal models --------
    private static class Room {
        String roomId;
        String state; // waiting|starting|in_game|closed
        int maxPlayers;
        List<RoomPlayer> players;
        // เก็บ spectators แยกใน Map มีลำดับ + thread-safe
        Map<String, RoomPlayer> spectators = Collections.synchronizedMap(new LinkedHashMap<>());
        RoomDtos.GameSettingsDto gameSettings;

        Room(String roomId, String state, int maxPlayers, List<RoomPlayer> players, RoomDtos.GameSettingsDto gameSettings) {
            this.roomId = roomId;
            this.state = state;
            this.maxPlayers = maxPlayers;
            this.players = players;
            this.gameSettings = gameSettings;
        }

        /** คืน players + spectators รวมกัน (players ก่อน, spectators ตามหลัง) */
        List<RoomPlayer> allMembers() {
            List<RoomPlayer> all = new ArrayList<>(players);
            all.addAll(spectators.values());
            return all;
        }
    }

    private static class RoomPlayer {
        String id;
        String name;
        List<RoomDtos.MinionDto> minions;
        boolean isHost;
        boolean isReady;
        boolean isSpectator;

        RoomPlayer(String id, String name, List<RoomDtos.MinionDto> minions, boolean isHost, boolean isReady) {
            this.id = id;
            this.name = name;
            this.minions = minions;
            this.isHost = isHost;
            this.isReady = isReady;
            this.isSpectator = false;
        }
    }
}