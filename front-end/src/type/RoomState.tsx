import type { Player } from "./Player.ts"

export type RoomState = {
    roomId: string;
    state: "waiting" | "starting" | "in_game" | "closed";
    maxPlayers: number;
    players: Player[];
    gameSettings: { map: string; mode: string };
    you?: { id: string };
};