import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { Box, Center, Title, Text, Button, Stack, Group, Paper, Loader, Tooltip } from "@mantine/core";

import background_LightDark from "../assets/background_LightDark.png";
import saber from "../assets/Light_Saber.png";
import archer from "../assets/Light_Archer.png";
import CloseButton from "../components/CloseButton";

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
    gameSettings: { map: string; mode: string };
    you?: { id: string };
};

export default function WaitingRoomPage() {
    const { roomId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();

    const created = (location.state as any)?.created === true;

    const userName: string =
        ((location.state as any)?.user as string | undefined) ??
        localStorage.getItem("username") ??
        "";

    const [roomState, setRoomState] = useState<RoomState | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [copied, setCopied] = useState(false);

    const you = useMemo(() => {
        if (!roomState?.you?.id) return null;
        return roomState.players.find((p) => p.id === roomState.you!.id) ?? null;
    }, [roomState]);

    // guard: roomId required
    useEffect(() => {
        if (!roomId) {
            setError("Missing roomId in URL");
            setLoading(false);
        }
    }, [roomId]);

    // load/join room
    useEffect(() => {
        if (!roomId) return;

        if (!userName || userName.trim().length < 3) {
            setError("Missing username (please login again)");
            setLoading(false);
            return;
        }

        localStorage.setItem("username", userName);
        let cancelled = false;

        async function loadRoom() {
            const roomRes = await fetch(`/api/room/${roomId}`);
            if (!roomRes.ok) throw new Error(`Failed to load room (${roomRes.status})`);
            const data: RoomState = await roomRes.json();
            if (!cancelled) setRoomState(data);
        }

        async function joinThenLoad() {
            const res = await fetch(`/api/room/${roomId}/join`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: userName }),
            });
            if (!res.ok) throw new Error(`Join failed (${res.status})`);
            await loadRoom();
        }

        (async () => {
            try {
                setLoading(true);
                setError(null);
                if (created) {
                    await loadRoom();
                } else {
                    await joinThenLoad();
                }
            } catch (e: any) {
                if (!cancelled) setError(e?.message ?? "Join/Load failed");
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();

        return () => { cancelled = true; };
    }, [roomId, userName, created]);

    // STOMP WebSocket subscribe
    useEffect(() => {
        if (!roomId) return;

        const client = new Client({
            webSocketFactory: () => new SockJS("/ws"),
            reconnectDelay: 2000,
            onConnect: () => {
                client.subscribe(`/topic/room/${roomId}`, (msg) => {
                    try {
                        const data: RoomState = JSON.parse(msg.body);
                        setRoomState(data);
                    } catch { /* ignore */ }
                });
            },
        });

        client.activate();
        return () => { client.deactivate(); };
    }, [roomId]);

    // polling fallback
    useEffect(() => {
        if (!roomId) return;

        const t = setInterval(async () => {
            try {
                const res = await fetch(`/api/room/${roomId}`);
                if (!res.ok) return;
                const data: RoomState = await res.json();
                setRoomState(data);
            } catch { /* ignore */ }
        }, 8000);

        return () => clearInterval(t);
    }, [roomId]);

    // navigate to battle when game starts
    useEffect(() => {
        if (!roomState || !roomId) return;
        if (roomState.state === "in_game") {
            navigate(`/battle/${roomId}`, { replace: true });
        }
    }, [roomState, roomId, navigate]);

    async function toggleReady() {
        if (!roomId || !roomState?.you?.id) return;
        await fetch(`/api/room/${roomId}/ready`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ playerId: roomState.you.id }),
        });
    }

    async function startGame() {
        if (!roomId || !roomState?.you?.id) return;
        await fetch(`/api/room/${roomId}/start`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ playerId: roomState.you.id }),
        });
    }

    function copyRoomLink() {
        const link = `${window.location.origin}/waitingRoom/${roomId}`;
        navigator.clipboard
            .writeText(link)
            .then(() => {
                setCopied(true);
                setTimeout(() => setCopied(false), 2000);
            })
            .catch(() => alert("Failed to copy room link."));
    }

    // ── Loading state ──
    if (loading) {
        return (
            <Box
                style={{
                    height: "100dvh",
                    width: "100%",
                    overflow: "hidden",
                    position: "relative",
                    backgroundImage: `url(${background_LightDark})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                }}
            >
                <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", backdropFilter: "blur(12px)", zIndex: 1 }} />
                <Center style={{ height: "100%", position: "relative", zIndex: 2 }}>
                    <Stack align="center" gap="lg">
                        <Loader color="#FAB005" size="lg" type="dots" />
                        <Text
                            size="lg"
                            fw={600}
                            style={{
                                color: "rgba(210,190,140,0.95)",
                                letterSpacing: 3,
                                textTransform: "uppercase",
                            }}
                        >
                            Entering Room...
                        </Text>
                    </Stack>
                </Center>
            </Box>
        );
    }

    // ── Error state ──
    if (error) {
        return (
            <Box
                style={{
                    height: "100dvh",
                    width: "100%",
                    overflow: "hidden",
                    position: "relative",
                    backgroundImage: `url(${background_LightDark})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                }}
            >
                <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", backdropFilter: "blur(12px)", zIndex: 1 }} />
                <Center style={{ height: "100%", position: "relative", zIndex: 2 }}>
                    <Paper
                        style={{
                            background: "rgba(10,12,16,0.75)",
                            backdropFilter: "blur(8px)",
                            border: "1px solid rgba(170,35,35,0.4)",
                            padding: "40px 48px",
                            borderRadius: 16,
                            textAlign: "center",
                            maxWidth: 420,
                        }}
                    >
                        <Text size="xl" fw={700} style={{ color: "rgba(255,100,100,0.95)", marginBottom: 12 }}>
                            ERROR
                        </Text>
                        <Text size="sm" style={{ color: "rgba(245,245,245,0.8)", marginBottom: 24 }}>
                            {error}
                        </Text>
                        <Button
                            onClick={() => navigate("/login")}
                            styles={{
                                root: {
                                    background: "linear-gradient(180deg, rgba(170,35,35,1), rgba(90,10,10,1))",
                                    border: "1px solid rgba(255,100,100,0.2)",
                                    letterSpacing: 2,
                                    textTransform: "uppercase" as const,
                                },
                            }}
                        >
                            Back to Login
                        </Button>
                    </Paper>
                </Center>
            </Box>
        );
    }

    if (!roomState) return null;

    const readyCount = roomState.players.filter((p) => p.isReady).length;
    const allReady =
        roomState.players.length > 0 &&
        roomState.players.every((p) => (p.isHost ? true : p.isReady));
    const isHost = !!you?.isHost;

    return (
        <Box
            style={{
                height: "100dvh",
                width: "100%",
                overflow: "hidden",
                position: "relative",
                backgroundImage: `url(${background_LightDark})`,
                backgroundSize: "cover",
                backgroundPosition: "center",
            }}
        >
            {/* Dark glass overlay */}
            <Box
                style={{
                    position: "fixed",
                    inset: 0,
                    background: "rgba(0,0,0,0.55)",
                    backdropFilter: "blur(10px)",
                    zIndex: 1,
                }}
            />

            {/* Spotlight vignette */}
            <Box
                style={{
                    position: "fixed",
                    inset: 0,
                    background:
                        "radial-gradient(ellipse at center, rgba(0,0,0,0.05) 0%, rgba(0,0,0,0.45) 60%, rgba(0,0,0,0.75) 100%)",
                    zIndex: 2,
                }}
            />

            {/* Left character silhouette */}
            <Box
                style={{
                    position: "fixed",
                    bottom: 0,
                    left: "-2%",
                    width: "28%",
                    height: "85%",
                    backgroundImage: `url(${saber})`,
                    backgroundSize: "contain",
                    backgroundPosition: "bottom center",
                    backgroundRepeat: "no-repeat",
                    opacity: 0.15,
                    filter: "brightness(0.6) contrast(1.2)",
                    zIndex: 2,
                    pointerEvents: "none",
                }}
            />

            {/* Right character silhouette */}
            <Box
                style={{
                    position: "fixed",
                    bottom: 0,
                    right: "-2%",
                    width: "28%",
                    height: "85%",
                    backgroundImage: `url(${archer})`,
                    backgroundSize: "contain",
                    backgroundPosition: "bottom center",
                    backgroundRepeat: "no-repeat",
                    opacity: 0.15,
                    filter: "brightness(0.6) contrast(1.2)",
                    zIndex: 2,
                    pointerEvents: "none",
                    transform: "scaleX(-1)",
                }}
            />

            {/* Main content */}
            <Center style={{ height: "100%", position: "relative", zIndex: 3, padding: "0 24px" }}>
                <Box
                    style={{
                        position: "relative",
                        width: "min(680px, 90vw)",
                        maxHeight: "min(88vh, 700px)",
                        borderRadius: 18,
                        overflow: "hidden",
                        boxShadow: "0 30px 90px rgba(0,0,0,0.75)",
                        border: "1px solid rgba(255,255,255,0.10)",
                        background: "rgba(10, 12, 16, 0.72)",
                        backdropFilter: "blur(16px)",
                        animation: "floatIn 0.5s ease-out",
                        display: "flex",
                        flexDirection: "column",
                    }}
                >
                    <CloseButton onClick={() => navigate("/login")} top={14} right={14} size={32} />

                    {/* ── Header ── */}
                    <Box
                        style={{
                            padding: "28px 32px 20px",
                            borderBottom: "1px solid rgba(250,176,5,0.15)",
                            background: "linear-gradient(180deg, rgba(250,176,5,0.06) 0%, transparent 100%)",
                            flexShrink: 0,
                        }}
                    >
                        <Title
                            order={2}
                            ta="center"
                            style={{
                                color: "rgba(235,235,235,0.95)",
                                letterSpacing: 3,
                                textTransform: "uppercase",
                                textShadow: "0 3px 18px rgba(0,0,0,0.9)",
                                fontSize: "clamp(1.2rem, 3vw, 1.6rem)",
                            }}
                        >
                            WAITING ROOM
                        </Title>
                        <Text
                            ta="center"
                            size="xs"
                            style={{
                                color: "rgba(230,230,230,0.45)",
                                letterSpacing: 2,
                                textTransform: "uppercase",
                                marginTop: 4,
                            }}
                        >
                            Prepare for battle
                        </Text>
                    </Box>

                    {/* ── Room Info Bar ── */}
                    <Box
                        style={{
                            padding: "14px 32px",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                            flexWrap: "wrap",
                            gap: 12,
                            borderBottom: "1px solid rgba(255,255,255,0.06)",
                            flexShrink: 0,
                        }}
                    >
                        <Group gap="lg">
                            <Box>
                                <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>
                                    Room
                                </Text>
                                <Text size="sm" fw={600} style={{ color: "rgba(250,176,5,0.9)", fontFamily: "monospace" }}>
                                    {roomState.roomId}
                                </Text>
                            </Box>
                            <Box>
                                <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>
                                    Players
                                </Text>
                                <Text size="sm" fw={600} style={{ color: "rgba(245,245,245,0.9)" }}>
                                    {roomState.players.length} / {roomState.maxPlayers}
                                </Text>
                            </Box>
                            <Box>
                                <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>
                                    Ready
                                </Text>
                                <Text size="sm" fw={600} style={{ color: readyCount === roomState.players.length && roomState.players.length > 0 ? "rgba(100,255,100,0.9)" : "rgba(245,245,245,0.9)" }}>
                                    {readyCount} / {roomState.players.length}
                                </Text>
                            </Box>
                        </Group>

                        <Tooltip label={copied ? "Copied!" : "Copy invite link"} withArrow>
                            <Button
                                variant="subtle"
                                size="xs"
                                onClick={copyRoomLink}
                                styles={{
                                    root: {
                                        color: "rgba(250,176,5,0.85)",
                                        border: "1px solid rgba(250,176,5,0.25)",
                                        background: "rgba(250,176,5,0.06)",
                                        letterSpacing: 1,
                                        textTransform: "uppercase" as const,
                                        fontWeight: 600,
                                        fontSize: 11,
                                        transition: "all 0.2s ease",
                                        "&:hover": {
                                            background: "rgba(250,176,5,0.12)",
                                        },
                                    },
                                }}
                            >
                                {copied ? "COPIED!" : "COPY LINK"}
                            </Button>
                        </Tooltip>
                    </Box>

                    {/* ── Player List ── */}
                    <Box
                        style={{
                            padding: "16px 28px",
                            flex: 1,
                            overflowY: "auto",
                            display: "flex",
                            flexDirection: "column",
                            gap: 10,
                        }}
                    >
                        {roomState.players.length === 0 && (
                            <Center style={{ flex: 1 }}>
                                <Text size="sm" style={{ color: "rgba(230,230,230,0.35)", letterSpacing: 1 }}>
                                    No players yet...
                                </Text>
                            </Center>
                        )}

                        {roomState.players.map((p, i) => {
                            const isYou = p.id === roomState.you?.id;
                            return (
                                <Box
                                    key={p.id}
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "space-between",
                                        padding: "14px 20px",
                                        borderRadius: 12,
                                        background: isYou
                                            ? "rgba(250,176,5,0.06)"
                                            : "rgba(255,255,255,0.03)",
                                        border: p.isReady
                                            ? "1px solid rgba(250,176,5,0.35)"
                                            : "1px solid rgba(255,255,255,0.08)",
                                        transition: "all 0.3s ease",
                                        animation: p.isReady ? "readyGlow 2s ease-in-out infinite" : "none",
                                        animationDelay: `${i * 0.15}s`,
                                    }}
                                >
                                    <Group gap="md">
                                        {/* Player avatar circle */}
                                        <Box
                                            style={{
                                                width: 40,
                                                height: 40,
                                                borderRadius: "50%",
                                                background: p.isHost
                                                    ? "linear-gradient(135deg, rgba(250,176,5,0.7), rgba(200,130,20,0.7))"
                                                    : "linear-gradient(135deg, rgba(112,72,232,0.5), rgba(80,50,180,0.5))",
                                                border: p.isReady
                                                    ? "2px solid rgba(250,176,5,0.6)"
                                                    : "2px solid rgba(255,255,255,0.12)",
                                                display: "grid",
                                                placeItems: "center",
                                                flexShrink: 0,
                                                transition: "border-color 0.3s ease",
                                            }}
                                        >
                                            <Text size="sm" fw={700} style={{ color: "rgba(255,255,255,0.9)" }}>
                                                {p.name.charAt(0).toUpperCase()}
                                            </Text>
                                        </Box>

                                        <Box>
                                            <Group gap={8} align="center">
                                                <Text size="sm" fw={600} style={{ color: "rgba(245,245,245,0.95)" }}>
                                                    {p.name}
                                                </Text>
                                                {p.isHost && (
                                                    <Box
                                                        style={{
                                                            padding: "1px 8px",
                                                            borderRadius: 4,
                                                            background: "rgba(250,176,5,0.15)",
                                                            border: "1px solid rgba(250,176,5,0.3)",
                                                        }}
                                                    >
                                                        <Text size="xs" fw={700} style={{ color: "rgba(250,176,5,0.9)", fontSize: 10, letterSpacing: 1 }}>
                                                            HOST
                                                        </Text>
                                                    </Box>
                                                )}
                                                {isYou && (
                                                    <Text size="xs" style={{ color: "rgba(230,230,230,0.35)", fontSize: 10 }}>
                                                        (you)
                                                    </Text>
                                                )}
                                            </Group>
                                            <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", marginTop: 2 }}>
                                                {p.minions?.length ? p.minions.join(", ") : "No minions selected"}
                                            </Text>
                                        </Box>
                                    </Group>

                                    {/* Ready status indicator */}
                                    <Box
                                        style={{
                                            padding: "4px 14px",
                                            borderRadius: 6,
                                            background: p.isReady
                                                ? "rgba(100,255,100,0.1)"
                                                : "rgba(255,255,255,0.04)",
                                            border: p.isReady
                                                ? "1px solid rgba(100,255,100,0.25)"
                                                : "1px solid rgba(255,255,255,0.08)",
                                            transition: "all 0.3s ease",
                                        }}
                                    >
                                        <Text
                                            size="xs"
                                            fw={700}
                                            style={{
                                                color: p.isReady ? "rgba(100,255,100,0.9)" : "rgba(230,230,230,0.4)",
                                                letterSpacing: 1,
                                                textTransform: "uppercase",
                                                fontSize: 11,
                                            }}
                                        >
                                            {p.isReady ? "READY" : "WAITING"}
                                        </Text>
                                    </Box>
                                </Box>
                            );
                        })}
                    </Box>

                    {/* ── Bottom Action Bar ── */}
                    <Box
                        style={{
                            padding: "18px 32px 22px",
                            borderTop: "1px solid rgba(255,255,255,0.06)",
                            background: "linear-gradient(0deg, rgba(0,0,0,0.25) 0%, transparent 100%)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            gap: 14,
                            flexWrap: "wrap",
                            flexShrink: 0,
                        }}
                    >
                        {/* Ready / Unready button */}
                        <Button
                            size="md"
                            radius="md"
                            onClick={toggleReady}
                            styles={{
                                root: {
                                    minWidth: 160,
                                    height: 46,
                                    letterSpacing: 3,
                                    textTransform: "uppercase" as const,
                                    fontWeight: 700,
                                    fontSize: 14,
                                    background: you?.isReady
                                        ? "linear-gradient(180deg, rgba(100,100,100,0.8), rgba(60,60,60,0.8))"
                                        : "linear-gradient(180deg, rgba(210,145,80,1) 0%, rgba(120,70,35,1) 100%)",
                                    border: you?.isReady
                                        ? "1px solid rgba(255,255,255,0.15)"
                                        : "1px solid rgba(255,215,170,0.18)",
                                    boxShadow: "0 10px 30px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(0,0,0,0.3)",
                                    transition: "all 0.25s ease",
                                },
                            }}
                        >
                            {you?.isReady ? "UNREADY" : "READY"}
                        </Button>

                        {/* Host: Start Game */}
                        {isHost && (
                            <Button
                                size="md"
                                radius="md"
                                onClick={startGame}
                                disabled={!allReady || roomState.players.length < 2}
                                styles={{
                                    root: {
                                        minWidth: 180,
                                        height: 46,
                                        letterSpacing: 3,
                                        textTransform: "uppercase" as const,
                                        fontWeight: 700,
                                        fontSize: 14,
                                        background: "linear-gradient(180deg, rgba(112,72,232,1) 0%, rgba(70,40,160,1) 100%)",
                                        border: "1px solid rgba(160,120,255,0.25)",
                                        boxShadow: "0 10px 30px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(0,0,0,0.3)",
                                        opacity: (!allReady || roomState.players.length < 2) ? 0.45 : 1,
                                        filter: (!allReady || roomState.players.length < 2) ? "grayscale(0.3) brightness(0.8)" : "none",
                                        cursor: (!allReady || roomState.players.length < 2) ? "not-allowed" : "pointer",
                                        transition: "all 0.25s ease",
                                    },
                                }}
                            >
                                START GAME
                            </Button>
                        )}

                        {/* Non-host: Waiting message */}
                        {!isHost && (
                            <Text
                                size="sm"
                                style={{
                                    color: "rgba(230,230,230,0.45)",
                                    letterSpacing: 1,
                                    fontStyle: "italic",
                                }}
                            >
                                Waiting for host to start
                                <span style={{ animation: "dotPulse 1.4s infinite", animationDelay: "0s" }}>.</span>
                                <span style={{ animation: "dotPulse 1.4s infinite", animationDelay: "0.2s" }}>.</span>
                                <span style={{ animation: "dotPulse 1.4s infinite", animationDelay: "0.4s" }}>.</span>
                            </Text>
                        )}
                    </Box>
                </Box>
            </Center>
        </Box>
    );
}
