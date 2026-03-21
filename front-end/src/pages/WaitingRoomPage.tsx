import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { Box, Center, Title, Text, Button, Stack, Group, Paper, Loader, Tooltip, TextInput } from "@mantine/core";
import { FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import { faBookOpen } from "@fortawesome/free-solid-svg-icons";

import background_LightDark from "../assets/background_LightDark.png";
import CloseButton from "../components/CloseButton";
import SpectatorButton from "../components/SpectatorButton";
import ManualWaitingPage from "./ManualWaitingPage.tsx";

import type { RoomState } from "../type/RoomState.tsx"

export default function WaitingRoomPage() {
    const { roomId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();

    const created = (location.state as any)?.created === true;
    const joining = (location.state as any)?.joining === true;

    const stateUser = (location.state as any)?.user as string | undefined;
    const alreadyJoined = !joining && !!sessionStorage.getItem(`playerId_${roomId}`);
    const [userName, setUserName] = useState<string>(stateUser ?? localStorage.getItem("username") ?? "");
    const [nameInput, setNameInput] = useState("");
    const [nameSubmitted, setNameSubmitted] = useState(!!stateUser || created || alreadyJoined);
    const needsName = !nameSubmitted;

    const [roomState, setRoomState] = useState<RoomState | null>(null);
    const [loading, setLoading] = useState(!needsName);
    const [error, setError] = useState<string | null>(null);
    const [copied, setCopied] = useState(false);
    const [joinRoomInput, setJoinRoomInput] = useState("");
    const [showManual, setShowManual] = useState(false);

    const storageKey = `minions_${roomId}`;
    const [selectedMinions, setSelectedMinions] = useState<{ type: string; strategy: string }[]>(() => {
        try {
            const saved = localStorage.getItem(storageKey);
            return saved ? JSON.parse(saved) : [];
        } catch { return []; }
    });

    useEffect(() => {
        try {
            const saved = localStorage.getItem(storageKey);
            if (saved) setSelectedMinions(JSON.parse(saved));
        } catch { /* ignore */ }
    }, [storageKey, location.key]);

    const hasMinions = selectedMinions.length > 0;

    const playerIdKey = `playerId_${roomId}`;
    const playerIdRef = useRef<string | null>(null);
    const leavingRoomRef = useRef(true);

    const playerId = useMemo(() => {
        if (roomState?.you?.id) {
            return roomState.you.id;
        }
        return sessionStorage.getItem(playerIdKey);
    }, [roomState?.you?.id, playerIdKey]);

    useEffect(() => {
        if (playerId) {
            sessionStorage.setItem(playerIdKey, playerId);
            playerIdRef.current = playerId;
        }
    }, [playerId, playerIdKey]);

    const you = useMemo(() => {
        if (!playerId) return null;
        return roomState?.players.find((p) => p.id === playerId) ?? null;
    }, [roomState, playerId]);

    useEffect(() => {
        if (!roomId) {
            setError("Missing roomId in URL");
            setLoading(false);
        }
    }, [roomId]);

    useEffect(() => {
        if (!roomId) return;
        if (needsName) return;

        localStorage.setItem("username", userName);
        let cancelled = false;

        async function loadRoom() {
            const roomRes = await fetch(`/api/room/${roomId}`);
            if (!roomRes.ok) throw new Error(`Failed to load room (${roomRes.status})`);
            const data: RoomState = await roomRes.json();
            if (!cancelled) setRoomState(data);
        }

        async function joinThenLoad() {
            const existingId = sessionStorage.getItem(`playerId_${roomId}`);
            if (existingId) {
                playerIdRef.current = existingId;
                await loadRoom();
                return;
            }
            const res = await fetch(`/api/room/${roomId}/join`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: userName }),
            });
            if (!res.ok) throw new Error(`Join failed (${res.status})`);
            const joined: RoomState = await res.json();
            if (joined.you?.id) {
                sessionStorage.setItem(`playerId_${roomId}`, joined.you.id);
                playerIdRef.current = joined.you.id;
            }
            if (!cancelled) setRoomState(joined);
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
    }, [roomId, userName, created, needsName]);

    const leaveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    useEffect(() => {
        if (!roomId) return;
        leavingRoomRef.current = true;

        if (leaveTimerRef.current) {
            clearTimeout(leaveTimerRef.current);
            leaveTimerRef.current = null;
        }

        const sendLeave = () => {
            const id = playerIdRef.current;
            if (!id) return;
            navigator.sendBeacon(
                `/api/room/${roomId}/leave`,
                new Blob([JSON.stringify({ playerId: id })], { type: "application/json" })
            );
            sessionStorage.removeItem(`playerId_${roomId}`);
        };

        window.addEventListener("beforeunload", sendLeave);
        return () => {
            window.removeEventListener("beforeunload", sendLeave);
            if (leavingRoomRef.current) {
                leaveTimerRef.current = setTimeout(() => {
                    sendLeave();
                    leaveTimerRef.current = null;
                }, 200);
            }
        };
    }, [roomId]);

    useEffect(() => {
        if (!roomId) return;

        const client = new Client({
            webSocketFactory: () => new SockJS(`${window.location.origin}/ws`),
            reconnectDelay: 2000,
            connectHeaders: {
                roomId: roomId,
                playerId: playerIdRef.current ?? sessionStorage.getItem(`playerId_${roomId}`) ?? "",
            },
            onConnect: () => {
                const latestId = playerIdRef.current ?? sessionStorage.getItem(`playerId_${roomId}`) ?? "";
                if (latestId && client.connected) {
                    client.publish({
                        destination: "/app/room/register",
                        body: JSON.stringify({ roomId, playerId: latestId }),
                    });
                }
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

    useEffect(() => {
        if (!roomState || !roomId) return;
        if (roomState.state === "in_game") {
            navigate(`/battle/${roomId}`, { replace: true });
        }
    }, [roomState, roomId, navigate]);

    async function toggleReady() {
        if (!roomId || !playerId) return;
        await fetch(`/api/room/${roomId}/ready`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ playerId }),
        });
    }

    async function startGame() {
        if (!roomId || !playerId) return;
        await fetch(`/api/room/${roomId}/start`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ playerId }),
        });
    }

    async function kickPlayer(targetId: string) {
        if (!roomId || !playerId) return;
        try {
            await fetch(`/api/room/${roomId}/kick`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ hostId: playerId, targetId }),
            });
        } catch (e) {
            console.error("Failed to kick player", e);
        }
    }

    async function addBot() {
        if (!roomId || !playerId) return;
        try {
            await fetch(`/api/room/${roomId}/add-bot`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ hostId: playerId }),
            });
        } catch (e) {
            console.error("Failed to add bot", e);
        }
    }

    function copyRoomLink() {
        const link = `${window.location.origin}/waitingRoom/${roomId}`;
        if (navigator.clipboard?.writeText) {
            navigator.clipboard
                .writeText(link)
                .then(() => {
                    setCopied(true);
                    setTimeout(() => setCopied(false), 2000);
                })
                .catch(() => fallbackCopy(link));
        } else {
            fallbackCopy(link);
        }
    }

    function fallbackCopy(text: string) {
        const ta = document.createElement("textarea");
        ta.value = text;
        ta.style.position = "fixed";
        ta.style.opacity = "0";
        document.body.appendChild(ta);
        ta.select();
        try {
            document.execCommand("copy");
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch {
            prompt("Copy this link:", text);
        }
        document.body.removeChild(ta);
    }

    // ── Join by Room ID ──
    const [roomIdInput, setRoomIdInput] = useState("");

    if (!roomId) {
        return (
            <Box style={{ height: "100dvh", width: "100%", overflow: "hidden", position: "relative", backgroundImage: `url(${background_LightDark})`, backgroundSize: "cover", backgroundPosition: "center" }}>
                <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", backdropFilter: "blur(12px)", zIndex: 1 }} />
                <Center style={{ height: "100%", position: "relative", zIndex: 2 }}>
                    <Paper style={{ background: "rgba(10,12,16,0.80)", backdropFilter: "blur(8px)", border: "1px solid rgba(255,255,255,0.10)", padding: "40px 48px", borderRadius: 16, width: "min(380px, 90vw)" }}>
                        <Stack gap="lg">
                            <Title order={3} ta="center" style={{ color: "rgba(235,235,235,0.95)", letterSpacing: 3, textTransform: "uppercase" }}>JOIN ROOM</Title>
                            <Text size="xs" ta="center" style={{ color: "rgba(230,230,230,0.45)", letterSpacing: 1 }}>Enter the room ID to join</Text>
                            <TextInput
                                placeholder="Room ID" value={roomIdInput} onChange={(e) => setRoomIdInput(e.currentTarget.value)}
                                onKeyDown={(e) => { if (e.key === "Enter" && roomIdInput.trim().length > 0) navigate(`/waitingRoom/${roomIdInput.trim()}`); }}
                                styles={{ input: { height: 44, backgroundColor: "rgba(255,255,255,0.06)", border: "1px solid rgba(255,255,255,0.14)", color: "rgba(250,176,5,0.95)", fontFamily: "monospace", fontSize: 16, textAlign: "center", letterSpacing: 3 } }}
                            />
                            <Button size="md" radius="md" fullWidth disabled={roomIdInput.trim().length === 0} onClick={() => navigate(`/waitingRoom/${roomIdInput.trim()}`)} styles={{ root: { height: 46, letterSpacing: 3, textTransform: "uppercase" as const, fontWeight: 700, background: "linear-gradient(180deg, rgba(210,145,80,1) 0%, rgba(120,70,35,1) 100%)", border: "1px solid rgba(255,215,170,0.18)", opacity: roomIdInput.trim().length === 0 ? 0.5 : 1 } }}>JOIN</Button>
                            <Button variant="subtle" size="sm" fullWidth onClick={() => navigate("/login")} styles={{ root: { color: "rgba(230,230,230,0.45)", letterSpacing: 1, textTransform: "uppercase" as const, fontSize: 11 } }}>BACK TO LOGIN</Button>
                        </Stack>
                    </Paper>
                </Center>
            </Box>
        );
    }

    // ── Name input ──
    if (needsName) {
        return (
            <Box style={{ height: "100dvh", width: "100%", overflow: "hidden", position: "relative", backgroundImage: `url(${background_LightDark})`, backgroundSize: "cover", backgroundPosition: "center" }}>
                <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", backdropFilter: "blur(12px)", zIndex: 1 }} />
                <Center style={{ height: "100%", position: "relative", zIndex: 2 }}>
                    <Paper style={{ background: "rgba(10,12,16,0.80)", backdropFilter: "blur(8px)", border: "1px solid rgba(255,255,255,0.10)", padding: "40px 48px", borderRadius: 16, width: "min(380px, 90vw)" }}>
                        <Stack gap="lg">
                            <Title order={3} ta="center" style={{ color: "rgba(235,235,235,0.95)", letterSpacing: 3, textTransform: "uppercase" }}>JOIN ROOM</Title>
                            <Text size="xs" ta="center" style={{ color: "rgba(230,230,230,0.45)", letterSpacing: 1 }}>Room: <span style={{ color: "rgba(250,176,5,0.9)", fontFamily: "monospace" }}>{roomId}</span></Text>
                            <TextInput
                                placeholder="Enter your name" value={nameInput} onChange={(e) => setNameInput(e.currentTarget.value)}
                                onKeyDown={(e) => {
                                    if (e.key === "Enter" && nameInput.trim().length >= 3) {
                                        localStorage.setItem("username", nameInput.trim());
                                        setUserName(nameInput.trim());
                                        setNameSubmitted(true);
                                        setLoading(true);
                                    }
                                }}
                                styles={{ input: { height: 44, backgroundColor: "rgba(255,255,255,0.06)", border: "1px solid rgba(255,255,255,0.14)", color: "rgba(245,245,245,0.95)" } }}
                            />
                            <Button size="md" radius="md" fullWidth disabled={nameInput.trim().length < 3} onClick={() => { localStorage.setItem("username", nameInput.trim()); setUserName(nameInput.trim()); setNameSubmitted(true); setLoading(true); }} styles={{ root: { height: 46, letterSpacing: 3, textTransform: "uppercase" as const, fontWeight: 700, background: "linear-gradient(180deg, rgba(210,145,80,1) 0%, rgba(120,70,35,1) 100%)", border: "1px solid rgba(255,215,170,0.18)", opacity: nameInput.trim().length < 3 ? 0.5 : 1 } }}>ENTER</Button>
                        </Stack>
                    </Paper>
                </Center>
            </Box>
        );
    }

    if (loading) {
        return (
            <Box style={{ height: "100dvh", width: "100%", overflow: "hidden", position: "relative", backgroundImage: `url(${background_LightDark})`, backgroundSize: "cover", backgroundPosition: "center" }}>
                <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", backdropFilter: "blur(12px)", zIndex: 1 }} />
                <Center style={{ height: "100%", position: "relative", zIndex: 2 }}>
                    <Stack align="center" gap="lg">
                        <Loader color="#FAB005" size="lg" type="dots" />
                        <Text size="lg" fw={600} style={{ color: "rgba(210,190,140,0.95)", letterSpacing: 3, textTransform: "uppercase" }}>Entering Room...</Text>
                    </Stack>
                </Center>
            </Box>
        );
    }

    if (error) {
        return (
            <Box style={{ height: "100dvh", width: "100%", overflow: "hidden", position: "relative", backgroundImage: `url(${background_LightDark})`, backgroundSize: "cover", backgroundPosition: "center" }}>
                <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", backdropFilter: "blur(12px)", zIndex: 1 }} />
                <Center style={{ height: "100%", position: "relative", zIndex: 2 }}>
                    <Paper style={{ background: "rgba(10,12,16,0.75)", backdropFilter: "blur(8px)", border: "1px solid rgba(170,35,35,0.4)", padding: "40px 48px", borderRadius: 16, textAlign: "center", maxWidth: 420 }}>
                        <Text size="xl" fw={700} style={{ color: "rgba(255,100,100,0.95)", marginBottom: 12 }}>ERROR</Text>
                        <Text size="sm" style={{ color: "rgba(245,245,245,0.8)", marginBottom: 24 }}>{error}</Text>
                        <Button onClick={() => navigate("/login")} styles={{ root: { background: "linear-gradient(180deg, rgba(170,35,35,1), rgba(90,10,10,1))", border: "1px solid rgba(255,100,100,0.2)", letterSpacing: 2, textTransform: "uppercase" as const } }}>Back to Login</Button>
                    </Paper>
                </Center>
            </Box>
        );
    }

    if (!roomState) return null;

    // ── แยกข้อมูลผู้เล่นและผู้ชม ──
    const activePlayers = roomState.players.filter((p: any) => !p.isSpectator);
    const spectators = roomState.players.filter((p: any) => p.isSpectator);

    const readyCount = activePlayers.filter((p) => p.isReady).length;
    const allReady = activePlayers.length > 0 && activePlayers.every((p) => (p.isHost ? true : p.isReady));
    const isHost = !!you?.isHost;
    const isYouSpectator = !!(you as any)?.isSpectator;

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
            <Box style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.55)", backdropFilter: "blur(10px)", zIndex: 1 }} />
            <Box style={{ position: "fixed", inset: 0, background: "radial-gradient(ellipse at center, rgba(0,0,0,0.05) 0%, rgba(0,0,0,0.45) 60%, rgba(0,0,0,0.75) 100%)", zIndex: 2 }} />

            <Center style={{ height: "100%", position: "relative", zIndex: 3, padding: "0 24px" }}>
                {/* ── Group ควบคุมการวางตัวของหน้าต่างหลัก และหน้าต่าง Spectator ── */}
                <Group align="stretch" justify="center" gap="xl" style={{ width: "100%" }}>

                    {/* ── Main content (Players) ── */}
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

                        {/* Manual / Guide Button — top-left, opposite of CloseButton */}
                        <Box
                            component="button"
                            type="button"
                            onClick={() => setShowManual(true)}
                            style={{
                                position: "absolute",
                                top: 14,
                                left: 14,
                                width: 32,
                                height: 32,
                                borderRadius: 10,
                                background: "none",
                                border: "none",
                                boxShadow: "none",
                                outline: "none",
                                cursor: "pointer",
                                zIndex: 50,
                                display: "grid",
                                placeItems: "center",
                                padding: 0,
                                fontSize: 20,
                                color: "rgba(255,255,255,255)",
                                fontWeight: 700,
                                transition: "all 0.2s ease",
                            }}
                            aria-label="Manual"
                            title="Game Manual"
                        >
                            <FontAwesomeIcon icon={faBookOpen} />
                        </Box>

                        <Box style={{ padding: "28px 32px 20px", borderBottom: "1px solid rgba(250,176,5,0.15)", background: "linear-gradient(180deg, rgba(250,176,5,0.06) 0%, transparent 100%)", flexShrink: 0 }}>
                            <Title order={2} ta="center" style={{ color: "rgba(235,235,235,0.95)", letterSpacing: 3, textTransform: "uppercase", textShadow: "0 3px 18px rgba(0,0,0,0.9)", fontSize: "clamp(1.2rem, 3vw, 1.6rem)" }}>
                                WAITING ROOM
                            </Title>
                            <Text ta="center" size="xs" style={{ color: "rgba(230,230,230,0.45)", letterSpacing: 2, textTransform: "uppercase", marginTop: 4 }}>
                                Prepare for battle
                            </Text>
                        </Box>

                        <Box style={{ padding: "14px 32px", display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: 12, borderBottom: "1px solid rgba(255,255,255,0.06)", flexShrink: 0 }}>
                            <Group gap="lg">
                                <Box>
                                    <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>Room</Text>
                                    <Text size="sm" fw={600} style={{ color: "rgba(250,176,5,0.9)", fontFamily: "monospace" }}>{roomState.roomId}</Text>
                                </Box>
                                <Box>
                                    <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>Players</Text>
                                    <Text size="sm" fw={600} style={{ color: "rgba(245,245,245,0.9)" }}>{activePlayers.length} / {roomState.maxPlayers}</Text>
                                </Box>
                                <Box>
                                    <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>Ready</Text>
                                    <Text size="sm" fw={600} style={{ color: readyCount === activePlayers.length && activePlayers.length > 0 ? "rgba(100,255,100,0.9)" : "rgba(245,245,245,0.9)" }}>{readyCount} / {activePlayers.length}</Text>
                                </Box>
                            </Group>

                            <Tooltip label={copied ? "Copied!" : "Copy invite link"} withArrow>
                                <Button variant="subtle" size="xs" onClick={copyRoomLink} styles={{ root: { color: "rgba(250,176,5,0.85)", border: "1px solid rgba(250,176,5,0.25)", background: "rgba(250,176,5,0.06)", letterSpacing: 1, textTransform: "uppercase" as const, fontWeight: 600, fontSize: 11, transition: "all 0.2s ease", "&:hover": { background: "rgba(250,176,5,0.12)" } } }}>
                                    {copied ? "COPIED!" : "COPY LINK"}
                                </Button>
                            </Tooltip>
                        </Box>

                        <Box style={{ padding: "10px 32px", display: "flex", alignItems: "center", gap: 8, borderBottom: "1px solid rgba(255,255,255,0.06)", flexShrink: 0 }}>
                            <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase", flexShrink: 0 }}>Join Room</Text>
                            <input
                                type="text" placeholder="Enter Room ID" value={joinRoomInput} onChange={(e) => setJoinRoomInput(e.target.value)}
                                onKeyDown={(e) => { if (e.key === "Enter" && joinRoomInput.trim()) navigate(`/waitingRoom/${joinRoomInput.trim()}`, { state: { joining: true } }); }}
                                style={{ height: 30, flex: 1, minWidth: 0, fontSize: 12, backgroundColor: "rgba(255,255,255,0.06)", border: "1px solid rgba(255,255,255,0.14)", color: "rgba(245,245,245,0.95)", fontFamily: "monospace", borderRadius: 6, padding: "0 10px", outline: "none" }}
                            />
                            <Button variant="subtle" size="xs" disabled={!joinRoomInput.trim()} onClick={() => navigate(`/waitingRoom/${joinRoomInput.trim()}`, { state: { joining: true } })} styles={{ root: { color: "rgba(112,72,232,0.85)", border: "1px solid rgba(112,72,232,0.25)", background: "rgba(112,72,232,0.06)", letterSpacing: 1, textTransform: "uppercase" as const, fontWeight: 600, fontSize: 11, flexShrink: 0 } }}>JOIN</Button>
                        </Box>

                        <Box style={{ padding: "16px 28px", flex: 1, overflowY: "auto", display: "flex", flexDirection: "column", gap: 10 }}>
                            {activePlayers.length === 0 && (
                                <Center style={{ flex: 1 }}>
                                    <Text size="sm" style={{ color: "rgba(230,230,230,0.35)", letterSpacing: 1 }}>No players yet...</Text>
                                </Center>
                            )}

                            {activePlayers.map((p, i) => {
                                const isYou = p.id === playerId;
                                return (
                                    <Box key={p.id} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "14px 20px", borderRadius: 12, background: isYou ? "rgba(250,176,5,0.06)" : "rgba(255,255,255,0.03)", border: p.isReady ? "1px solid rgba(250,176,5,0.35)" : "1px solid rgba(255,255,255,0.08)", transition: "all 0.3s ease", animation: p.isReady ? "readyGlow 2s ease-in-out infinite" : "none", animationDelay: `${i * 0.15}s` }}>
                                        <Group gap="md">
                                            <Box style={{ width: 40, height: 40, borderRadius: "50%", background: p.isHost ? "linear-gradient(135deg, rgba(250,176,5,0.7), rgba(200,130,20,0.7))" : "linear-gradient(135deg, rgba(112,72,232,0.5), rgba(80,50,180,0.5))", border: p.isReady ? "2px solid rgba(250,176,5,0.6)" : "2px solid rgba(255,255,255,0.12)", display: "grid", placeItems: "center", flexShrink: 0, transition: "border-color 0.3s ease" }}>
                                                <Text size="sm" fw={700} style={{ color: "rgba(255,255,255,0.9)" }}>{p.name.charAt(0).toUpperCase()}</Text>
                                            </Box>
                                            <Box>
                                                <Group gap={8} align="center">
                                                    <Text size="sm" fw={600} style={{ color: "rgba(245,245,245,0.95)" }}>{p.name}</Text>
                                                    {p.isHost && (
                                                        <Box style={{ padding: "1px 8px", borderRadius: 4, background: "rgba(250,176,5,0.15)", border: "1px solid rgba(250,176,5,0.3)" }}>
                                                            <Text size="xs" fw={700} style={{ color: "rgba(250,176,5,0.9)", fontSize: 10, letterSpacing: 1 }}>HOST</Text>
                                                        </Box>
                                                    )}
                                                    {isYou && <Text size="xs" style={{ color: "rgba(230,230,230,0.35)", fontSize: 10 }}>(you)</Text>}
                                                </Group>
                                                <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", marginTop: 2 }}>{p.minions?.length ? p.minions.join(", ") : "No minions selected"}</Text>
                                            </Box>
                                        </Group>
                                        <Group gap={8}>
                                            <Box style={{ padding: "4px 14px", borderRadius: 6, background: p.isReady ? "rgba(100,255,100,0.1)" : "rgba(255,255,255,0.04)", border: p.isReady ? "1px solid rgba(100,255,100,0.25)" : "1px solid rgba(255,255,255,0.08)", transition: "all 0.3s ease" }}>
                                                <Text size="xs" fw={700} style={{ color: p.isReady ? "rgba(100,255,100,0.9)" : "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase", fontSize: 11 }}>{p.isReady ? "READY" : "WAITING"}</Text>
                                            </Box>
                                            {isHost && !isYou && (
                                                <Box
                                                    component="button"
                                                    onClick={() => kickPlayer(p.id)}
                                                    style={{
                                                        padding: "4px 10px",
                                                        borderRadius: 6,
                                                        background: "rgba(255,80,80,0.1)",
                                                        border: "1px solid rgba(255,80,80,0.25)",
                                                        cursor: "pointer",
                                                        transition: "all 0.2s ease",
                                                        outline: "none",
                                                    }}
                                                    onMouseEnter={(e: any) => { e.currentTarget.style.background = "rgba(255,80,80,0.25)"; e.currentTarget.style.border = "1px solid rgba(255,80,80,0.5)"; }}
                                                    onMouseLeave={(e: any) => { e.currentTarget.style.background = "rgba(255,80,80,0.1)"; e.currentTarget.style.border = "1px solid rgba(255,80,80,0.25)"; }}
                                                    title={`Kick ${p.name}`}
                                                >
                                                    <Text size="xs" fw={700} style={{ color: "rgba(255,80,80,0.9)", letterSpacing: 1, textTransform: "uppercase", fontSize: 10 }}>KICK</Text>
                                                </Box>
                                            )}
                                        </Group>
                                    </Box>
                                );
                            })}

                            {isHost && activePlayers.length < roomState.maxPlayers && (
                                <Box component="button" onClick={addBot} style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "14px 20px", borderRadius: 12, background: "rgba(255,255,255,0.015)", border: "1px dashed rgba(255,255,255,0.15)", cursor: "pointer", transition: "all 0.2s ease", width: "100%", minHeight: 70 }} onMouseEnter={(e: any) => { e.currentTarget.style.background = "rgba(255,255,255,0.04)"; e.currentTarget.style.border = "1px dashed rgba(250,176,5,0.4)"; }} onMouseLeave={(e: any) => { e.currentTarget.style.background = "rgba(255,255,255,0.015)"; e.currentTarget.style.border = "1px dashed rgba(255,255,255,0.15)"; }}>
                                    <Group gap="sm">
                                        <Text size="lg" style={{ color: "rgba(250,176,5,0.7)", fontWeight: 300 }}>+</Text>
                                        <Text size="sm" fw={600} style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 2, textTransform: "uppercase" }}>ADD BOT</Text>
                                    </Group>
                                </Box>
                            )}
                        </Box>

                        <Box style={{ padding: "12px 28px", borderTop: "1px solid rgba(255,255,255,0.06)", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, flexShrink: 0, background: hasMinions ? "rgba(250,176,5,0.04)" : "rgba(255,100,100,0.04)" }}>
                            <Box>
                                <Text size="xs" style={{ color: "rgba(230,230,230,0.4)", letterSpacing: 1, textTransform: "uppercase" }}>Your Minions</Text>
                                {hasMinions ? <Text size="sm" fw={600} style={{ color: "rgba(250,176,5,0.9)", marginTop: 2 }}>{selectedMinions.map(m => m.type).join(", ")}</Text> : <Text size="sm" style={{ color: "rgba(255,100,100,0.7)", marginTop: 2 }}>No minions selected — select before readying up</Text>}
                            </Box>
                            <Button variant="subtle" size="xs" onClick={() => { leavingRoomRef.current = false; navigate("/select", { state: { roomId, fromRoom: true } }); }} styles={{ root: { color: hasMinions ? "rgba(230,230,230,0.7)" : "rgba(250,176,5,0.85)", border: hasMinions ? "1px solid rgba(255,255,255,0.12)" : "1px solid rgba(250,176,5,0.3)", background: hasMinions ? "rgba(255,255,255,0.04)" : "rgba(250,176,5,0.08)", letterSpacing: 1, textTransform: "uppercase" as const, fontWeight: 600, fontSize: 11 } }}>
                                {hasMinions ? "CHANGE" : "SELECT MINIONS"}
                            </Button>
                        </Box>

                        <Box style={{ padding: "18px 32px 22px", borderTop: "1px solid rgba(255,255,255,0.06)", background: "linear-gradient(0deg, rgba(0,0,0,0.25) 0%, transparent 100%)", display: "flex", alignItems: "center", justifyContent: "center", gap: 14, flexWrap: "wrap", flexShrink: 0 }}>

                            {you && (
                                <SpectatorButton roomId={roomId} playerId={you.id} isSpectator={isYouSpectator} />
                            )}

                            {!isHost && !isYouSpectator && (
                                <Tooltip label="Select minions first" disabled={hasMinions} withArrow>
                                    <Button size="md" radius="md" onClick={toggleReady} disabled={!hasMinions && !you?.isReady} styles={{ root: { minWidth: 160, height: 46, letterSpacing: 3, textTransform: "uppercase" as const, fontWeight: 700, fontSize: 14, background: you?.isReady ? "linear-gradient(180deg, rgba(100,100,100,0.8), rgba(60,60,60,0.8))" : "linear-gradient(180deg, rgba(210,145,80,1) 0%, rgba(120,70,35,1) 100%)", border: you?.isReady ? "1px solid rgba(255,255,255,0.15)" : "1px solid rgba(255,215,170,0.18)", boxShadow: "0 10px 30px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(0,0,0,0.3)", opacity: (!hasMinions && !you?.isReady) ? 0.45 : 1, filter: (!hasMinions && !you?.isReady) ? "grayscale(0.3) brightness(0.8)" : "none", cursor: (!hasMinions && !you?.isReady) ? "not-allowed" : "pointer", transition: "all 0.25s ease" } }}>
                                        {you?.isReady ? "UNREADY" : "READY"}
                                    </Button>
                                </Tooltip>
                            )}

                            {isHost && (
                                <Button size="md" radius="md" onClick={startGame} disabled={!allReady || activePlayers.length < 2} styles={{ root: { minWidth: 180, height: 46, letterSpacing: 3, textTransform: "uppercase" as const, fontWeight: 700, fontSize: 14, background: "linear-gradient(180deg, rgba(112,72,232,1) 0%, rgba(70,40,160,1) 100%)", border: "1px solid rgba(160,120,255,0.25)", boxShadow: "0 10px 30px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(0,0,0,0.3)", opacity: (!allReady || activePlayers.length < 2) ? 0.45 : 1, filter: (!allReady || activePlayers.length < 2) ? "grayscale(0.3) brightness(0.8)" : "none", cursor: (!allReady || activePlayers.length < 2) ? "not-allowed" : "pointer", transition: "all 0.25s ease" } }}>
                                    START GAME
                                </Button>
                            )}

                            {!isHost && activePlayers.length >= 0 && (
                                <Text size="sm" style={{ color: "rgba(230,230,230,0.45)", letterSpacing: 1, fontStyle: "italic" }}>
                                    Waiting for host to start<span style={{ animation: "dotPulse 1.4s infinite", animationDelay: "0s" }}>.</span><span style={{ animation: "dotPulse 1.4s infinite", animationDelay: "0.2s" }}>.</span><span style={{ animation: "dotPulse 1.4s infinite", animationDelay: "0.4s" }}>.</span>
                                </Text>
                            )}
                        </Box>
                    </Box>

                    {/* ── Side Modal (Spectators) ── */}
                    {(spectators.length > 0 || isYouSpectator) && (
                        <Box
                            style={{
                                width: "min(320px, 90vw)",
                                maxHeight: "min(88vh, 700px)",
                                borderRadius: 18,
                                overflow: "hidden",
                                boxShadow: "0 30px 90px rgba(0,0,0,0.5)",
                                border: "1px solid rgba(150,200,255,0.15)",
                                background: "rgba(10, 15, 25, 0.72)",
                                backdropFilter: "blur(16px)",
                                display: "flex",
                                flexDirection: "column",
                                animation: "slideInRight 0.4s cubic-bezier(0.16, 1, 0.3, 1)", // แอนิเมชันให้สไลด์เข้ามาจากด้านขวา
                            }}
                        >
                            <Box style={{ padding: "24px 24px 16px", borderBottom: "1px solid rgba(150,200,255,0.1)", background: "linear-gradient(180deg, rgba(80,160,255,0.06) 0%, transparent 100%)", flexShrink: 0 }}>
                                <Title order={4} ta="center" style={{ color: "rgba(235,245,255,0.9)", letterSpacing: 2, textTransform: "uppercase" }}>
                                    SPECTATORS
                                </Title>
                                <Text ta="center" size="xs" style={{ color: "rgba(150,200,255,0.5)", letterSpacing: 1, textTransform: "uppercase", marginTop: 4 }}>
                                    {spectators.length} Watching
                                </Text>
                            </Box>

                            <Box style={{ padding: "16px", flex: 1, overflowY: "auto", display: "flex", flexDirection: "column", gap: 10 }}>
                                {spectators.length === 0 ? (
                                    <Center style={{ flex: 1 }}>
                                        <Text size="sm" style={{ color: "rgba(150,200,255,0.3)", letterSpacing: 1 }}>It's quiet here...</Text>
                                    </Center>
                                ) : (
                                    spectators.map((s, i) => {
                                        const isYou = s.id === playerId;
                                        return (
                                            <Box key={s.id} style={{ display: "flex", alignItems: "center", gap: 12, padding: "10px 14px", borderRadius: 10, background: isYou ? "rgba(80,160,255,0.1)" : "rgba(255,255,255,0.02)", border: isYou ? "1px solid rgba(80,160,255,0.3)" : "1px solid rgba(255,255,255,0.05)" }}>
                                                <Box style={{ width: 32, height: 32, borderRadius: "50%", background: "linear-gradient(135deg, rgba(80,160,255,0.5), rgba(40,100,180,0.5))", border: "1px solid rgba(255,255,255,0.1)", display: "grid", placeItems: "center", flexShrink: 0 }}>
                                                    <Text size="xs" fw={700} style={{ color: "rgba(255,255,255,0.9)" }}>{s.name.charAt(0).toUpperCase()}</Text>
                                                </Box>
                                                <Box>
                                                    <Group gap={6} align="center">
                                                        <Text size="sm" fw={600} style={{ color: "rgba(235,245,255,0.9)" }}>{s.name}</Text>
                                                        {isYou && <Text size="xs" style={{ color: "rgba(150,200,255,0.6)", fontSize: 10 }}>(you)</Text>}
                                                    </Group>
                                                    <Text size="xs" style={{ color: "rgba(150,200,255,0.4)", marginTop: 2, fontSize: 10, textTransform: "uppercase", letterSpacing: 1 }}>
                                                        Observing
                                                    </Text>
                                                </Box>
                                            </Box>
                                        );
                                    })
                                )}
                            </Box>
                        </Box>
                    )}
                </Group>
            </Center>

            {showManual && <ManualWaitingPage onClose={() => setShowManual(false)} />}
        </Box>
    );
}