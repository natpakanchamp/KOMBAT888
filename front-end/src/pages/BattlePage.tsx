// src/pages/BattlePage.tsx
import { Center, Box } from '@mantine/core';
import { useParams } from 'react-router-dom';
import { Hexagon } from '../components/Hexagon';
import type { HexState } from '../type/HexState';

export default function BattlePage() {
    const { roomId } = useParams<{ roomId: string }>();
    const rows = 8;
    const cols = 8;

    const getInitialState = (c: number, r: number): HexState => {
        // 5 ช่องมุมซ้ายบน (ฝั่งแสง)
        if (c < 2 && r < 2) return 'LIGHT';
        if (c === 2 && r === 0) return 'LIGHT';

        // 5 ช่องมุมขวาล่าง (ฝั่งมืด)
        if (c > 5 && r > 5) return 'DARK';
        if (c === 5 && r === 7) return 'DARK';

        return 'NEUTRAL';
    };

    return (
        <Center
            style={{
                minHeight: '100vh',
                width: '100%', // บังคับให้ฟิล์มกางเต็มความกว้างจอ
                // 👇 ทำตัวเป็นฟิล์มกรองแสงสีดำ โปร่งใส 60% เพื่อดรอปความสว่างของรูป Main Layout ด้านหลัง
                backgroundColor: 'rgba(0, 0, 0, 0.6)',
            }}
        >
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
        </Center>
    );
}