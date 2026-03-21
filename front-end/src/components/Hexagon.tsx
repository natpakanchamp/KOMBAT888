import { UnstyledButton } from '@mantine/core';
import type { HexagonProps } from '../props/HexagonProps';

// เอา image ออกจากการรับค่า Props เพราะเราไม่ได้ใช้แล้ว
export function Hexagon({ state, onClick }: HexagonProps) {

    // ปรับให้คืนค่าแค่สีพื้นหลัง (fill) อย่างเดียว เพราะเส้นขอบเราล็อคเป็นสีดำแล้ว
    const getFillColor = () => {
        switch (state) {
            case 'LIGHT': return 'rgba(250, 176, 5, 0.8)';
            case 'DARK': return 'rgba(112, 72, 232, 0.8)';
            case 'TURNING_LIGHT': return 'rgba(180, 230, 180, 0.6)';
            case 'TURNING_DARK': return 'rgba(240, 180, 180, 0.6)';
            case 'NEUTRAL': return 'rgba(255, 255, 255, 0.5)';
            default: return 'transparent';
        }
    };

    const fill = getFillColor();

    // พิกัดของหกเหลี่ยมแนวนอน (Flat-Topped)
    const hexPoints = "16,0 48,0 64,28 48,56 16,56 0,28";

    return (
        <UnstyledButton
            onClick={onClick}
            style={{
                width: '64px',
                height: '56px',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                marginBottom: '-1px',
                transition: 'transform 0.2s',
                position: 'relative',
            }}
            onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'scale(1.1)';
                e.currentTarget.style.zIndex = '10';
            }}
            onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'scale(1)';
                e.currentTarget.style.zIndex = '1';
            }}
        >
            <svg width="64" height="56" viewBox="0 0 64 56" style={{ overflow: 'visible' }}>

                {/* เลเยอร์ 1: สีพื้นหลัง (แสดงตาม State) */}
                <polygon
                    points={hexPoints}
                    fill={fill}
                    style={{ transition: 'all 0.3s ease' }}
                />

                {/* เลเยอร์ 2: เส้นขอบ (วาดทับบนสุด ล็อคเป็นสีดำ) */}
                <polygon
                    points={hexPoints}
                    fill="none"
                    stroke="black"    /* 👈 เปลี่ยนเส้นขอบเป็นสีดำตรงนี้ */
                    strokeWidth="2"   /* 👈 ปรับความหนาของเส้นขอบได้ที่นี่ (เลขยิ่งเยอะยิ่งหนา) */
                    style={{ transition: 'all 0.3s ease' }}
                />

            </svg>
        </UnstyledButton>
    );
}