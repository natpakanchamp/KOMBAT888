// src/props/HexagonProps.ts
import type { HexState } from '../type/HexState';
import type { UnitData } from '../components/MinionToken';

export interface HexagonProps {
    state: HexState;
    onClick?: () => void;
    isSelected?: boolean;
    unit?: UnitData | null; // ตัวละครที่อยู่บน hex นี้
}