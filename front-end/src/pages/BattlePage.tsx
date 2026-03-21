// src/pages/BattlePage.tsx
import { Box, Button } from '@mantine/core';
import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Hexagon } from '../components/Hexagon';
import { PlayerPanel } from '../components/PlayerPanel';
import type { HexState } from '../type/HexState';

export default function BattlePage() {
    const { roomId } = useParams<{ roomId: string }>();
    const [currentTurn, setCurrentTurn] = useState<number>(1);

    const rows = 8;
    const cols = 8;

    const getInitialState = (c: number, r: number): HexState => {
        if (c < 2 && r < 2) return 'LIGHT';
        if (c === 2 && r === 0) return 'LIGHT';
        if (c > 5 && r > 5) return 'DARK';
        if (c === 5 && r === 7) return 'DARK';
        return 'NEUTRAL';
    };

    return (
        <Box
            style={{
                minHeight: '100vh',
                width: '100%',
                backgroundColor: 'rgba(0, 0, 0, 0.6)',
                display: 'flex',
                flexDirection: 'row', // 👈 ให้จัดเรียงซ้าย-ขวาตามเดิม
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '0 40px',
                position: 'relative' // 👈 สำคัญ: เพื่อให้ปุ่มอ้างอิงตำแหน่งได้ถูกต้อง
            }}
        >
            {/* 👈 แผงข้อมูลด้านซ้าย (Player 1) */}
            <PlayerPanel
                playerName="Player 1 (Light)"
                themeColor="yellow"
                borderColor="#FAB005"
                budget={10000}
                spawnsLeft={47}
                isActive={currentTurn === 1}
            />

            {/* 🎯 กระดานเกมตรงกลาง */}
            <Box style={{ display: 'flex', flexDirection: 'row' }}>
                {Array.from({ length: cols }).map((_, c) => (
                    <Box
                        key={`col-${c}`}
                        style={{
                            display: 'flex', flexDirection: 'column',
                            marginLeft: c === 0 ? '0px' : '-16px',
                            marginTop: c % 2 === 0 ? '28px' : '0px'
                        }}
                    >
                        {Array.from({ length: rows }).map((_, r) => {
                            const hexState = getInitialState(c, r);
                            return (
                                <Hexagon
                                    key={`hex-${c}-${r}`}
                                    state={hexState}
                                    onClick={() => console.log(`คลิกที่ช่อง: คอลัมน์ ${c}, แถว ${r}`)}
                                />
                            );
                        })}
                    </Box>
                ))}
            </Box>

            {/* 👉 แผงข้อมูลด้านขวา (Player 2) */}
            <PlayerPanel
                playerName="Player 2 (Dark)"
                themeColor="violet"
                borderColor="#7048E8"
                budget={10000}
                spawnsLeft={47}
                isActive={currentTurn === 2}
            />

            {/* 👇 ปุ่มจบเทิร์น ย้ายมาไว้ด้านล่างสุดตรงกลาง */}
            <Button
                style={{
                    position: 'absolute',
                    bottom: '40px', // 👈 เปลี่ยนเป็น bottom เพื่อเกาะขอบล่าง
                    left: '50%',
                    transform: 'translateX(-50%)',
                    zIndex: 10
                }}
                onClick={() => setCurrentTurn(currentTurn === 1 ? 2 : 1)}
            >
                จบเทิร์น (สลับไป P{currentTurn === 1 ? 2 : 1})
            </Button>
        </Box>
    );
}