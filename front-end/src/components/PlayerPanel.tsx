// src/components/PlayerPanel.tsx
import { Paper, Stack, Text, Group, Badge } from '@mantine/core';

export function PlayerPanel({ playerName, themeColor, borderColor, budget, spawnsLeft, isActive }: PlayerPanelProps) {
    return (
        <Paper
            p="md"
            radius="md"
            style={{
                flex: 1, // 👈 ขยายเต็มพื้นที่ที่เหลือใน Group
                maxWidth: '400px', // 👈 จำกัดความกว้างไม่ให้ใหญ่เกินไป
                backgroundColor: 'rgba(20, 20, 20, 0.8)',
                color: 'white',
                border: `2px solid ${isActive ? borderColor : '#444'}`,
                boxShadow: isActive ? `0 0 15px ${borderColor}` : 'none',
                opacity: isActive ? 1 : 0.5,
                transition: 'all 0.3s ease'
            }}
        >
            <Stack gap="xs"> {/* 👈 ลดช่องว่างระหว่างบรรทัดให้กระชับ */}
                <Group justify="space-between">
                    <Text size="xl" fw={700} c={isActive ? themeColor : 'dimmed'}>
                        {playerName}
                    </Text>
                    {isActive && (
                        <Badge color={themeColor} variant="light" size="lg">
                            ⭐ YOUR TURN
                        </Badge>
                    )}
                </Group>

                {/* 👇 จับข้อมูลเงินและมินเนียนมาเรียงแนวนอนประหยัดพื้นที่ */}
                <Group justify="space-between">
                    <Text>💰 Budget: ${budget.toLocaleString()}</Text>
                    <Text>⚔️ Spawns Left: {spawnsLeft}</Text>
                </Group>
            </Stack>
        </Paper>
    );
}