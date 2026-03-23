export interface PurchasePanelProps {
    isActive: boolean;
    themeColor: string;
    borderColor: string;
    selectedHex: { col: number, row: number } | null;
    onBuy: () => void;
    onSkip: () => void; // ฟังก์ชันข้าม
    canAfford: boolean;
    hasPurchased?: boolean;
}