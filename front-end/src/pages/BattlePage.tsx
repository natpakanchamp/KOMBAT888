import { Box, Button, Stack, Text, Paper } from '@mantine/core';
import { useState, useMemo, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Howl } from 'howler';

import { Hexagon } from '../components/Hexagon';
import { PlayerPanel } from '../components/PlayerPanel';
import { PurchasePanel } from '../components/PurchasePanel';
import { SpawnMinionModal } from '../components/SpawnMinionModal';
import type { HexState } from '../type/HexState';
import type { UnitData } from '../components/MinionToken';
import { setBattleHowl } from '../hooks/useBGM';
import {useSparkleTrail} from "../hooks/useSparkleTrail.ts";

const ROWS = 8;
const COLS = 8;
const OFFSET_X = -16;
const STAGGER_Y = 28;
const GAP_Y = -2;

const getInitialState = (c: number, r: number): HexState => {
    if (r === 0 && (c === 0 || c === 1 || c === 2)) return 'LIGHT';
    if (r === 1 && (c === 0 || c === 1)) return 'LIGHT';

    if (r === ROWS - 1 && (c === COLS - 1 || c === COLS - 2 || c === COLS - 3)) return 'DARK';
    if (r === ROWS - 2 && (c === COLS - 1 || c === COLS - 2)) return 'DARK';

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

function ownershipToBoard(ownership: number[][]): Record<string, HexState> {
    const b: Record<string, HexState> = {};
    for (let r = 0; r < ownership.length; r++) {
        for (let c = 0; c < ownership[r].length; c++) {
            const v = ownership[r][c];
            b[`${c}-${r}`] = v === 1 ? 'LIGHT' : v === 2 ? 'DARK' : 'NEUTRAL';
        }
    }
    return b;
}

export default function BattlePage() {
    const { roomId } = useParams<{ roomId: string }>();
    const isSpectator = roomId ? sessionStorage.getItem(`isSpectator_${roomId}`) === 'true' : false;
    const myPlayer = roomId ? Number(sessionStorage.getItem(`playerNumber_${roomId}`)) || 0 : 0;

    const [fadeIn, setFadeIn] = useState(true);
    const [currentPlayer, setCurrentPlayer] = useState<number>(1);
    const isMyTurn = myPlayer === currentPlayer;
    const canAct = isMyTurn && !isSpectator;

    const [units, setUnits] = useState<UnitData[]>([]);
    const [turnCount, setTurnCount] = useState<number>(1);
    const [maxTurns, setMaxTurns] = useState<number>(0);
    const [hexCost, setHexCost] = useState<number>(750);
    const [spawnCost, setSpawnCost] = useState<number>(500);
    const [hasPurchasedThisTurn, setHasPurchasedThisTurn] = useState<boolean>(false);

    const [board, setBoard] = useState<Record<string, HexState>>(initializeBoard());
    const [selectedHex, setSelectedHex] = useState<{ col: number; row: number } | null>(null);
    const [hexToSpawn, setHexToSpawn] = useState<{ col: number; row: number } | null>(null);

    const [p1Budget, setP1Budget] = useState<number>(10000);
    const [p2Budget, setP2Budget] = useState<number>(4000);

    const stompRef = useRef<Client | null>(null);

    const p1SelectedMinions = ['Saber', 'Archer'];
    const p2SelectedMinions = ['Lancer', 'Caster', 'Berserker'];

    useSparkleTrail();

    useEffect(() => {
        const timer = setTimeout(() => setFadeIn(false), 100);
        return () => clearTimeout(timer);
    }, []);

    useEffect(() => {
        const battleBGM = new Howl({
            src: ['/bgm/battle-bgm.mp3'],
            loop: true,
            volume: 0.1,
            mute: sessionStorage.getItem('bgmMuted') === 'true',
        });

        setBattleHowl(battleBGM);

        if (sessionStorage.getItem('bgmMuted') !== 'true') {
            battleBGM.play();
        }

        return () => {
            battleBGM.stop();
            setBattleHowl(null);
        };
    }, []);

    useEffect(() => {
        if (!roomId) return;

        const client = new Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: () => {
                client.subscribe(`/topic/game/${roomId}`, msg => {
                    const state = JSON.parse(msg.body);

                    if (state.hexOwnership) setBoard(ownershipToBoard(state.hexOwnership));
                    if (state.units) setUnits(state.units);
                    if (state.p1Budget !== undefined) setP1Budget(state.p1Budget);
                    if (state.p2Budget !== undefined) setP2Budget(state.p2Budget);
                    if (state.currentPlayer !== undefined) setCurrentPlayer(state.currentPlayer);
                    if (state.currentTurn !== undefined) setTurnCount(state.currentTurn);
                    if (state.maxTurns !== undefined) setMaxTurns(state.maxTurns);
                });
            },
        });

        client.activate();
        stompRef.current = client;

        return () => {
            void client.deactivate();
        };
    }, [roomId]);

    useEffect(() => {
        if (!roomId) return;

        fetch(`/api/game/${roomId}/config`)
            .then(r => (r.ok ? r.json() : null))
            .then(cfg => {
                if (!cfg) return;
                if (cfg.hexPurchaseCost !== undefined) setHexCost(cfg.hexPurchaseCost);
                if (cfg.spawnCost !== undefined) setSpawnCost(cfg.spawnCost);
            })
            .catch(() => {});
    }, [roomId]);

    // ส่งไปหน้า Stat
    useEffect(() => {
        if (!roomId) return;
        //
        fetch(`/api/game/${roomId}/state`)
            .then(r => (r.ok ? r.json() : null))
            .then(state => {
                if (!state) return;

                if (state.hexOwnership) setBoard(ownershipToBoard(state.hexOwnership));
                if (state.units) setUnits(state.units);
                if (state.p1Budget !== undefined) setP1Budget(state.p1Budget);
                if (state.p2Budget !== undefined) setP2Budget(state.p2Budget);
                if (state.currentPlayer !== undefined) setCurrentPlayer(state.currentPlayer);
                if (state.currentTurn !== undefined) setTurnCount(state.currentTurn);
                if (state.maxTurns !== undefined) setMaxTurns(state.maxTurns);
            })
            .catch(() => {});
    }, [roomId]);

    const getNeighbors = (c: number, r: number) => {
        const isEvenCol = c % 2 === 0;
        const directions = isEvenCol
            ? [[0, -1], [0, 1], [-1, 0], [-1, 1], [1, 0], [1, 1]]
            : [[0, -1], [0, 1], [-1, -1], [-1, 0], [1, -1], [1, 0]];

        return directions.map(([dc, dr]) => ({ nc: c + dc, nr: r + dr }));
    };

    const purchasableHexes = useMemo(() => {
        const validHexes = new Set<string>();
        if (!canAct || hasPurchasedThisTurn) return validHexes;

        const myState = myPlayer === 1 ? 'LIGHT' : 'DARK';

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
    }, [board, canAct, myPlayer, hasPurchasedThisTurn]);

    const handleHexagonClick = (c: number, r: number) => {
        if (!canAct) return;

        const key = `${c}-${r}`;
        const clickedState = board[key];
        const myColor = myPlayer === 1 ? 'LIGHT' : 'DARK';
        const isOccupied = units.some(u => u.col === c && u.row === r);

        if (purchasableHexes.has(key)) {
            setSelectedHex({ col: c, row: r });
        } else if (clickedState === myColor && !isOccupied) {
            setSelectedHex(null);
            setHexToSpawn({ col: c, row: r });
        } else {
            setSelectedHex(null);
        }
    };

    const handleBuyHex = async () => {
        if (!canAct || !selectedHex || hasPurchasedThisTurn || !roomId) return;

        const res = await fetch(`/api/game/${roomId}/buy-hex`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                player: myPlayer,
                row: selectedHex.row,
                col: selectedHex.col,
            }),
        });

        if (res.ok) {
            setHasPurchasedThisTurn(true);
            setSelectedHex(null);
        }
    };

    const handleSkipHex = () => {
        if (!canAct || hasPurchasedThisTurn) return;
        setHasPurchasedThisTurn(true);
        setSelectedHex(null);
    };

    const handleConfirmSpawn = async (minionClass: string, _cost: number) => {
        void _cost;

        if (!canAct || !hexToSpawn || !roomId) {
            // กรณีที่ข้อมูลไม่ครบหรือไม่สามารถกระทำได้ ให้ปิด Modal และรีเซ็ตสถานะ
            return;
        }


        await fetch(`/api/game/${roomId}/spawn`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                player: myPlayer,
                minionType: minionClass,
                row: hexToSpawn.row,
                col: hexToSpawn.col,
            }),
        });

        setHexToSpawn(null);
        setSelectedHex(null);
        setHasPurchasedThisTurn(false);
    };

    const handleEndTurn = async () => {
        if (!canAct || !roomId) return;

        try {
            await fetch(`/api/game/${roomId}/next-turn`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ player: myPlayer }),
            });

            setSelectedHex(null);
            setHasPurchasedThisTurn(false);
            setHexToSpawn(null);
        } catch (e) {
            console.error('Failed to end turn', e);
        }
    };

    return (
        <Box
            style={{
                minHeight: '100vh',
                width: '100%',
                backgroundColor: 'rgba(0, 0, 0, 0.6)',
                display: 'flex',
                flexDirection: 'row',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '0 40px',
                position: 'fixed',
                backdropFilter: 'blur(5px)',
                inset: 0,
                zIndex: 1,
            }}
        >
            <Box
                style={{
                    position: 'fixed',
                    inset: 0,
                    zIndex: 9999,
                    background: 'black',
                    opacity: fadeIn ? 1 : 0,
                    transition: 'opacity 2s ease',
                    pointerEvents: 'none',
                }}
            />

            <Paper
                radius="md"
                style={{
                    position: 'absolute',
                    top: '30px',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    padding: '8px 32px',
                    backgroundColor: 'rgba(20, 20, 20, 0.85)',
                    border: '1px solid #444',
                    boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
                    zIndex: 10,
                }}
            >
                <Text size="xl" fw={800} c="white" style={{ letterSpacing: '2px' }}>
                    TURN {turnCount} / {maxTurns}
                </Text>
            </Paper>

            {hexToSpawn && (
                <SpawnMinionModal
                    opened={!!hexToSpawn}
                    onClose={() => setHexToSpawn(null)}
                    col={hexToSpawn.col}
                    row={hexToSpawn.row}
                    budget={myPlayer === 1 ? p1Budget : p2Budget}
                    spawnCost={spawnCost}
                    availableMinions={myPlayer === 1 ? p1SelectedMinions : p2SelectedMinions}
                    themeColor={myPlayer === 1 ? 'yellow' : 'violet'}
                    onConfirmSpawn={handleConfirmSpawn}
                />
            )}

            <Stack gap="md">
                <PlayerPanel
                    playerName="Player 1 (Light)"
                    themeColor="yellow"
                    borderColor="#FAB005"
                    budget={p1Budget}
                    isActive={currentPlayer === 1}
                />
                {myPlayer === 1 && isMyTurn && (
                    <PurchasePanel
                        isActive={true}
                        themeColor="yellow"
                        borderColor="#FAB005"
                        selectedHex={selectedHex}
                        onBuy={handleBuyHex}
                        onSkip={handleSkipHex}
                        canAfford={p1Budget >= hexCost}
                        hasPurchased={hasPurchasedThisTurn}
                    />
                )}
            </Stack>

            <Box style={{ display: 'flex', flexDirection: 'row' }}>
                {Array.from({ length: COLS }).map((_, c) => (
                    <Box
                        key={`col-${c}`}
                        style={{
                            display: 'flex',
                            flexDirection: 'column',
                            marginLeft: c === 0 ? '0px' : `${OFFSET_X}px`,
                            marginTop: c % 2 === 0 ? `${STAGGER_Y}px` : '0px',
                        }}
                    >
                        {Array.from({ length: ROWS }).map((_, r) => {
                            const key = `${c}-${r}`;
                            const actualState = board[key];
                            const isPurchasable = purchasableHexes.has(key);
                            const isSelected = selectedHex?.col === c && selectedHex?.row === r;

                            let displayState = actualState;
                            if (actualState === 'NEUTRAL' && isPurchasable) {
                                displayState = myPlayer === 1 ? 'TURNING_LIGHT' : 'TURNING_DARK';
                            }

                            const unitOnHex = units.find(u => u.col === c && u.row === r) ?? null;

                            return (
                                <Box key={`hex-wrapper-${c}-${r}`} style={{ marginTop: r === 0 ? '0px' : `${GAP_Y}px` }}>
                                    <Hexagon
                                        state={displayState}
                                        onClick={() => handleHexagonClick(c, r)}
                                        isSelected={isSelected}
                                        unit={unitOnHex}
                                    />
                                </Box>
                            );
                        })}
                    </Box>
                ))}
            </Box>

            <Stack gap="md">
                <PlayerPanel
                    playerName="Player 2 (Dark)"
                    themeColor="violet"
                    borderColor="#7048E8"
                    budget={p2Budget}
                    isActive={currentPlayer === 2}
                />
                {myPlayer === 2 && isMyTurn && (
                    <PurchasePanel
                        isActive={true}
                        themeColor="violet"
                        borderColor="#7048E8"
                        selectedHex={selectedHex}
                        onBuy={handleBuyHex}
                        onSkip={handleSkipHex}
                        canAfford={p2Budget >= hexCost}
                        hasPurchased={hasPurchasedThisTurn}
                    />
                )}
            </Stack>

            {canAct && (
                <Button
                    style={{
                        position: 'absolute',
                        bottom: '40px',
                        left: '50%',
                        transform: 'translateX(-50%)',
                        zIndex: 10,
                    }}
                    onClick={handleEndTurn}
                >
                    จบเทิร์น
                </Button>
            )}
        </Box>
    );
}