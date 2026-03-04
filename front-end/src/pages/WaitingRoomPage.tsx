import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

type Player = {
    id: string;
    name: string;
    minions: string[];
    isHost: boolean;
    isReady: boolean;
};

type RoomState = {
    roomId: string;
    state: "waiting" | "starting" | "in_game" | "closed";
    maxPlayers: number;
    players: Player[];
    gameSettings: {
        map: string;
        mode: string;
    };
    you?: { id: string };
};

export default function WaitingRoomPage() {
    const { roomId } = useParams();
    const navigate = useNavigate();

    const [roomState, setRoomState] = useState<RoomState | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const you = useMemo(() => {
        if (!roomState?.you?.id) return null;
        return roomState.players.find((p) => p.id === roomState.you!.id) ?? null;
    }, [roomState]);

    // โหลดสถานะห้องครั้งแรก
    useEffect(() => {
        if (!roomId) {
            setError("Invalid room ID");
            setLoading(false);
            return;
        }

        let cancelled = false;

        async function load() {
            try {
                setLoading(true);
                setError(null);

                const res = await fetch(`/api/room/${roomId}`);
                if (!res.ok) throw new Error(`Failed to load room (${res.status})`);
                const data: RoomState = await res.json();

                if (!cancelled) setRoomState(data);
            } catch (e: any) {
                if (!cancelled) setError(e?.message ?? "Failed to load room");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => {
            cancelled = true;
        };
    }, [roomId]);

    // (ตัวเลือก) polling กันข้อมูลค้าง ถ้ายังไม่ทำ websocket
    useEffect(() => {
        if (!roomId) return;

        const t = setInterval(async () => {
            try {
                const res = await fetch(`/api/room/${roomId}`);
                if (!res.ok) return;
                const data: RoomState = await res.json();
                setRoomState(data);
            } catch {
                // ignore
            }
        }, 1200);

        return () => clearInterval(t);
    }, [roomId]);

    // TODO: WebSocket subscribe (ใส่ทีหลัง)
    useEffect(() => {
        // ตัวอย่าง:
        // const ws = new WebSocket(`ws://.../rooms/${roomId}`);
        // ws.onmessage = (ev) => setRoomState(JSON.parse(ev.data));
        // return () => ws.close();
    }, [roomId]);

    // ถ้าเกมเริ่มแล้วให้เด้งไปหน้า battle
    useEffect(() => {
        if (!roomState || !roomId) return;
        if (roomState.state === "in_game") {
            navigate(`/battle/${roomId}`, { replace: true });
        }
    }, [roomState, roomId, navigate]);

    async function toggleReady() {
        if (!roomId) return;
        await fetch(`/api/room/${roomId}/ready`, { method: "POST" });
        // รอ websocket/poll อัปเดต state
    }

    async function startGame() {
        if (!roomId) return;
        await fetch(`/api/room/${roomId}/start`, { method: "POST" });
        // รอ websocket/poll อัปเดต state
    }

    function copyRoomLink() {
        const link = `${window.location.origin}/waitingRoom/${roomId}`;
        navigator.clipboard
            .writeText(link)
            .then(() => alert("Room link copied to clipboard!"))
            .catch(() => alert("Failed to copy room link."));
    }

    if (loading) return <div style={{ padding: 16 }}>Loading room...</div>;
    if (error) return <div style={{ padding: 16, color: "red" }}>{error}</div>;
    if (!roomState) return <div style={{ padding: 16 }}>No room data!!</div>;

    const readyCount = roomState.players.filter((p) => p.isReady).length;
    const allReady =
        roomState.players.length > 0 &&
        roomState.players.every((p) => (p.isHost ? true : p.isReady));

    const isHost = !!you?.isHost;

    return (
        <div style={{ padding: 16, maxWidth: 600, margin: "0 auto" }}>
            <h1>Waiting Room: {roomState.roomId}</h1>

            <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
                <div>
                    <div>
                        <b>Room:</b> {roomState.roomId}
                    </div>
                    <div>
                        <b>Players:</b> {roomState.players.length} / {roomState.maxPlayers}
                    </div>
                    <div>
                        <b>Ready:</b> {readyCount} / {roomState.players.length}
                    </div>
                    <div>
                        <b>Map:</b> {roomState.gameSettings.map} | <b>Mode:</b> {roomState.gameSettings.mode}
                    </div>
                </div>

                <button onClick={copyRoomLink}>Copy Room Link</button>
            </div>

            <hr style={{ margin: "16px 0" }} />

            <h3>Players</h3>
            <ul style={{ listStyle: "none", padding: 0, display: "flex", flexDirection: "column", gap: 8 }}>
                {roomState.players.map((p) => (
                    <li
                        key={p.id}
                        style={{
                            border: `1px solid ${p.isReady ? "green" : "#ddd"}`,
                            borderRadius: 8,
                            padding: 10,
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                        }}
                    >
                        <div>
                            <b>{p.name}</b> {p.isHost && "(Host)"}
                            <div style={{ fontSize: 12, opacity: 0.8 }}>
                                Minions: {p.minions?.length ? p.minions.join(", ") : "-"}
                            </div>
                        </div>

                        <div>{p.isReady ? "Ready" : "Not ready"}</div>
                    </li>
                ))}
            </ul>

            <hr style={{ margin: "16px 0" }} />

            <div style={{ display: "flex", gap: 10, alignItems: "center", flexWrap: "wrap" }}>
                <button onClick={toggleReady}>{you?.isReady ? "Unready" : "Ready"}</button>

                {isHost && (
                    <button onClick={startGame} disabled={!allReady || roomState.players.length === 0}>
                        Start Game
                    </button>
                )}

                {!isHost && <span>Waiting for host to start…</span>}
            </div>
        </div>
    );
}