// src/components/PurchasePanel.tsx
import { Paper, Text, Group, Button, Stack, Badge, Divider } from '@mantine/core';
import { IconHexagon, IconCoins } from '@tabler/icons-react';

export interface PurchasePanelProps {
    isActive: boolean;
    themeColor: string;
    borderColor: string;
    selectedHex: { col: number, row: number } | null;
    onBuy: () => void;
    onSkip: () => void; // 👈 เพิ่ม Props สำหรับฟังก์ชันข้าม
    canAfford: boolean;
    hasPurchased?: boolean;
}

export function PurchasePanel({ isActive, themeColor, borderColor, selectedHex, onBuy, onSkip, canAfford, hasPurchased }: PurchasePanelProps) {
    const COST = 150;

    return (
        <Paper
            p="md"
            radius="md"
            style={{
                width: '280px',
                backgroundColor: 'rgba(20, 20, 20, 0.8)',
                color: 'white',
                border: `2px solid ${isActive ? borderColor : '#444'}`,
                boxShadow: isActive ? `0 0 15px ${borderColor}` : 'none',
                opacity: isActive ? 1 : 0.5,
                transition: 'all 0.3s ease'
            }}
        >
            <Stack gap="sm">
                <Text size="md" fw={700} c={isActive ? themeColor : 'dimmed'}>
                    ซื้อพื้นที่ใหม่ (Purchase Area)
                </Text>

                <Divider color="#444" />

                {hasPurchased ? (
                    <Text size="sm" c="dimmed" ta="center" py="xl">
                        คุณใช้สิทธิ์ไปแล้วในเทิร์นนี้<br/>(จำกัด 1 ครั้ง/เทิร์น)
                    </Text>
                ) : selectedHex ? (
                    <>
                        <Group justify="space-between">
                            <Group gap="xs">
                                <IconHexagon size={16} color={borderColor} />
                                <Text size="sm">Hex [{selectedHex.col}, {selectedHex.row}]</Text>
                            </Group>
                            <Badge color={themeColor} variant="light">เลือกแล้ว</Badge>
                        </Group>

                        <Group justify="space-between" mt="xs">
                            <Text size="sm" c="dimmed">ราคา (Cost):</Text>
                            <Group gap="xs">
                                <IconCoins size={16} color="#FAB005" />
                                <Text size="sm" fw={700} c="yellow">{COST}</Text>
                            </Group>
                        </Group>

                        <Button
                            color={themeColor}
                            fullWidth
                            mt="sm"
                            onClick={onBuy}
                            disabled={!isActive || !canAfford}
                            variant={isActive ? "filled" : "light"}
                        >
                            ซื้อพื้นที่ (Buy)
                        </Button>

                        {/* 👇 ปุ่มข้ามการซื้อ (กรณีเลือกช่องไว้แล้วแต่เปลี่ยนใจ) */}
                        <Button variant="subtle" color="gray" fullWidth onClick={onSkip} disabled={!isActive}>
                            ข้ามการซื้อ (Skip)
                        </Button>
                    </>
                ) : (
                    <Stack gap="xs">
                        <Text size="sm" c="dimmed" ta="center" py="sm">
                            คลิกเลือกพื้นที่รอบอาณาเขต<br/>ที่เรืองแสงเพื่อซื้อ
                        </Text>

                        {/* 👇 ปุ่มข้ามการซื้อ (กรณีไม่ได้เลือกช่องไหนเลย) */}
                        <Button variant="light" color="gray" fullWidth mt="xs" onClick={onSkip} disabled={!isActive}>
                            ข้ามการซื้อเทิร์นนี้ (Skip)
                        </Button>
                    </Stack>
                )}
            </Stack>
        </Paper>
    );
}