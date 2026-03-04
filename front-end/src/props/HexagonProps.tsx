import type { HexState } from "../type/HexState"

export interface HexagonProps {
    state: HexState;
    image?: string;
    onClick?: () => void;
}