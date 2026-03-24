// src/pages/BattlePage.tsx

import { Box, Button, Stack, Text, Paper } from '@mantine/core';

import { useState, useMemo, useEffect, useRef } from 'react';

import { useParams } from 'react-router-dom';

import { Client } from '@stomp/stompjs';

import SockJS from 'sockjs-client';

import { Hexagon } from '../components/Hexagon';

import { PlayerPanel } from '../components/PlayerPanel';

import { PurchasePanel } from '../components/PurchasePanel';

import { SpawnMinionModal } from '../components/SpawnMinionModal';

import type { HexState } from '../type/HexState';

import type { UnitData } from '../components/MinionToken';

import { setBattleHowl } from '../hooks/useBGM';



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



// แปลง hexOwnership[row][col] (0=neutral,1=light,2=dark) เป็น board Record

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

    const isSpectator = roomId ? sessionStorage.getItem(`isSpectator_${roomId}`) === "true" : false;

    const [fadeIn, setFadeIn] = useState(true);

    const [currentTurn, setCurrentTurn] = useState<number>(0);



// State จาก backend

    const [units, setUnits] = useState<UnitData[]>([]);

    const stompRef = useRef<Client | null>(null);



    useEffect(() => {

// เริ่มจากจอดำ แล้วค่อยๆ fade out ใน 2 วินาที

        const timer = setTimeout(() => setFadeIn(false), 100);

        return () => clearTimeout(timer);

    }, []);



// เล่น BGM เมื่อเข้าหน้า และหยุดเมื่อออกจากหน้า

    useEffect(() => {

        const battleBGM = new Howl({

            src: ['/bgm/battle-bgm.mp3'],

            loop: true,

            volume: 0.1,

            mute: sessionStorage.getItem("bgmMuted") === "true",

        });

        setBattleHowl(battleBGM); // register ให้ GearMenu mute ได้

        if (sessionStorage.getItem("bgmMuted") !== "true") {

            battleBGM.play();

        }

        return () => {

            battleBGM.stop();

            setBattleHowl(null); // unregister เมื่อออกจากหน้า

        };

    }, []);



// Subscribe WebSocket /topic/game/{roomId}

    useEffect(() => {

        if (!roomId) return;

        const client = new Client({

            webSocketFactory: () => new SockJS('/ws'),

            onConnect: () => {

                client.subscribe(`/topic/game/${roomId}`, (msg) => {

                    const state = JSON.parse(msg.body);

// อัปเดต board จาก hexOwnership

                    if (state.hexOwnership) {

                        setBoard(ownershipToBoard(state.hexOwnership));

                    }

// อัปเดต units

                    if (state.units) setUnits(state.units);

// อัปเดต budget

                    if (state.p1Budget !== undefined) setP1Budget(state.p1Budget);

                    if (state.p2Budget !== undefined) setP2Budget(state.p2Budget);

// อัปเดต turn

                    if (state.currentTurn !== undefined) setTurnCount(state.currentTurn);

                });

            },

        });

        client.activate();

        stompRef.current = client;

        return () => { client.deactivate(); };

    }, [roomId]);



// ดึง state ครั้งแรก

    useEffect(() => {

        if (!roomId) return;

        fetch(`/api/game/${roomId}/state`)

            .then(r => r.ok ? r.json() : null)

            .then(state => {

                if (!state) return;

                if (state.hexOwnership) setBoard(ownershipToBoard(state.hexOwnership));

                if (state.units) setUnits(state.units);

                if (state.p1Budget !== undefined) setP1Budget(state.p1Budget);

                if (state.p2Budget !== undefined) setP2Budget(state.p2Budget);

                if (state.currentTurn !== undefined) setTurnCount(state.currentTurn);

            })

            .catch(() => {});

    }, [roomId]);



