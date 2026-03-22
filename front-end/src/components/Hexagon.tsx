// src/components/Hexagon.tsx
import { UnstyledButton } from '@mantine/core';
import type { HexagonProps } from '../props/HexagonProps';

export function Hexagon({ state, onClick, isSelected }: HexagonProps) {

    // สีจะเปลี่ยนไปตาม State ที่ส่งเข้ามา (รวมถึง TURNING_LIGHT/DARK ด้วย)
    const getFillColor = () => {
        switch (state) {
            case 'LIGHT': return 'rgba(250, 176, 5, 0.8)';
            case 'DARK': return 'rgba(112, 72, 232, 0.8)';
            case 'TURNING_LIGHT': return 'rgba(180, 230, 180, 0.6)'; // สีเขียวอ่อนๆ (เปลี่ยนสีได้ตามชอบ)
            case 'TURNING_DARK': return 'rgba(240, 180, 180, 0.6)'; // สีแดงอ่อนๆ (เปลี่ยนสีได้ตามชอบ)
            case 'NEUTRAL': return 'rgba(255, 255, 255, 0.5)';
            default: return 'transparent';
        }
    };

    const fill = getFillColor();
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
                transition: 'all 0.2s ease',
                position: 'relative',

                // 👇 ขยายขนาดถ่าถูกคลิกเลือกอยู่
                transform: isSelected ? 'scale(1.15)' : 'scale(1)',
                zIndex: isSelected ? '10' : '1',
            }}
            onMouseEnter={(e) => {
                if (!isSelected) {
                    e.currentTarget.style.transform = 'scale(1.1)';
                    e.currentTarget.style.zIndex = '10';
                }
            }}
            onMouseLeave={(e) => {
                if (!isSelected) {
                    e.currentTarget.style.transform = 'scale(1)';
                    e.currentTarget.style.zIndex = '1';
                }
            }}
        >
            <svg width="64" height="56" viewBox="0 0 64 56" style={{ overflow: 'visible' }}>
                <polygon
                    points={hexPoints}
                    fill={fill}
                    style={{ transition: 'all 0.3s ease' }}
                />
                <polygon
                    points={hexPoints}
                    fill="none"
                    stroke="black"
                    strokeWidth="2"
                />
            </svg>
        </UnstyledButton>
    );
}