// src/pages/LoginPage.tsx
import { Center, Stack, TextInput, Button, Title, Paper } from '@mantine/core';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function LoginPage() {
    const [userName, setUserName] = useState(''); // เก็บค่าที่พิมพ์
    const navigate = useNavigate(); // ใช้สำหรับเปลี่ยนหน้า

    const handleStart = () => {
        if (userName.trim().length > 2) {
            // สามารถส่งค่า userName ไปยังหน้าถัดไปผ่าน state ได้
            navigate('/select', { state: { user: userName } });
        } else {
            alert("กรุณากรอกชื่ออย่างน้อย 3 ตัวอักษร");
        }
    };

    return (
        <Center style={{ minHeight: '100vh' }}>
            <Paper
                p="xl"
                radius="lg"
                style={{
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    backdropFilter: 'blur(10px)',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    width: 350
                }}
            >
                <Stack align="stretch" gap="md">
                    <Title order={2} c="white" ta="center">IDENTIFY YOURSELF</Title>

                    <TextInput
                        placeholder="Enter your name..."
                        label="USERNAME"
                        value={userName}
                        onChange={(event) => setUserName(event.currentTarget.value)} // อัปเดต State
                        styles={{
                            input: { backgroundColor: 'rgba(255,255,255,0.1)', color: 'white' },
                            label: { color: 'yellow' }
                        }}
                    />

                    <Button
                        variant="filled"
                        color="yellow.6"
                        fullWidth
                        onClick={handleStart}
                        disabled={userName.trim().length < 3}
                    >
                        ENTER THE WORLD
                    </Button>
                </Stack>
            </Paper>
        </Center>
    );
}