// State นับหมายเลขเทิร์น (เริ่มที่ 1)

    const [turnCount, setTurnCount] = useState<number>(1);

    const [hasPurchasedThisTurn, setHasPurchasedThisTurn] = useState<boolean>(false);



    const [board, setBoard] = useState<Record<string, HexState>>(initializeBoard());

    const [selectedHex, setSelectedHex] = useState<{ col: number, row: number } | null>(null);

    const [hexToSpawn, setHexToSpawn] = useState<{ col: number, row: number } | null>(null);



    const [p1Budget, setP1Budget] = useState(10000);

    const [p2Budget, setP2Budget] = useState(4000);



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

        if (isSpectator) return;

        const key = `${c}-${r}`;

        const clickedState = board[key];

        const myColor = currentTurn === 0 ? 'LIGHT' : 'DARK';



        if (purchasableHexes.has(key)) {

            setSelectedHex({ col: c, row: r });

        } else if (clickedState === myColor) {

            setSelectedHex(null);

            setHexToSpawn({ col: c, row: r });

        } else {

            setSelectedHex(null);

        }

    };



    const handleBuyHex = async () => {

        if (isSpectator || !selectedHex || hasPurchasedThisTurn || !roomId) return;

        const player = currentTurn === 0 ? 1 : 2;

        const res = await fetch(`/api/game/${roomId}/buy-hex`, {

            method: 'POST',

            headers: { 'Content-Type': 'application/json' },

            body: JSON.stringify({ player, row: selectedHex.row, col: selectedHex.col }),

        });

        if (res.ok) {

// board/budget จะ update ผ่าน WebSocket broadcast

            setHasPurchasedThisTurn(true);

// เปิด modal spawn ทันที

            setHexToSpawn({ col: selectedHex.col, row: selectedHex.row });

            setSelectedHex(null);

        }

    };



    const handleSkipHex = () => {

        if (isSpectator || hasPurchasedThisTurn) return;

        setHasPurchasedThisTurn(true);

        setSelectedHex(null);

    };



    const handleConfirmSpawn = async (minionClass: string, _cost: number) => {

        if (isSpectator || !hexToSpawn || !roomId) return;

        const player = currentTurn === 0 ? 1 : 2;

        await fetch(`/api/game/${roomId}/spawn`, {

            method: 'POST',

            headers: { 'Content-Type': 'application/json' },

            body: JSON.stringify({ player, minionType: minionClass, row: hexToSpawn.row, col: hexToSpawn.col }),

        });

// units จะ update ผ่าน WebSocket broadcast

        setHexToSpawn(null);

        if (currentTurn === 1) setTurnCount(prev => prev + 1);

        setCurrentTurn(currentTurn === 0 ? 1 : 0);

        setSelectedHex(null);

        setHasPurchasedThisTurn(false);

    };



    const handleEndTurn = () => {

        if (currentTurn === 1) {

            setTurnCount(prev => prev + 1);

        }

        setCurrentTurn(currentTurn === 0 ? 1 : 0);

        setSelectedHex(null);

        setHasPurchasedThisTurn(false);

        setHexToSpawn(null);

    };



    return (

        <Box

            style={{

                minHeight: '100vh', width: '100%', backgroundColor: 'rgba(0, 0, 0, 0.6)',

                display: 'flex', flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',

                padding: '0 40px', position: 'fixed', backdropFilter: "blur(5px)", inset: 0, zIndex: 1,

            }}

        >

            {/* ── Fade in from black ── */}

            <Box style={{

                position: "fixed", inset: 0, zIndex: 9999,

                background: "black",

                opacity: fadeIn ? 1 : 0,

                transition: "opacity 2s ease",

                pointerEvents: "none",

            }} />



            {/* กล่องแสดงหมายเลข Turn ด้านบนตรงกลาง */}

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

                    TURN {turnCount}

                </Text>

            </Paper>



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



            {/* ฝั่งซ้าย (Player 1) */}

            <Stack gap="md">

                <PlayerPanel

                    playerName="Player 1 (Light)" themeColor="yellow" borderColor="#FAB005"

                    budget={p1Budget} isActive={currentTurn === 0}

                />

                <PurchasePanel

                    isActive={currentTurn === 0} themeColor="yellow" borderColor="#FAB005"

                    selectedHex={currentTurn === 0 ? selectedHex : null}

                    onBuy={handleBuyHex} onSkip={handleSkipHex}

                    canAfford={p1Budget >= HEX_COST} hasPurchased={currentTurn === 0 && hasPurchasedThisTurn}

                />

            </Stack>



            {/* กระดานเกมตรงกลาง */}

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



                            const unitOnHex = units.find(u => u.col === c && u.row === r) ?? null;

                            return (

                                <Hexagon

                                    key={`hex-${c}-${r}`} state={displayState}

                                    onClick={() => handleHexagonClick(c, r)} isSelected={isSelected}

                                    unit={unitOnHex}

                                />

                            );

                        })}

                    </Box>

                ))}

            </Box>



            {/* ฝั่งขวา (Player 2) */}

            <Stack gap="md">

                <PlayerPanel

                    playerName="Player 2 (Dark)" themeColor="violet" borderColor="#7048E8"

                    budget={p2Budget} isActive={currentTurn === 1}

                />

                <PurchasePanel

                    isActive={currentTurn === 1} themeColor="violet" borderColor="#7048E8"

                    selectedHex={currentTurn === 1 ? selectedHex : null}

                    onBuy={handleBuyHex} onSkip={handleSkipHex}

                    canAfford={p2Budget >= HEX_COST} hasPurchased={currentTurn === 1 && hasPurchasedThisTurn}

                />

            </Stack>



            {/* ปุ่มจบเทิร์น (สำหรับกดข้ามกรณีไม่ได้ซื้อมินเนียน) */}

            <Button

                style={{ position: 'absolute', bottom: '40px', left: '50%', transform: 'translateX(-50%)', zIndex: 10 }}

                onClick={handleEndTurn}

            >

                จบเทิร์น (สลับไป P{currentTurn === 0 ? 2 : 1})

            </Button>

        </Box>

    );

}