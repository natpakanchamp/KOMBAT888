// src/components/UnitCard.tsx
import { Box, Image, Text, Stack, Button } from '@mantine/core';
import { useState } from 'react';
import type { UnitCardProps } from "../Props/UnitCardProps.tsx";

export function UnitCard({ strategy, description, charImg, backImg, onSelect }: UnitCardProps) {
    const [isFlipped, setIsFlipped] = useState(false);

    // ฟังก์ชันสลับสถานะการพลิก
    const handleFlip = () => setIsFlipped(!isFlipped);

    return (
        <Box
            style={{
                perspective: '1000px',
                width: 190,
                height: 266,
                transition: 'transform 0.3s',
                cursor: 'pointer' // เปลี่ยนเมาส์เป็นรูปมือให้รู้ว่าคลิกได้
            }}
            onMouseEnter={(e) => (e.currentTarget.style.transform = 'translateY(-15px)')}
            onMouseLeave={(e) => (e.currentTarget.style.transform = 'translateY(0)')}
            onClick={handleFlip} // 👈 คลิกตรงไหนของการ์ดก็ได้เพื่อพลิก
        >
            <Box style={{
                position: 'relative',
                width: '100%',
                height: '100%',
                transition: 'transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
                transformStyle: 'preserve-3d',
                transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
            }}>

                {/* --- ด้านหลังการ์ด --- */}
                <Box style={{
                    position: 'absolute',
                    inset: 0,
                    backfaceVisibility: 'hidden',
                    borderRadius: '14px',
                    overflow: 'hidden',
                    boxShadow: '0 8px 20px rgba(0,0,0,0.3)'
                }}>
                    <Image src={backImg} w="100%" h="100%" fit="cover" />
                </Box>

                {/* --- ด้านหน้าการ์ด --- */}
                <Box style={{
                    position: 'absolute',
                    inset: 0,
                    backfaceVisibility: 'hidden',
                    transform: 'rotateY(180deg)',
                    borderRadius: '14px',
                    backgroundColor: '#1A1B1E',
                    border: '2px solid #373A40',
                    overflow: 'hidden',
                    display: 'flex',
                    flexDirection: 'column'
                }}>
                    <Image src={charImg} h={135} fit="cover" />
                    <Stack p="sm" gap={4} style={{ flex: 1, justifyContent: 'space-between' }}>
                        {/* ใช้ space-between เพื่อดันปุ่มลงล่างสุดแต่ไม่ให้ทับขอบ */}

                        <Box>
                            <Text fw={700} size="md" c="yellow.4" style={{ lineHeight: 1.2 }}>{strategy}</Text>
                            <Text size="xs" c="dimmed" lineClamp={2} style={{ fontSize: '11px', marginTop: 4 }}>
                                {description}
                            </Text>
                        </Box>

                        <Box style={{ paddingBottom: '8px' }}> {/* 👈 เพิ่ม Padding ล่างเพื่อไม่ให้ปุ่มตกขอบ */}
                            <Button
                                w={130} // ปรับความกว้างให้พอดี
                                h={30}  // ปรับความสูงให้เพรียวขึ้น
                                mx="auto"
                                display="block" // ช่วยให้ mx="auto" ทำงานได้แม่นยำขึ้น
                                variant="outline"
                                color="yellow.6"
                                styles={{
                                    root: {
                                        backgroundColor: 'rgba(0, 0, 0, 0.6)',
                                        border: '1.5px solid #FAB005',
                                        borderRadius: '20px',
                                    },
                                    inner: {
                                        fontSize: '10px',
                                        fontWeight: 800,
                                        letterSpacing: '1px'
                                    }
                                }}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onSelect();
                                }}
                            >
                                SETUP
                            </Button>
                        </Box>
                    </Stack>
                </Box>
            </Box>
        </Box>
    );
}