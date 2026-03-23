// src/props/SetupModalProps.tsx
export interface SetupModalProps {
    opened: boolean;
    onClose: () => void;
    onSave: (strategy: string) => void;
    onRun?: (strategy: string) => void; // 👈 เพิ่มบรรทัดนี้เข้าไป
    unitType: string;
}