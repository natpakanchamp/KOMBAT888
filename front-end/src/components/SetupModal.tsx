import { Modal, Stack, Title, Group, Button, Box, Text, Divider, Textarea } from "@mantine/core"
import { useState, useEffect } from "react";
import type { SetupModalProps } from "../props/SetupModalProps"

// 👇 เพิ่ม onRun เข้ามาใน Props ที่รับมา
export function SetupModal({ opened, onClose, onSave, onRun, unitType }: SetupModalProps) {
    const [strategy, setStrategy] = useState('');

    // ล้างค่าในช่องกรอกทุกครั้งที่เปิด Modal ใหม่
    useEffect(() => {
        if (opened) setStrategy('');
    }, [opened]);

    return (
        <Modal
            opened={opened}
            onClose={onClose}
            title={<Title order={4} c="yellow.5">UNIT STRATEGY SETUP</Title>}
            centered
            radius="md"
            overlayProps={{ backgroundOpacity: 0.55, blur: 3 }}
            styles={{
                content: { backgroundColor: '#1A1B1E', color: 'white', border: '1px solid #373A40' },
                header: { backgroundColor: '#1A1B1E' }
            }}
        >
            <Stack gap="md">
                {/* ส่วนแสดงชื่อตัวละครที่ Fix ไว้ */}
                <Box>
                    <Text size="xs" c="dimmed" fw={500} mb={4}>SELECTED UNIT</Text>
                    <Text size="xl" fw={700} c="blue.4">{unitType}</Text>
                </Box>

                <Divider my="xs" label="Strategy Details" labelPosition="center" />

                {/* ช่องกรอกแบบหลายบรรทัด (Textarea) */}
                <Textarea
                    label="STRATEGY PLAN"
                    placeholder="ระบุแผนการรบสำหรับตัวละครนี้ (เช่น เน้นตั้งรับแล้วสวนกลับ...)"
                    minRows={4}
                    autosize
                    value={strategy}
                    onChange={(e) => setStrategy(e.currentTarget.value)}
                    styles={{
                        input: { backgroundColor: '#2C2E33', color: 'white', fontFamily: 'monospace' }, // เพิ่ม monospace ให้อ่านโค้ดง่ายขึ้น
                        label: { color: 'gray.4', marginBottom: 5 }
                    }}
                />

                <Group justify="flex-end" mt="md">
                    <Button variant="subtle" color="gray" onClick={onClose}>
                        CANCEL
                    </Button>

                    {/* 👇 เพิ่มปุ่ม RUN ตรงนี้ 👇 */}
                    <Button
                        variant="light"
                        color="teal"
                        onClick={() => onRun && onRun(strategy)}
                        disabled={!strategy.trim()}
                    >
                        ▶ RUN
                    </Button>

                    <Button
                        color="blue"
                        onClick={() => { onSave(strategy); onClose(); }}
                        disabled={!strategy.trim()}
                    >
                        CONFIRM STRATEGY
                    </Button>
                </Group>
            </Stack>
        </Modal>
    );
}