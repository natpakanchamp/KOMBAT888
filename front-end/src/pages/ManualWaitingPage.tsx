import {Box, Text, Title, ScrollArea, Divider} from "@mantine/core";
import CloseButton from "../components/CloseButton";
import { useEffect } from "react";

type ManualWaitingPageProps = {
    onClose: () => void;
};

// ถ้าผู้เล่นคลิกที่ปุ่ม "How to Play" ในหน้า Waiting Room จะเปิด ManualWaitingPage นี้ขึ้นมา ซึ่งจะมีคำแนะนำการใช้งานต่างๆ เช่น วิธีการสร้างห้อง รอผู้เล่นอื่นเข้าร่วม เลือกมินเนี่ยน และเริ่มเกม รวมถึงเคล็ดลับต่างๆ เพื่อช่วยให้ผู้เล่นเข้าใจระบบและวิธีการเล่นได้ง่ายขึ้น
// และรองรับการปิดด้วยการคลิกที่ปุ่ม Close หรือกดปุ่ม Escape บนคีย์บอร์ด เพื่อให้ผู้เล่นสามารถกลับไปที่หน้า Waiting Room ได้อย่างสะดวก
export default function ManualWaitingPage({ onClose }: ManualWaitingPageProps) {
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") {
                onClose();
            }
        };

        window.addEventListener("keydown", handleKeyDown);
        return () => {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [onClose]);

    return (
        <Box
            style={{
                position: "fixed",
                inset: 0,
                zIndex: 200,
                background: "rgba(0,0,0,0.7)",
                backdropFilter: "blur(6px)",
                display: "grid",
                placeItems: "center",
                animation: "fadeIn 0.25s ease-out",
            }}
            onClick={onClose}
        >
            <Box
                style={{
                    position: "relative",
                    width: "min(560px, 88vw)",
                    maxHeight: "min(80vh, 640px)",
                    borderRadius: 18,
                    overflow: "hidden",
                    boxShadow: "0 30px 90px rgba(0,0,0,0.75)",
                    border: "1px solid rgba(250,176,5,0.18)",
                    background: "rgba(10, 12, 16, 0.92)",
                    backdropFilter: "blur(16px)",
                    display: "flex",
                    flexDirection: "column",
                }}
                onClick={(e) => e.stopPropagation()}
            >
                <CloseButton onClick={onClose} top={14} right={14} size={28} />

                <Box
                    style={{
                        padding: "24px 28px 16px",
                        borderBottom: "1px solid rgba(250,176,5,0.15)",
                        background: "linear-gradient(180deg, rgba(250,176,5,0.06) 0%, transparent 100%)",
                        flexShrink: 0,
                    }}
                >
                    <Title
                        order={3}
                        ta="center"
                        style={{
                            color: "rgba(250,176,5,0.95)",
                            letterSpacing: 3,
                            textTransform: "uppercase",
                            textShadow: "0 3px 18px rgba(0,0,0,0.9)",
                            fontSize: "clamp(1rem, 2.5vw, 1.3rem)",
                        }}
                    >
                        วิธีใช้งาน Waiting Room
                    </Title>
                </Box>

                <ScrollArea style={{ flex: 1, padding: "20px 28px 24px" }} type="auto">
                    <Section title="BOT Mode">
                        <a>หากผู้เล่นต้องการเล่น Bot Mode จะต้องกดไปที่ปุ่ม Spectate เพื่อย้ายตัวเองเข้าไปเป็นผู้รับชม จากนั้นผู้ที่เป็น Host จะต้องกด เพิ่มบอทให้เต็มทั้ง 2 คน</a>
                    </Section>

                    <Section title="Solitare Mode">
                        <a> หากผู้เล่นต้องการเล่นกับบอท ผู้เล่นที่เป็น Host จะต้องกดปุ่ม Add Bot เข้ามาให้ครบ 2 คน</a>
                    </Section>

                    <Section title="DUEL Mode">
                        <li><a>หากผู้เล่นต้องการเล่นกับผู้เล่นอื่น ผู้เล่นที่เป็น Host จะต้องรอให้มีผู้เล่นคนอื่นเข้ามาในห้องจนครบ 2 คน</a></li>
                        <li><a>ผู้เล่นที่จะเข้ามาเล่นกับ Host สามารถเข้าร่วมได้ทั้งแบบ Copy Link หรือกรอกรหัสห้องที่ช่องก็ได้</a></li>
                    </Section>
                    <Section>
                        <Divider />
                    </Section>
                    <Section title="การเลือก Minion">
                        <a>เมื่อผู้เล่นเลือกโหมดการเล่นได้แล้ว ผู้เล่นจะต้องเลือก Minion ที่จะใช้ในการเล่น โดยผู้เล่นสามารถเลือกได้จากตัวเลือกที่มีอยู่ในหน้าจอ และสามารถดูรายละเอียดของแต่ละ Minion ได้ก่อนที่จะทำการเลือก ซึ่งทั้งสองผู้เล่นจะต้องเลือกตัวละครให้เหมือนกันทั้งสองฝั่ง</a>
                    </Section>
                </ScrollArea>
            </Box>
        </Box>
    );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
    return (
        <Box style={{ marginBottom: 20 }}>
            <Text
                fw={700}
                size="sm"
                style={{
                    color: "rgba(250,176,5,0.9)",
                    letterSpacing: 1.5,
                    marginBottom: 8,
                    textTransform: "uppercase",
                }}
            >
                {title}
            </Text>
            <Box style={{ display: "flex", flexDirection: "column", gap: 6 }}>{children}</Box>
        </Box>
    );
}

function BulletItem({ children }: { children: React.ReactNode }) {
    return (
        <Text
            size="sm"
            style={{
                color: "rgba(230,230,230,0.8)",
                paddingLeft: 16,
                position: "relative",
                lineHeight: 1.6,
            }}
        >
            <Box
                component="span"
                style={{
                    position: "absolute",
                    left: 0,
                    top: 2,
                    color: "rgba(250,176,5,0.5)",
                }}
            >
                •
            </Box>
            {children}
        </Text>
    );
}

function Highlight({ children }: { children: React.ReactNode }) {
    return (
        <Box
            component="span"
            style={{
                color: "rgba(250,176,5,0.95)",
                fontWeight: 600,
            }}
        >
            {children}
        </Box>
    );
}
