import type { HexState } from "../type/HexState"

export interface HexagonProps {
    state: HexState;
    onClick?: () => void;
}