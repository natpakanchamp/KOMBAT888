// src/components/PurchasePanel.tsx
import { Paper, Text, Group, Button, Stack, Badge, Divider } from '@mantine/core';
import { IconHexagon, IconCoins } from '@tabler/icons-react';

export interface PurchasePanelProps {
    isActive: boolean;
    themeColor: string;
    borderColor: string;
    selectedHex: { col: number, row: number } | null;
    onBuy: () => void;
    canAfford: boolean;
    hasPurchased?: boolean; // 👈 เพิ่ม Props ตัวนี้เข้ามา
}

export function PurchasePanel({ isActive, themeColor, borderColor, selectedHex, onBuy, canAfford, hasPurchased }: PurchasePanelProps) {
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

                {/* 👇 เช็คเงื่อนไข: ถ้าซื้อไปแล้วให้โชว์ข้อความนี้ */}
                {hasPurchased ? (
                    <Text size="sm" c="dimmed" ta="center" py="xl">
                        คุณซื้อพื้นที่ไปแล้วในเทิร์นนี้<br/>(จำกัด 1 ครั้ง/เทิร์น)
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
                    </>
                ) : (
                    <Text size="sm" c="dimmed" ta="center" py="xl">
                        คลิกเลือกพื้นที่รอบอาณาเขต<br/>ที่เรืองแสงเพื่อซื้อ
                    </Text>
                )}
            </Stack>
        </Paper>
    );
}