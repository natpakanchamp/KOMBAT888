// src/pages/LoginPage.tsx
import { Box, Button, Center, Paper, Stack, Text, TextInput, Image } from "@mantine/core";
import {useEffect, useState} from "react";
import { useNavigate } from "react-router-dom";

import background_LightDark from "../assets/background_LightDark.png";
import caster from "../assets/caster.png";
import berserker from "../assets/berserker.png";
import CloseButton from "../components/CloseButton";
import kombat888_login from "../assets/kombat888_login.png";

export default function LoginPage() {
    const [userName, setUserName] = useState("");
    const navigate = useNavigate();
    const [showManual, setShowManual] = useState(false);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Enter") {
                if (showManual) {
                    setShowManual(false);
                } else {
                    handleStart();
                }
            }
        };
        window.addEventListener("keydown", handleKeyDown);

        return () => {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [showManual, userName]);

    const handleStart = async () => {
        if (userName.trim().length <= 2) {
            alert("กรุณากรอกชื่ออย่างน้อย 3 ตัวอักษร");
            return;
        }

        try {
            const res = await fetch("/api/room", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: userName }),
            });

            if (!res.ok) throw new Error(`Create room failed (${res.status})`);

            const room = await res.json(); // ต้องมี roomId กลับมา
            // เก็บ playerId ลง sessionStorage (แยกแต่ละ tab) เพื่อให้ WaitingRoomPage รู้ว่าเราเป็นใคร
            if (room.you?.id) {
                sessionStorage.setItem(`playerId_${room.roomId}`, room.you.id);
            }
            navigate(`/waitingRoom/${room.roomId}`, { state: { user: userName, created: true } });
        } catch (e: any) {
            alert(e?.message ?? "Create room failed");
        }
        };
        return (
            <Box
                style={{
                    height: "100dvh", //ล็อกความสูง = จอ
                    width: "100%",
                    overflow: "hidden", // ห้าม scroll
                    position: "relative",
                    backgroundImage: `url(${background_LightDark})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                }}
            >
            {/* กระจกดำเต็มจอ */}
            <Box
                style={{
                    position: "fixed",
                    inset: 0,
                    background: "rgba(0,0,0,0.55)",
                    backdropFilter: "blur(10px)",
                    zIndex: 1,
                }}
            />

            {/* Spotlight แบบในรูป */}
            <Box
                style={{
                    position: "fixed",
                    inset: 0,
                    background:
                        "radial-gradient(ellipse at center, rgba(0,0,0,0.12) 0%, rgba(0,0,0,0.55) 60%, rgba(0,0,0,0.78) 100%)",
                    zIndex: 2,
                }}
            />

            {/* กล่องอยู่กลางจอ */}
            <Center style={{ height: "100%", position: "relative", zIndex: 3, padding: "0 24px" }}>
                {/* เฟรมใหญ่ซ้าย-กลาง-ขวา (responsive + ไม่ล้นจอ) */}
                <Box
                    style={{
                        position: "relative",
                        width: "min(1100px, 56vw)",
                        height: "min(78vh, 540px)",
                        display: "grid",
                        gridTemplateColumns: "1fr clamp(320px, 34vw, 420px) 1fr",
                        borderRadius: 18,
                        overflow: "hidden",
                        boxShadow: "0 30px 90px rgba(0,0,0,0.75)",
                        border: "1px solid rgba(255,255,255,0.14)",
                        background: "rgba(0,0,0,0.18)",
                        backdropFilter: "blur(6px)",
                    }}
                >
                    <CloseButton onClick={() => navigate("/")} top={12} right={12} size={34} />
                    {/* LEFT panel */}
                    <Box style={{ position: "relative", height: "100%" }}>
                        <Box
                            style={{
                                position: "absolute",
                                inset: 0,
                                backgroundImage: `url(${caster})`,
                                backgroundSize: "cover",
                                backgroundPosition: "center",
                                filter: "contrast(1.05) saturate(1.05)",
                                opacity: 0.95,
                            }}
                        />
                        <Box
                            style={{
                                position: "absolute",
                                inset: 0,
                                background:
                                    "linear-gradient(90deg, rgba(0,160,255,0.10) 0%, rgba(0,0,0,0.40) 55%, rgba(0,0,0,0.75) 100%)",
                            }}
                        />
                        <Box
                            style={{
                                position: "absolute",
                                inset: 0,
                                boxShadow:
                                    "inset 0 0 0 1px rgba(120,170,255,0.28), inset 0 0 44px rgba(0,170,255,0.10)",
                            }}
                        />
                    </Box>

                    {/* CENTER panel (login) */}
                    <Paper
                        radius={0}
                        style={{
                            position: "relative",
                            height: "100%",
                            width: "100%",
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            background: "rgba(10, 12, 16, 0.62)",
                            backdropFilter: "blur(6px)",
                            borderLeft: "1px solid rgba(255,255,255,0.08)",
                            borderRight: "1px solid rgba(255,255,255,0.08)",
                            padding: "92px 40px 70px", // เผื่อหัวข้อ + ปุ่มแดงล่าง
                        }}
                    >
                        {/* Title top */}
                        <Box style={{ position: "absolute", top: 28, left: 0, right: 0, textAlign: "center" }}>
                            <Stack align="center">
                                <Image src={kombat888_login} alt="Game Logo" w={300}></Image>
                            </Stack>


                            <Text size="xs" c="rgba(230,230,230,0.50)" style={{ letterSpacing: 2 }}>
                                IDENTIFY YOURSELF
                            </Text>
                        </Box>

                        <Stack style={{ width: "100%", maxWidth: 340 }} gap="md">
                            <Text
                                fw={700}
                                ta="center"
                                style={{
                                    color: "rgba(210,190,140,0.95)",
                                    letterSpacing: 2,
                                    textTransform: "uppercase",
                                }}
                            >
                                USERNAME
                            </Text>

                            <TextInput
                                placeholder="Enter Username"
                                value={userName}
                                onChange={(e) => setUserName(e.currentTarget.value)}
                                styles={{
                                    input: {
                                        height: 44,
                                        backgroundColor: "rgba(255,255,255,0.06)",
                                        border: "1px solid rgba(255,255,255,0.14)",
                                        color: "rgba(245,245,245,0.95)",
                                        boxShadow: "inset 0 0 18px rgba(0,0,0,0.55)",
                                    },
                                }}
                            />

                            {/* ปุ่ม ENTER ที่กดได้เฉพาะเมื่อกรอกชื่อครบ 3 ตัวอักษรขึ้นไป และมีสไตล์เหมือนในรูป */}
                            <Box
                                style={{
                                    width: "80%",
                                    position: "fixed",
                                    margin: "0 auto",
                                    bottom: "30px",
                                    left: "50%",
                                    transform: "translateX(-50%)"
                                }}
                                >
                                    <Button
                                        size="lg"
                                        radius="md"
                                        fullWidth
                                        onClick={handleStart}
                                        disabled={userName.trim().length < 3}
                                        styles={{
                                            root: {
                                                height: 46,
                                                letterSpacing: 4,
                                                textTransform: "uppercase",
                                                fontWeight: 700,
                                                background: "linear-gradient(180deg, rgba(210,145,80,1) 0%, rgba(120,70,35,1) 100%)",
                                                border: "1px solid rgba(255,215,170,0.18)",
                                                boxShadow: "0 16px 40px rgba(0,0,0,0.65), inset 0 0 0 1px rgba(0,0,0,0.35)",

                                                // ✅ ทำให้จางลงตอน disabled
                                                opacity: userName.trim().length < 3 ? 0.55 : 1,
                                                filter: userName.trim().length < 3 ? "grayscale(0.25) brightness(0.85)" : "none",
                                                cursor: userName.trim().length < 3 ? "not-allowed" : "pointer",
                                                transition: "opacity 0.15s ease, filter 0.15s ease, transform 0.06s ease",
                                            },
                                        }}
                                    >
                                        ENTER
                                    </Button>
                            </Box>

                            <Text size="xs" c="rgba(230,230,230,0.35)" ta="center">
                                (ใส่ชื่ออย่างน้อย 3 ตัวอักษร)
                            </Text>
                        </Stack>
                    </Paper>

                    {/* RIGHT panel */}
                    <Box style={{ position: "relative", height: "100%" }}>
                        <Box
                            style={{
                                position: "absolute",
                                inset: 0,
                                backgroundImage: `url(${berserker})`,
                                backgroundSize: "cover",
                                backgroundPosition: "center",
                                filter: "contrast(1.05) saturate(1.05)",
                                opacity: 0.95,
                            }}
                        />
                        <Box
                            style={{
                                position: "absolute",
                                inset: 0,
                                background:
                                    "linear-gradient(270deg, rgba(255,80,80,0.10) 0%, rgba(0,0,0,0.40) 55%, rgba(0,0,0,0.75) 100%)",
                            }}
                        />
                        <Box
                            style={{
                                position: "absolute",
                                inset: 0,
                                boxShadow:
                                    "inset 0 0 0 1px rgba(255,120,170,0.22), inset 0 0 44px rgba(255,80,120,0.08)",
                            }}
                        />
                    </Box>
                </Box>
            </Center>
        </Box>
    );
}