// src/components/UnitCard.tsx
import { Box, Image, Text, Stack, Button } from '@mantine/core';
import { useState } from 'react';
import { playSFX, SFX } from "../hooks/useSFX";
import type { UnitCardProps } from "../props/UnitCardProps.tsx";

export function UnitCard({
    strategy,
    description,
    charImg,
    backImg,
    onSelect,
    onFlip,
    isSelected = false,
    hasStrategy = false,
    initialFlipped = false,
}: UnitCardProps) {
    const [isFlipped, setIsFlipped] = useState(initialFlipped);

    const handleFlip = () => {
        playSFX(SFX.YES_YES, 1);
        const next = !isFlipped;
        setIsFlipped(next);
        onFlip?.(next);
    };

    return (
        <Box
            style={{
                perspective: '1000px',
                width: 190,
                height: 266,
                cursor: 'pointer',
                borderRadius: 16,
                border: isSelected ? '2px solid rgba(250,176,5,0.85)' : '2px solid transparent',
                boxShadow: isSelected
                    ? '0 0 20px rgba(250,176,5,0.45), 0 8px 24px rgba(0,0,0,0.4)'
                    : '0 8px 20px rgba(0,0,0,0.3)',
                transition: 'transform 0.3s, border-color 0.3s, box-shadow 0.3s',
            }}
            onMouseEnter={(e) => (e.currentTarget.style.transform = 'translateY(-15px)')}
            onMouseLeave={(e) => (e.currentTarget.style.transform = 'translateY(0)')}
            onClick={handleFlip}
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
                        <Box>
                            <Text fw={700} size="md" c="yellow.4" style={{ lineHeight: 1.2 }}>{strategy}</Text>
                            <Text size="xs" c="dimmed" lineClamp={2} style={{ fontSize: '11px', marginTop: 4 }}>
                                {description}
                            </Text>
                        </Box>

                        <Box style={{ paddingBottom: '8px' }}>
                            <Button
                                w={130}
                                h={30}
                                mx="auto"
                                display="block"
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
                            {!hasStrategy && (
                                <Text
                                    ta="center"
                                    style={{
                                        color: 'rgba(250,176,5,0.75)',
                                        fontSize: 10,
                                        marginTop: 5,
                                        letterSpacing: '0.5px',
                                    }}
                                >
                                    ⚠ Fill in your setup
                                </Text>
                            )}
                        </Box>
                    </Stack>
                </Box>
            </Box>
        </Box>
    );
}
