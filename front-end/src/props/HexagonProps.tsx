// src/props/HexagonProps.ts
import type { HexState } from '../type/HexState';

export interface HexagonProps {
    state: HexState;
    onClick?: () => void;
    isSelected?: boolean; // 👈 เปลี่ยนมาใช้แค่ boolean เช็คว่ากำลังถูกเลือกอยู่ไหม
}