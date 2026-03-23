export type Player = {
    id: string;
    name: string;
    minions: string[];
    isHost: boolean;
    isReady: boolean;
    isSpectator: boolean;
};