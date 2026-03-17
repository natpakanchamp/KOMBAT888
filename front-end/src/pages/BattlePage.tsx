// src/pages/BattlePage.tsx
import { Center, Box } from '@mantine/core';
import { useParams } from 'react-router-dom';
import { Hexagon } from '../components/Hexagon';
import type { HexState } from '../type/HexState'

// 👈 Import รูปตัวละครของคุณมาทดสอบ
import goldenGrass from "../assets/golden_grass.png";
import archer from "../assets/archer.png";

export default function BattlePage() {
    const { roomId } = useParams<{ roomId: string }>();
    const rows = 8;
    const cols = 8;

    const getInitialState = (c: number, r: number): HexState => {
        // 5 ช่องมุมซ้ายบน (ฝั่งแสง)
        if (c < 2 && r < 2) return 'LIGHT'; // หรือ TURNING_LIGHT ตามที่คุณตั้งไว้
        if (c === 2 && r === 0) return 'LIGHT';

        // 5 ช่องมุมขวาล่าง (ฝั่งมืด)
        if (c > 5 && r > 5) return 'DARK'; // หรือ TURNING_DARK ตามที่คุณตั้งไว้
        if (c === 5 && r === 7) return 'DARK';

        return 'NEUTRAL';
    };

    // 👇 1. แก้ฟังก์ชันนี้ให้รับค่า state แทนพิกัด
    const getUnitImage = (state: HexState) => {
        // ถ้าเป็นฝั่งแสง (สีเหลือง/เขียว) ให้แสดงรูป Saber
        if (state === 'LIGHT' || state === 'TURNING_LIGHT') {
            return goldenGrass;
        }
        // ถ้าเป็นฝั่งมืด (สีม่วง/แดง) ให้แสดงรูป Archer
        if (state === 'DARK' || state === 'TURNING_DARK') {
            return archer;
        }
        // ถ้าเป็นสีเทากลาง ไม่ต้องใส่รูป
        return undefined;
    };

    return (
        <Center style={{ minHeight: '100vh', backgroundColor: 'transparent' }}>
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
                            // 👇 2. ดึงสถานะของช่องนี้มาก่อน
                            const hexState = getInitialState(c, r);

                            return (
                                <Hexagon
                                    key={`hex-${c}-${r}`}
                                    state={hexState}
                                    image={getUnitImage(hexState)} // 👇 3. โยนสถานะเข้าไปเพื่อดึงรูป
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