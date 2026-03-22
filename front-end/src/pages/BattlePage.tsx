// src/pages/BattlePage.tsx
import { Box, Button, Stack } from '@mantine/core';
import { useState, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { Hexagon } from '../components/Hexagon';
import { PlayerPanel } from '../components/PlayerPanel';
import { PurchasePanel } from '../components/PurchasePanel';
import { SpawnMinionModal } from '../components/SpawnMinionModal';
import type { HexState } from '../type/HexState';

const HEX_COST = 150;
const ROWS = 8;
const COLS = 8;

const getInitialState = (c: number, r: number): HexState => {
    if (c < 2 && r < 2) return 'LIGHT';
    if (c === 2 && r === 0) return 'LIGHT';
    if (c > 5 && r > 5) return 'DARK';
    if (c === 5 && r === 7) return 'DARK';
    return 'NEUTRAL';
};

const initializeBoard = () => {
    const initial: Record<string, HexState> = {};
    for (let c = 0; c < COLS; c++) {
        for (let r = 0; r < ROWS; r++) {
            initial[`${c}-${r}`] = getInitialState(c, r);
        }
    }
    return initial;
};

export default function BattlePage() {
    const { roomId } = useParams<{ roomId: string }>();
    const [currentTurn, setCurrentTurn] = useState<number>(0);
    const [hasPurchasedThisTurn, setHasPurchasedThisTurn] = useState<boolean>(false);

    const [board, setBoard] = useState<Record<string, HexState>>(initializeBoard());
    const [selectedHex, setSelectedHex] = useState<{ col: number, row: number } | null>(null);

    // State สำหรับคุมหน้าต่างลงมินเนียน (เด้งขึ้นมาหลังจากซื้อ Hex)
    const [hexToSpawn, setHexToSpawn] = useState<{ col: number, row: number } | null>(null);

    const [p1Budget, setP1Budget] = useState(10000);
    const [p1Spawns, setP1Spawns] = useState(47);
    const [p2Budget, setP2Budget] = useState(4000);
    const [p2Spawns, setP2Spawns] = useState(47);

    // จำลองรายชื่อมินเนียนที่ผู้เล่นเลือกมาจากหน้า Waiting Room
    const p1SelectedMinions = ['Saber', 'Archer'];
    const p2SelectedMinions = ['Lancer', 'Caster', 'Berserker'];

    const getNeighbors = (c: number, r: number) => {
        const isEvenCol = c % 2 === 0;
        const directions = isEvenCol
            ? [[0, -1], [0, 1], [-1, 0], [-1, 1], [1, 0], [1, 1]]
            : [[0, -1], [0, 1], [-1, -1], [-1, 0], [1, -1], [1, 0]];
        return directions.map(([dc, dr]) => ({ nc: c + dc, nr: r + dr }));
    };

    const purchasableHexes = useMemo(() => {
        const validHexes = new Set<string>();
        if (hasPurchasedThisTurn) return validHexes;

        const myState = currentTurn === 0 ? 'LIGHT' : 'DARK';

        Object.entries(board).forEach(([key, state]) => {
            if (state === myState) {
                const [c, r] = key.split('-').map(Number);
                getNeighbors(c, r).forEach(({ nc, nr }) => {
                    if (nc >= 0 && nc < COLS && nr >= 0 && nr < ROWS) {
                        const nKey = `${nc}-${nr}`;
                        if (board[nKey] === 'NEUTRAL') {
                            validHexes.add(nKey);
                        }
                    }
                });
            }
        });
        return validHexes;
    }, [board, currentTurn, hasPurchasedThisTurn]);

    const handleHexagonClick = (c: number, r: number) => {
        const key = `${c}-${r}`;
        if (purchasableHexes.has(key)) {
            setSelectedHex({ col: c, row: r });
        } else {
            setSelectedHex(null);
        }
    };

    const handleBuyHex = () => {
        if (!selectedHex || hasPurchasedThisTurn) return;

        if (currentTurn === 0) {
            setP1Budget(prev => prev - HEX_COST);
        } else {
            setP2Budget(prev => prev - HEX_COST);
        }

        setBoard(prev => ({
            ...prev,
            [`${selectedHex.col}-${selectedHex.row}`]: currentTurn === 0 ? 'LIGHT' : 'DARK'
        }));

        setHasPurchasedThisTurn(true);

        // พอซื้อที่ดินสำเร็จ ให้บันทึกพิกัดเพื่อเปิด Modal ลงมินเนียนต่อทันที
        setHexToSpawn({ col: selectedHex.col, row: selectedHex.row });
        setSelectedHex(null);
    };

    // ฟังก์ชันสำหรับกดข้ามการซื้อที่ดิน
    const handleSkipHex = () => {
        if (hasPurchasedThisTurn) return;
        setHasPurchasedThisTurn(true);
        setSelectedHex(null);
    };

    // ฟังก์ชันเมื่อกดยืนยันลงมินเนียนจากใน Modal
    const handleConfirmSpawn = (minionClass: string, cost: number) => {
        if (!hexToSpawn) return;

        if (currentTurn === 0) {
            setP1Budget(prev => prev - cost);
            setP1Spawns(prev => prev - 1);
        } else {
            setP2Budget(prev => prev - cost);
            setP2Spawns(prev => prev - 1);
        }

        console.log(`ผู้เล่น ${currentTurn + 1} ลง ${minionClass} ที่ช่อง [${hexToSpawn.col}, ${hexToSpawn.row}] จ่ายไป ${cost}`);

        setHexToSpawn(null); // ปิด Modal
    };

    return (
        <Box
            style={{
                minHeight: '100vh', width: '100%', backgroundColor: 'rgba(0, 0, 0, 0.6)',
                display: 'flex', flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
                padding: '0 40px', position: 'fixed', backdropFilter: "blur(5px)", inset: 0, zIndex: 1,
            }}
        >
            {/* 👇 หน้าต่างลงมินเนียน เด้งมาหลังซื้อ Hex */}
            {hexToSpawn && (
                <SpawnMinionModal
                    opened={!!hexToSpawn}
                    onClose={() => setHexToSpawn(null)}
                    col={hexToSpawn.col}
                    row={hexToSpawn.row}
                    budget={currentTurn === 0 ? p1Budget : p2Budget}
                    availableMinions={currentTurn === 0 ? p1SelectedMinions : p2SelectedMinions}
                    themeColor={currentTurn === 0 ? 'yellow' : 'violet'}
                    onConfirmSpawn={handleConfirmSpawn}
                />
            )}

            {/* 👈 ฝั่งซ้าย (Player 1) */}
            <Stack gap="md">
                <PlayerPanel
                    playerName="Player 1 (Light)" themeColor="yellow" borderColor="#FAB005"
                    budget={p1Budget} spawnsLeft={p1Spawns} isActive={currentTurn === 0}
                />
                <PurchasePanel
                    isActive={currentTurn === 0} themeColor="yellow" borderColor="#FAB005"
                    selectedHex={currentTurn === 0 ? selectedHex : null}
                    onBuy={handleBuyHex}
                    onSkip={handleSkipHex}
                    canAfford={p1Budget >= HEX_COST}
                    hasPurchased={currentTurn === 0 && hasPurchasedThisTurn}
                />
            </Stack>

            {/* 🎯 กระดานเกมตรงกลาง */}
            <Box style={{ display: 'flex', flexDirection: 'row' }}>
                {Array.from({ length: COLS }).map((_, c) => (
                    <Box key={`col-${c}`} style={{ display: 'flex', flexDirection: 'column', marginLeft: c === 0 ? '0px' : '-16px', marginTop: c % 2 === 0 ? '28px' : '0px' }}>
                        {Array.from({ length: ROWS }).map((_, r) => {
                            const key = `${c}-${r}`;
                            const actualState = board[key];
                            const isPurchasable = purchasableHexes.has(key);
                            const isSelected = selectedHex?.col === c && selectedHex?.row === r;

                            let displayState = actualState;
                            if (actualState === 'NEUTRAL' && isPurchasable) {
                                displayState = currentTurn === 0 ? 'TURNING_LIGHT' : 'TURNING_DARK';
                            }

                            return (
                                <Hexagon
                                    key={`hex-${c}-${r}`} state={displayState}
                                    onClick={() => handleHexagonClick(c, r)} isSelected={isSelected}
                                />
                            );
                        })}
                    </Box>
                ))}
            </Box>

            {/* 👉 ฝั่งขวา (Player 2) */}
            <Stack gap="md">
                <PlayerPanel
                    playerName="Player 2 (Dark)" themeColor="violet" borderColor="#7048E8"
                    budget={p2Budget} spawnsLeft={p2Spawns} isActive={currentTurn === 1}
                />
                <PurchasePanel
                    isActive={currentTurn === 1} themeColor="violet" borderColor="#7048E8"
                    selectedHex={currentTurn === 1 ? selectedHex : null}
                    onBuy={handleBuyHex}
                    onSkip={handleSkipHex}
                    canAfford={p2Budget >= HEX_COST}
                    hasPurchased={currentTurn === 1 && hasPurchasedThisTurn}
                />
            </Stack>

            {/* 👇 ปุ่มจบเทิร์น */}
            <Button
                style={{ position: 'absolute', bottom: '40px', left: '50%', transform: 'translateX(-50%)', zIndex: 10 }}
                onClick={() => {
                    setCurrentTurn(currentTurn === 0 ? 1 : 0);
                    setSelectedHex(null);
                    setHasPurchasedThisTurn(false); // รีเซ็ตสิทธิ์การซื้อ
                    setHexToSpawn(null); // รีเซ็ตหน้าต่างเผื่อค้าง
                }}
            >
                จบเทิร์น (สลับไป P{currentTurn === 0 ? 2 : 1})
            </Button>
        </Box>
    );
}