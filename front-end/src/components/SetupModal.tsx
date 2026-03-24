import { Modal, Stack, Title, Group, Button, Box, Text, Divider, Textarea, Loader } from "@mantine/core"
import { useState, useEffect } from "react";
import type { SetupModalProps } from "../props/SetupModalProps"

export function SetupModal({ opened, onClose, onSave, onRun, unitType, isPlayer2 }: SetupModalProps & { isPlayer2?: boolean }) {
    const [strategy, setStrategy] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    // 👇 ใช้ useEffect วิ่งไปอ่านไฟล์ .txt เมื่อหน้าต่างถูกเปิด
    useEffect(() => {
        if (opened && unitType) {
            setIsLoading(true);
            //  XXX.txt อยู่ในโฟลเดอร์ public/strategies/
            fetch(`/strategies/${unitType}.txt`)
                .then((response) => {
                    if (!response.ok) {
                        throw new Error("ไม่พบไฟล์สคริปต์");
                    }
                    return response.text(); // แปลงไฟล์เป็นข้อความ
                })
                .then((text) => {
                    setStrategy(text.trim()); // เอาข้อความมาใส่ในกล่อง และตัดช่องว่างหัวท้ายทิ้ง
                    setIsLoading(false);
                })
                .catch((error) => {
                    console.error("Error loading strategy:", error);
                    // ถ้าหาไฟล์ไม่เจอ ให้ตั้งค่า Default กันเหนียวไว้
                    setStrategy("move downright");
                    setIsLoading(false);
                });
        }
    }, [opened, unitType]);

    return (
        <Modal
            opened={opened}
            onClose={onClose}
            title={<Title order={4} c={isPlayer2 ? "violet.5" : "yellow.5"}>MINION AST SCRIPT</Title>}
            centered
            size="lg"
            radius="md"
            overlayProps={{ backgroundOpacity: 0.7, blur: 4 }}
            styles={{
                content: { backgroundColor: '#141517', color: 'white', border: '1px solid #373A40' },
                header: { backgroundColor: '#141517' }
            }}
        >
            <Stack gap="md">
                <Box>
                    <Text size="xs" c="dimmed" fw={500} mb={4}>SELECTED UNIT</Text>
                    <Group>
                        <Text size="xl" fw={700} c={isPlayer2 ? "violet.4" : "blue.4"}>{unitType}</Text>
                        {isLoading && <Loader color="gray" size="sm" />}
                    </Group>
                </Box>

                <Divider my="xs" color="dark.4" />

                <Textarea
                    label="STRATEGY SCRIPT"
                    description="อ่านข้อมูลจากไฟล์ Text (สามารถแก้ไขสคริปต์ได้)"
                    placeholder="กำลังโหลดข้อมูล..."
                    minRows={8}
                    autosize
                    spellCheck={false}
                    disabled={isLoading} // ล็อกกล่องไว้ตอนกำลังอ่านไฟล์
                    value={strategy}
                    onChange={(e) => setStrategy(e.currentTarget.value)}
                    styles={{
                        input: {
                            backgroundColor: '#1E1E1E',
                            color: '#D4D4D4',
                            fontFamily: '"Fira Code", "Consolas", monospace',
                            fontSize: '14px',
                            lineHeight: '1.6',
                            border: '1px solid #444',
                            padding: '12px'
                        },
                        label: { color: 'gray.4', marginBottom: 5, fontWeight: 600, fontSize: '12px', letterSpacing: '1px' },
                        description: { color: 'gray.6', marginBottom: 12, fontSize: '11px' }
                    }}
                />

                <Group justify="flex-end" mt="md">
                    <Button variant="subtle" color="gray" onClick={onClose}>
                        CANCEL
                    </Button>

                    <Button
                        variant="light"
                        color="teal"
                        onClick={() => onRun && onRun(strategy)}
                        disabled={!strategy.trim() || isLoading}
                    >
                        ▶ TEST COMPILE
                    </Button>

                    <Button
                        color="blue"
                        onClick={() => { onSave(strategy); onClose(); }}
                        disabled={!strategy.trim() || isLoading}
                    >
                        CONFIRM SCRIPT
                    </Button>
                </Group>
            </Stack>
        </Modal>
    );
}