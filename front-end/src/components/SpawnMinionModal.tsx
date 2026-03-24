// src/components/SpawnMinionModal.tsx
import { Modal, Text, Group, Stack, Button, Paper, Badge, Divider } from '@mantine/core';
import { IconCoins, IconUserPlus } from '@tabler/icons-react';
import { useState } from 'react';

// เก็บแค่ไอคอนของแต่ละคลาส
const MINION_ICONS: Record<string, string> = {
    'Saber': '🗡️',
    'Archer': '🏹',
    'Lancer': '🔱',
    'Caster': '🪄',
    'Berserker': '🪓',
};

export interface SpawnMinionModalProps {
    opened: boolean;
    onClose: () => void;
    col: number;
    row: number;
    budget: number;
    spawnCost: number;
    availableMinions: string[];
    themeColor: string;
    onConfirmSpawn: (minionClass: string, cost: number) => void;
}

export function SpawnMinionModal({ opened, onClose, col, row, budget, spawnCost, availableMinions, themeColor, onConfirmSpawn }: SpawnMinionModalProps) {
    const [selectedMinion, setSelectedMinion] = useState<string | null>(null);

    const canAfford = budget >= spawnCost;

    const withGlow = {
        boxShadow: `0 0 10px ${themeColor === 'yellow' ? 'rgba(250, 176, 5, 0.7)' : 'rgba(112, 72, 232, 0.7)'}`,
        borderColor: themeColor === 'yellow' ? '#FAB005' : '#7048E8',
    };

    return (
        <Modal
            opened={opened}
            onClose={() => {
                setSelectedMinion(null);
                onClose();
            }}
            title={
                <Group gap="xs">
                    <IconUserPlus size={24} color={themeColor === 'yellow' ? '#FAB005' : '#7048E8'} />
                    <Text size="xl" fw={700} c={themeColor}>ส่งมินเนียนลงสนาม</Text>
                </Group>
            }
            size="md"
            radius="md"
            centered
            styles={{
                header: { backgroundColor: 'rgba(10, 10, 10, 0.95)', borderBottom: '1px solid #444' },
                body: { backgroundColor: 'rgba(10, 10, 10, 0.95)', color: 'white' },
                content: { ...withGlow, border: '1px solid transparent', backgroundColor: 'rgba(10, 10, 10, 0.95)' },
            }}
        >
            <Stack gap="md">
                <Text size="sm" c="dimmed">คุณยึดช่อง [{col}, {row}] สำเร็จแล้ว! ต้องการส่งมินเนียนลงไปป้องกันเลยหรือไม่?</Text>

                <Divider size="sm" color="#333" />

                <Text size="md" fw={600}>มินเนียนในทีมของคุณ:</Text>
                <Stack gap="xs">
                    {availableMinions.map((minionClass) => {
                        const icon = MINION_ICONS[minionClass] || '❓';
                        const isSelected = selectedMinion === minionClass;

                        return (
                            <Paper
                                key={minionClass}
                                p="sm"
                                radius="md"
                                onClick={() => setSelectedMinion(isSelected ? null : minionClass)}
                                style={{
                                    cursor: 'pointer',
                                    backgroundColor: isSelected ? (themeColor === 'yellow' ? 'rgba(250, 176, 5, 0.1)' : 'rgba(112, 72, 232, 0.1)') : 'rgba(20, 20, 20, 0.8)',
                                    border: `2px solid ${isSelected ? (themeColor === 'yellow' ? '#FAB005' : '#7048E8') : '#444'}`,
                                }}
                            >
                                <Group justify="space-between">
                                    <Group>
                                        <Text size="xl">{icon}</Text>
                                        {/* 👇 แสดงแค่ชื่อคลาส (ลบสเตตัสออกแล้ว) */}
                                        <Text size="md" fw={isSelected ? 700 : 500}>{minionClass}</Text>
                                    </Group>
                                    <Group gap="xs">
                                        <IconCoins size={16} color={budget >= spawnCost ? "#FAB005" : "#D88888"} />
                                        {/* 👇 แสดงราคาคงที่ */}
                                        <Text size="md" fw={700} c={budget >= spawnCost ? "yellow" : "#D88888"}>{spawnCost}</Text>
                                    </Group>
                                </Group>
                            </Paper>
                        );
                    })}
                </Stack>

                <Divider size="sm" color="#333" />

                <Group justify="space-between">
                    <Text>งบประมาณคงเหลือ:</Text>
                    <Text fw={700} c="yellow">${budget}</Text>
                </Group>

                {!canAfford && selectedMinion && (
                    <Badge color="red" fullWidth>เงินไม่พอสำหรับส่งมินเนียน!</Badge>
                )}

                <Group justify="space-between" mt="md">
                    <Button variant="subtle" color="gray" onClick={onClose}>ข้ามไปก่อน (Skip)</Button>
                    <Button
                        color={themeColor}
                        onClick={() => selectedMinion && onConfirmSpawn(selectedMinion, spawnCost)}
                        disabled={!selectedMinion || !canAfford}
                    >
                        ยืนยันการลงมินเนียน
                    </Button>
                </Group>
            </Stack>
        </Modal>
    );
}