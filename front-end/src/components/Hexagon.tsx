// src/components/Hexagon.tsx
import { UnstyledButton } from '@mantine/core';
import { useId } from 'react'; // 👈 นำเข้า useId
// import type { HexState } from '../type/HexState'
import type { HexagonProps } from '../props/HexagonProps';

export function Hexagon({ state, image, onClick }: HexagonProps) {
    const clipId = useId(); // 👈 สร้าง ID เฉพาะสำหรับช่องนี้ เพื่อไม่ให้หน้ากากตีกัน

    const getColors = () => {
        switch (state) {
            case 'LIGHT': return { fill: 'rgba(250, 176, 5, 0.8)', stroke: '#FAB005' };
            case 'DARK': return { fill: 'rgba(112, 72, 232, 0.8)', stroke: '#7048E8' };
            case 'TURNING_LIGHT': return { fill: 'rgba(180, 230, 180, 0.6)', stroke: '#88C888' };
            case 'TURNING_DARK': return { fill: 'rgba(240, 180, 180, 0.6)', stroke: '#D88888' };
            case 'NEUTRAL':
            default: return { fill: 'transparent', stroke: '#A6A7AB' };
        }
    };

    const { fill, stroke } = getColors();

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
                position: 'relative', // เผื่อการจัดวาง Z-index
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
                <defs>
                    {/* สร้างหน้ากาก (Clip Path) เป็นรูปหกเหลี่ยม */}
                    <clipPath id={clipId}>
                        <polygon points={hexPoints} />
                    </clipPath>
                </defs>

                {/* เลเยอร์ 1: สีพื้นหลัง (แสดงด้านหลังรูปตัวละคร) */}
                <polygon points={hexPoints} fill={fill} style={{ transition: 'all 0.3s ease' }} />

                {/* เลเยอร์ 2: รูปภาพตัวละคร (ถูกตัดขอบตามหน้ากากหกเหลี่ยม) */}
                {image && (
                    <image
                        href={image}
                        width="64"
                        height="56"
                        clipPath={`url(#${clipId})`}
                        preserveAspectRatio="xMidYMid slice" // จัดรูปให้อยู่ตรงกลางพอดี
                        style={{ pointerEvents: 'none' }} // ป้องกันรูปไปบังการคลิก
                    />
                )}

                {/* เลเยอร์ 3: เส้นขอบ (วาดทับบนสุดเพื่อให้ขอบคมชัด ไม่ถูกรูปบัง) */}
                <polygon points={hexPoints} fill="none" stroke={stroke} strokeWidth="2" style={{ transition: 'all 0.3s ease' }} />
            </svg>
        </UnstyledButton>
    );
}