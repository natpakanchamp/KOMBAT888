// src/components/CloseButton.tsx
import { Box } from "@mantine/core";
import type { CSSProperties } from "react";

type CloseButtonProps = {
    onClick: () => void;
    top?: number;
    right?: number;
    size?: number;
    style?: CSSProperties;
};

export default function CloseButton({
                                        onClick,
                                        top = 12,
                                        right = 12,
                                        size = 34,
                                        style,
                                    }: CloseButtonProps) {
    return (
        <Box
            component="button"
            type="button"
            onClick={onClick}
            style={{
                position: "absolute",
                top,
                right,
                width: size,
                height: size,
                borderRadius: Math.round(size * 0.3),
                background: "linear-gradient(180deg, rgba(170,35,35,1), rgba(90,10,10,1))",
                border: "1px solid rgba(255,255,255,0.18)",
                boxShadow: "0 10px 20px rgba(0,0,0,0.65)",
                cursor: "pointer",
                zIndex: 50,
                display: "grid",
                placeItems: "center",
                padding: 0,
                ...style,
            }}
            aria-label="Close"
            title="Close"
        >
            <Box style={{ width: size * 0.42, height: size * 0.42, position: "relative" }}>
                <Box
                    style={{
                        position: "absolute",
                        inset: 0,
                        margin: "auto",
                        width: "100%",
                        height: Math.max(2, Math.round(size * 0.06)),
                        background: "rgba(255,255,255,0.9)",
                        transform: "rotate(45deg)",
                        borderRadius: 2,
                    }}
                />
                <Box
                    style={{
                        position: "absolute",
                        inset: 0,
                        margin: "auto",
                        width: "100%",
                        height: Math.max(2, Math.round(size * 0.06)),
                        background: "rgba(255,255,255,0.9)",
                        transform: "rotate(-45deg)",
                        borderRadius: 2,
                    }}
                />
            </Box>
        </Box>
    );
}