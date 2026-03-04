import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

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
    const location = useLocation();
    const navigate = useNavigate();

    // ✅ รับชื่อจาก state + รองรับ refresh ด้วย localStorage
    const userName: string =
        ((location.state as any)?.user as string | undefined) ??
        localStorage.getItem("username") ??
        "";

    const [roomState, setRoomState] = useState<RoomState | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const you = useMemo(() => {
        if (!roomState?.you?.id) return null;
        return roomState.players.find((p) => p.id === roomState.you!.id) ?? null;
    }, [roomState]);

    // ✅ guard: roomId ต้องมี
    useEffect(() => {
        if (!roomId) {
            setError("Missing roomId in URL (expected /waitingRoom/:roomId)");
            setLoading(false);
        }
    }, [roomId]);

    // ✅ join ห้องครั้งแรก (ครั้งเดียวต่อการเข้า roomId นี้)
    useEffect(() => {
        if (!roomId) return;

        // ถ้าไม่มีชื่อ ให้เด้งกลับ login (หรือจะโชว์ error ก็ได้)
        if (!userName || userName.trim().length < 3) {
            setError("Missing username (please login again)");
            setLoading(false);
            return;
        }

        // เก็บไว้กัน refresh
        localStorage.setItem("username", userName);

        let cancelled = false;

        async function joinAndLoad() {
            try {
                setLoading(true);
                setError(null);

                const res = await fetch(`/api/room/${roomId}/join`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ name: userName }),
                });
                if (!res.ok) throw new Error(`Join failed (${res.status})`);

                // โหลดสถานะห้องครั้งแรก
                const roomRes = await fetch(`/api/room/${roomId}`);
                if (!roomRes.ok) throw new Error(`Failed to load room (${roomRes.status})`);
                const data: RoomState = await roomRes.json();

                if (!cancelled) setRoomState(data);
            } catch (e: any) {
                if (!cancelled) setError(e?.message ?? "Join failed");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        joinAndLoad();
        return () => {
            cancelled = true;
        };
    }, [roomId, userName]);

    // ✅ STOMP WebSocket subscribe: server push room state
    useEffect(() => {
        if (!roomId) return;

        const client = new Client({
            // ใช้ SockJS endpoint /ws (ตาม config ฝั่ง Spring)
            webSocketFactory: () => new SockJS("/ws"),
            reconnectDelay: 2000,
            onConnect: () => {
                client.subscribe(`/topic/room/${roomId}`, (msg) => {
                    try {
                        const data: RoomState = JSON.parse(msg.body);
                        setRoomState(data);
                    } catch {
                        // ignore parse errors
                    }
                });
            },
            onStompError: (frame) => {
                console.warn("STOMP error", frame.headers["message"], frame.body);
            },
        });

        client.activate();
        return () => client.deactivate();
    }, [roomId]);

    // ✅ fallback polling ช้า ๆ (เผื่อ WS หลุด/ยังไม่พร้อม)
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
        }, 8000); // ✅ ทุก 8 วิพอ

        return () => clearInterval(t);
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
        // ✅ ไม่ต้อง setState เอง เดี๋ยว WS push มา (fallback polling ก็จะอัปเดตให้)
    }

    async function startGame() {
        if (!roomId) return;
        await fetch(`/api/room/${roomId}/start`, { method: "POST" });
    }

    function copyRoomLink() {
        const link = `${window.location.origin}/waitingRoom/${roomId}`;
        navigator.clipboard
            .writeText(link)
            .then(() => alert("Room link copied to clipboard!"))
            .catch(() => alert("Failed to copy room link."));
    }

    // ✅ render states
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