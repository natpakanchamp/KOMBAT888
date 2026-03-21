import { useState } from "react";
import { Button, Tooltip } from "@mantine/core";

interface SpectatorButtonProps {
    roomId: string;
    playerId: string;
    isSpectator?: boolean; // สถานะปัจจุบันว่าเป็นผู้ชมอยู่หรือไม่
}

export default function SpectatorButton({ roomId, playerId, isSpectator = false }: SpectatorButtonProps) {
    const [loading, setLoading] = useState(false);

    async function toggleSpectator() {
        if (!roomId || !playerId) return;
        setLoading(true);
        try {
            // สมมติว่า Backend มี Endpoint สำหรับเปลี่ยนโหมดนี้
            await fetch(`/api/room/${roomId}/spectate`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ playerId, isSpectator: !isSpectator }),
            });
        } catch (e) {
            console.error("Failed to toggle spectator mode", e);
        } finally {
            setLoading(false);
        }
    }

    return (
        <Tooltip label={isSpectator ? "Join as a player" : "Watch as a spectator"} withArrow>
            <Button
                size="md"
                radius="md"
                onClick={toggleSpectator}
                loading={loading}
                styles={{
                    root: {
                        minWidth: 160,
                        height: 46,
                        letterSpacing: 2,
                        textTransform: "uppercase" as const,
                        fontWeight: 700,
                        fontSize: 13,
                        // ถ้าเป็นผู้ชมอยู่แล้ว ปุ่มจะเป็นสีฟ้าชวนให้กลับไปเล่น ถ้าเล่นอยู่ปุ่มจะเป็นสีเทา
                        background: isSpectator
                            ? "linear-gradient(180deg, rgba(80,160,255,0.8), rgba(40,100,180,0.8))"
                            : "linear-gradient(180deg, rgba(60,60,60,0.8), rgba(30,30,30,0.8))",
                        border: isSpectator
                            ? "1px solid rgba(150,200,255,0.25)"
                            : "1px solid rgba(255,255,255,0.15)",
                        boxShadow: "0 10px 30px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(0,0,0,0.3)",
                        transition: "all 0.25s ease",
                    },
                }}
            >
                {isSpectator ? "PLAY GAME" : "SPECTATE"}
            </Button>
        </Tooltip>
    );
}