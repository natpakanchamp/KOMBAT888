// src/pages/HomePage.tsx
import { useNavigate } from 'react-router-dom'
import { Center, Image, UnstyledButton, Stack } from "@mantine/core";
import logo from "../assets/logo.png";
import playBtn from "../assets/play.png";

export default function HomePage() {

    const navigate = useNavigate();

    return (
        <Center style={{ minHeight: "100vh" }}>
            <Stack align="center" gap={100}>
                <Image
                    src={logo}
                    alt="Game Logo"
                    w={500}
                />

                <UnstyledButton onClick={() => navigate('/select')}>
                    <Image
                        src={playBtn}
                        w={400}
                        style={{ transition: "transform 0.2s" }}
                        onMouseEnter={(e) =>
                            (e.currentTarget.style.transform = "scale(1.05)")}
                        onMouseLeave={(e) =>
                            (e.currentTarget.style.transform = "scale(1)")}/>
                </UnstyledButton>
            </Stack>
        </Center>
    );
}