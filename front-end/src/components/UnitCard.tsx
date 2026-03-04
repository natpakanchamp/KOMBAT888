// src/components/UnitCard.tsx
import { Box, Image, UnstyledButton, Text, Stack, Button } from '@mantine/core';
import { keyframes } from '@emotion/react';
import { useEffect, useState } from 'react';
import type { UnitCardProps } from "../Props/UnitCardProps.tsx";

// 1. เปลี่ยนการเขียน Keyframes เป็นแบบ Template Literal ของ Emotion
const Shake = keyframes`
    0% { transform: translateX(0px); }
    25% { transform: translateX(-20px); }
    50% { transform: translateX(20px); }
    75% { transform: translateX(-20px); }
    100% { transform: translateX(0px); }
`;

export function UnitCard({ name, description, charImg, backImg }: UnitCardProps) {
    const [isFlipped, setIsFlipped] = useState(false);
    const [shake, setShake] = useState(false);

    useEffect(() => {
        if (!isFlipped) return;
        setShake(true);
        const t = setTimeout(() => setShake(false), 450);
        return () => clearTimeout(t);
    }, [isFlipped]);

    return (
        // กล่องชั้นนอกสุด: กำหนดมิติความลึก (Perspective)
        <Box style={{ perspective: '1000px', width: 200, height: 280 }}>

            {/* กล่องชั้นกลาง: รับหน้าที่ "สั่น" อย่างเดียว (ดึง keyframes มาใช้ตรงๆ) */}
            <Box
                style={{
                    width: '100%',
                    height: '100%',
                    animation: shake ? `${Shake} 450ms ease-in-out` : 'none',
                    willChange: 'transform',
                }}
            >
                {/* กล่องชั้นในสุด: รับหน้าที่ "พลิก 3D" อย่างเดียว */}
                <Box
                    style={{
                        position: 'relative',
                        width: '100%',
                        height: '100%',
                        transition: 'transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
                        transformStyle: 'preserve-3d',
                        transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
                        willChange: 'transform',
                    }}
                >
                    {/* --- 🔹 ด้านหลังการ์ด --- */}
                    <UnstyledButton
                        onClick={() => setIsFlipped(true)}
                        style={{
                            position: 'absolute',
                            inset: 0,
                            backfaceVisibility: 'hidden',
                            borderRadius: '16px',
                            overflow: 'hidden',
                            boxShadow: '0 8px 20px rgba(0,0,0,0.3)',
                        }}
                    >
                        <Image src={backImg} w="100%" h="100%" fit="cover" />
                    </UnstyledButton>

                    {/* --- 🔹 ด้านหน้าการ์ด (มีข้อมูล + ปุ่ม) --- */}
                    <Box
                        onClick={() => setIsFlipped(false)}
                        style={{
                            position: 'absolute',
                            inset: 0,
                            backfaceVisibility: 'hidden',
                            transform: 'rotateY(180deg)',
                            borderRadius: '16px',
                            backgroundColor: '#1A1B1E',
                            border: '2px solid #373A40',
                            overflow: 'hidden',
                            display: 'flex',
                            flexDirection: 'column',
                            color: 'white'
                        }}
                    >
                        <Image src={charImg} h={150} fit="cover" />

                        <Stack p="md" gap="xs" style={{ flex: 1 }}>
                            <Text fw={700} size="lg" c="yellow.4">{name}</Text>
                            <Text size="xs" c="dimmed" lineClamp={3}>
                                {description}
                            </Text>

                            {/* ส่วนของปุ่มกดบนหน้าการ์ด */}
                            <Stack mt="auto" gap={5}>
                                <Button
                                    variant="filled"
                                    color="blue"
                                    fullWidth
                                    size="xs"
                                    onClick={(e) => {
                                        e.stopPropagation(); // ⚠️ กันไม่ให้กดปุ่มแล้วการ์ดพลิกกลับ
                                        alert(`คุณเลือกใช้งาน: ${name}`);
                                    }}
                                >
                                    setup
                                </Button>
                            </Stack>
                        </Stack>
                    </Box>
                </Box>
            </Box>
        </Box>
    );
}