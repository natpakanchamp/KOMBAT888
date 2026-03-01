// src/components/UnitCard.tsx
import { Box, Image, UnstyledButton, Text, Stack, Button } from '@mantine/core';
import { useState } from 'react';
import type { UnitCardProps } from "../Props/UnitCardProps.tsx"; // ✅ ถูกต้องตามมาตรฐาน TS

export function UnitCard({ name, description, charImg, backImg }: UnitCardProps) {
    const [isFlipped, setIsFlipped] = useState(false);

    return (
        <Box style={{ perspective: '1000px', width: 200, height: 280 }}>
            <Box
                style={{
                    position: 'relative',
                    width: '100%',
                    height: '100%',
                    transition: 'transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
                    transformStyle: 'preserve-3d',
                    transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
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
                                    e.stopPropagation(); // ⚠️ สำคัญมาก: กันไม่ให้กดปุ่มแล้วการ์ดพลิกกลับ
                                    alert(`คุณเลือกใช้งาน: ${name}`);
                                }}
                            >
                                setup
                            </Button>

                            <Button
                                variant="subtle"
                                color="gray"
                                size="compact-xs"
                                onClick={() => setIsFlipped(false)}
                            >
                                พลิกกลับ
                            </Button>
                        </Stack>
                    </Stack>
                </Box>
            </Box>
        </Box>
    );
}