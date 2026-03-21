import { Box, Button } from "@mantine/core";
import { useNavigate } from "react-router-dom";
import errorBackground from "../assets/error404.png";

export default function ErrorPage() {
    const navigate = useNavigate();

    return (
        <Box
            style={{
                minHeight: "100vh",
                width: "100%",
                backgroundImage: `url(${errorBackground})`,
                backgroundSize: "cover",
                backgroundPosition: "center",
                backgroundAttachment: "fixed",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "flex-end",
                paddingBottom: "12vh",
            }}
        >
            <Button
                onClick={() => navigate(-1)}
                size="lg"
                styles={{
                    root: {
                        background: "linear-gradient(180deg, rgba(210,170,100,0.9) 0%, rgba(120,80,30,0.9) 100%)",
                        border: "1px solid rgba(255,215,170,0.25)",
                        boxShadow: "0 8px 32px rgba(0,0,0,0.6)",
                        color: "rgba(255,255,255,0.95)",
                        letterSpacing: 4,
                        textTransform: "uppercase" as const,
                        fontWeight: 700,
                        fontSize: 16,
                        padding: "0 40px",
                        transition: "all 0.2s ease",
                    },
                }}
            >
                Go Back
            </Button>
        </Box>
    );
}