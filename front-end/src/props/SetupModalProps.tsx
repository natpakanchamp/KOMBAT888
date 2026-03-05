export interface SetupModalProps {
    opened: boolean;
    onClose: () => void;
    onSave: (strategy: string) => void;
    unitType: string;
}