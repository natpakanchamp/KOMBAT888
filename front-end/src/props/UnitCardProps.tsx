export interface UnitCardProps {
    strategy: string;
    description: string;
    charImg: string;
    backImg: string;
    onSelect: () => void;
    onFlip?: (flipped: boolean) => void;
    isSelected?: boolean;
    hasStrategy?: boolean;
    initialFlipped?: boolean;
}
