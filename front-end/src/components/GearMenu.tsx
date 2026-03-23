import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Box, Text } from "@mantine/core";
import GearIcon from "./GearIcon";
import { getHowl } from "../hooks/useBGM";

export default function GearMenu() {
    const [open, setOpen] = useState(false);
    const [bgmMuted, setBgmMuted] = useState(() => sessionStorage.getItem("bgmMuted") === "true");
    const [sfxMuted, setSfxMuted] = useState(() => sessionStorage.getItem("sfxMuted") === "true");
    const navigate = useNavigate();
    const location = useLocation();

    const toggleBgmMute = () => {
        const next = !bgmMuted;
        getHowl().mute(next);
        sessionStorage.setItem("bgmMuted", String(next));
        setBgmMuted(next);
    };

    const toggleSfxMute = () => {
        const next = !sfxMuted;
        sessionStorage.setItem("sfxMuted", String(next));
        setSfxMuted(next);
    };

    // เช็คว่าอยู่หน้า battle หรือไม่
    const isBattle = location.pathname.startsWith("/battle/");
    // ถ้าอยู่หน้า battle ดึง roomId จาก URL แล้วเช็ค spectator status
    const roomId = isBattle ? location.pathname.split("/battle/")[1] : null;
    const isSpectator = roomId ? sessionStorage.getItem(`isSpectator_${roomId}`) === "true" : false;

    // ผู้เล่นในเกม (ไม่ใช่ spectator) ไม่สามารถกด Waiting Room ได้
    const waitingRoomDisabled = isBattle && !isSpectator;

    return (
        <>
            {/* Gear Button */}
            <Box
                component="button"
                onClick={() => setOpen(true)}
                style={{
                    position: "fixed",
                    top: 16,
                    right: 16,
                    zIndex: 100,
                    background: "none",
                    border: "none",
                    outline: "none",
                    cursor: "pointer",
                    padding: 0,
                    opacity: 0.7,
                    transition: "transform 0.3s ease",
                }}
                onMouseEnter={(e) => (e.currentTarget.style.transform = "rotate(90deg)")}
                onMouseLeave={(e) => (e.currentTarget.style.transform = "rotate(0deg)")}
            >
                <GearIcon size={28} color="rgba(250,255,255,255)" />
            </Box>

            {/* Modal Overlay */}
            {open && (
                <Box
                    style={{
                        position: "fixed",
                        inset: 0,
                        zIndex: 200,
                        background: "rgba(0,0,0,0.6)",
                        backdropFilter: "blur(6px)",
                        display: "grid",
                        placeItems: "center",
                        animation: "fadeIn 0.2s ease-out",
                    }}
                    onClick={() => setOpen(false)}
                >
                    <Box
                        style={{
                            background: "rgba(10,12,16,0.92)",
                            backdropFilter: "blur(16px)",
                            border: "1px solid rgba(250,176,5,0.18)",
                            borderRadius: 16,
                            padding: "28px 32px",
                            minWidth: 240,
                            display: "flex",
                            flexDirection: "column",
                            gap: 12,
                            boxShadow: "0 20px 60px rgba(0,0,0,0.7)",
                        }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <Text
                            fw={700}
                            size="sm"
                            style={{
                                color: "rgba(250,176,5,0.9)",
                                letterSpacing: 2,
                                textTransform: "uppercase",
                                textAlign: "center",
                                marginBottom: 8,
                            }}
                        >
                            Menu
                        </Text>

                        <MenuItem label="Home" onClick={() => { setOpen(false); navigate("/"); }} />
                        <MenuItem
                            label="Waiting Room"
                            onClick={() => { if (!waitingRoomDisabled) { setOpen(false); navigate(roomId ? `/waitingRoom/${roomId}` : "/waitingRoom"); } }}
                            disabled={waitingRoomDisabled}
                        />
                        <MenuItem
                            label={bgmMuted ? "🔇 Unmute Music" : "🔊 Mute Music"}
                            onClick={toggleBgmMute}
                            variant="mute"
                        />
                        <MenuItem
                            label={sfxMuted ? "🔇 Unmute SFX" : "🔊 Mute SFX"}
                            onClick={toggleSfxMute}
                            variant="mute"
                        />
                        <MenuItem label="Close" onClick={() => setOpen(false)} variant="close" />
                    </Box>
                </Box>
            )}
        </>
    );
}

function MenuItem({ label, onClick, variant, disabled }: { label: string; onClick: () => void; variant?: "close" | "mute"; disabled?: boolean }) {
    const isClose = variant === "close";
    const isMute = variant === "mute";
    const bg = disabled ? "rgba(255,255,255,0.03)" : isClose ? "rgba(255,255,255,0.05)" : isMute ? "rgba(80,120,255,0.1)" : "linear-gradient(180deg, rgba(250,176,5,0.15), rgba(250,176,5,0.05))";
    const border = disabled ? "1px solid rgba(255,255,255,0.05)" : isClose ? "1px solid rgba(255,255,255,0.1)" : isMute ? "1px solid rgba(80,120,255,0.25)" : "1px solid rgba(250,176,5,0.2)";
    const color = disabled ? "rgba(230,230,230,0.25)" : isClose ? "rgba(230,230,230,0.6)" : "rgba(230,230,230,0.9)";
    return (
        <Box
            component="button"
            onClick={disabled ? undefined : onClick}
            style={{
                background: bg,
                border,
                borderRadius: 10,
                padding: "10px 20px",
                cursor: disabled ? "not-allowed" : "pointer",
                outline: "none",
                transition: "all 0.2s ease",
                color,
                fontSize: 14,
                fontWeight: 600,
                letterSpacing: 1,
                textTransform: "uppercase" as const,
                opacity: disabled ? 0.5 : 1,
            }}
            onMouseEnter={(e) => {
                if (disabled) return;
                e.currentTarget.style.background = isClose ? "rgba(255,255,255,0.1)" : isMute ? "rgba(80,120,255,0.2)" : "linear-gradient(180deg, rgba(250,176,5,0.3), rgba(250,176,5,0.1))";
            }}
            onMouseLeave={(e) => {
                if (disabled) return;
                e.currentTarget.style.background = bg;
            }}
        >
            {label}
        </Box>
    );
}
