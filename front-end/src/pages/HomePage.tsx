// src/pages/HomePage.tsx
import { useNavigate } from "react-router-dom";
import { Box, Center, Image, UnstyledButton, Stack } from "@mantine/core";
import logo from "../assets/logo.png";
import playBtn from "../assets/play.png";

export default function HomePage() {
    const navigate = useNavigate();

    return (
        <Box style={{ minHeight: "100vh", position: "relative" }}>
            {/* ✅ gradient ดำด้านล่าง */}
            <Box
                style={{
                    position: "fixed",
                    inset: 0,
                    pointerEvents: "none",
                    background:
                        "linear-gradient(to bottom, rgba(0,0,0,0) 55%, rgba(0,0,0,0.55) 85%, rgba(0,0,0,0.85) 100%)",
                    zIndex: 1,
                }}
            />

            {/* ✅ เนื้อหาต้องอยู่เหนือ gradient */}
            <Center style={{ minHeight: "100vh", position: "relative", zIndex: 2 }}>
                <Stack align="center" gap={100}>
                    <Image src={logo} alt="Game Logo" w={500} />

                    <UnstyledButton onClick={() => navigate("/login")}>
                        <Image
                            src={playBtn}
                            w={400}
                            style={{ transition: "transform 0.2s" }}
                            onMouseEnter={(e) => (e.currentTarget.style.transform = "scale(1.05)")}
                            onMouseLeave={(e) => (e.currentTarget.style.transform = "scale(1)")}
                        />
                    </UnstyledButton>
                </Stack>
            </Center>
        </Box>
    );
}