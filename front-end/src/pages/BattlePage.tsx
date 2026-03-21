// src/pages/BattlePage.tsx
// หน้าหลักสำหรับแสดงกระดานเกม เชื่อมต่อกับ Backend ผ่าน REST API และ WebSocket

import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Center, Box, Loader, Text } from '@mantine/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Hexagon } from '../components/Hexagon';
import type { HexState } from '../type/HexState';

import goldenGrass from "../assets/golden_grass.png";
import archer from "../assets/archer.png";

// ── Type ของ Unit ตรงกับ model/engine/Unit.java ──
// ใช้รับข้อมูลจาก API แล้วแสดงบนกระดาน
type UnitData = {
    id: number;         // id เฉพาะของ unit (นับจาก 1)
    owner: number;      // เจ้าของ: 1 = Player1 (แสง), 2 = Player2 (มืด)
    type: number;       // ประเภท: 1=Saber, 2=Archer, 3=Lancer, 4=Caster, 5=Berserker
    row: number;        // ตำแหน่งแถวบนกระดาน
    col: number;        // ตำแหน่งคอลัมน์บนกระดาน
    hp: number;         // เลือดปัจจุบัน (0-100)
    maxHp: number;      // เลือดสูงสุด (100)
    defense: number;    // ค่าเกราะ
    alive: boolean;     // คำนวณจาก hp > 0 ใน Unit.isAlive()
};

// ── Type ของ GameState ตรงกับ model/engine/GameState.java ──
// ข้อมูลสถานะเกมทั้งหมดที่ได้จาก GET /api/game/{roomId}/state
type GameStateData = {
    boardRows: number;          // จำนวนแถวของกระดาน (จาก GameEngine = 10)
    boardCols: number;          // จำนวนคอลัมน์ของกระดาน (จาก GameEngine = 10)
    currentTurn: number;        // เทิร์นปัจจุบัน (เริ่มที่ 1)
    maxTurns: number;           // เทิร์นสูงสุด (จาก config = 69)
    p1Budget: number;           // งบประมาณของ Player 1
    p2Budget: number;           // งบประมาณของ Player 2
    units: UnitData[];          // รายชื่อ unit ทั้งหมดในเกม
    hexOwnership: number[][];   // ความเป็นเจ้าของ hex แต่ละช่อง (0=กลาง, 1=P1, 2=P2)
};

export default function BattlePage() {
    // ดึง roomId จาก URL เช่น /battle/90uvg0 → roomId = "90uvg0"
    const { roomId } = useParams<{ roomId: string }>();

    // state เก็บข้อมูลเกมที่ได้จาก API
    // null = ยังโหลดอยู่, มีข้อมูล = แสดงกระดานได้แล้ว
    const [gameState, setGameState] = useState<GameStateData | null>(null);

    // ── Effect 1: โหลด Game State ครั้งแรกเมื่อเข้าหน้า ──
    useEffect(() => {
        if (!roomId) return;

        // เรียก GET /api/game/{roomId}/state
        // Vite proxy จะส่งต่อไปที่ localhost:8080/api/game/{roomId}/state
        fetch(`/api/game/${roomId}/state`)
            .then(res => {
                if (!res.ok) throw new Error(`HTTP error ${res.status}`);
                return res.json();
            })
            .then((data: GameStateData) => setGameState(data))
            .catch(err => console.error("Failed to load game state:", err));
    }, [roomId]); // รันใหม่ถ้า roomId เปลี่ยน

    // ── Effect 2: เชื่อมต่อ WebSocket รับ update real-time ──
    useEffect(() => {
        if (!roomId) return;

        // สร้าง STOMP client เชื่อมต่อผ่าน SockJS
        const client = new Client({
            // ใช้ relative path ผ่าน Vite proxy (/ws → localhost:8080/ws)
            webSocketFactory: () => new SockJS('/ws'),
            // ถ้า connection หลุด ให้ reconnect ทุก 2 วินาที
            reconnectDelay: 2000,
            onConnect: () => {
                // subscribe รับข้อมูลจาก /topic/game/{roomId}
                // Backend จะ broadcast มาทุกครั้งที่ GameService.broadcastState() ถูกเรียก
                client.subscribe(`/topic/game/${roomId}`, (msg) => {
                    try {
                        // แปลง JSON string → GameStateData object แล้วอัพเดต state
                        const data: GameStateData = JSON.parse(msg.body);
                        setGameState(data); // React จะ re-render กระดานอัตโนมัติ
                    } catch {
                        console.error("Failed to parse game state from WebSocket");
                    }
                });
            },
        });

        // เริ่มเชื่อมต่อ WebSocket
        client.activate();

        // cleanup: ตัดการเชื่อมต่อเมื่อออกจากหน้า BattlePage
        return () => { void client.deactivate(); };
    }, [roomId]);

    // ── Loading State: แสดง spinner ระหว่างรอโหลดข้อมูลครั้งแรก ──
    if (!gameState) {
        return (
            <Center style={{ minHeight: '100vh' }}>
                <Loader color="#FAB005" size="lg" type="dots" />
            </Center>
        );
    }

    // แตก gameState ออกมาใช้งาน
    const { boardRows, boardCols, units, currentTurn, maxTurns, p1Budget, p2Budget } = gameState;

    // ── ฟังก์ชันแปลงตำแหน่ง (col, row) → HexState ──
    // ใช้กำหนดสีของ hex แต่ละช่อง
    const getHexState = (col: number, row: number): HexState => {
        // หา unit ที่มีชีวิตอยู่ในตำแหน่งนี้
        // Unit.isAlive() = hp > 0 แต่ JSON ส่งมาเป็น field ตรงๆ
        const unit = units.find(u => u.hp > 0 && u.row === row && u.col === col);
        if (!unit) return 'NEUTRAL'; // ไม่มี unit = ช่องสีเทา

        // owner 1 = Player1 = ฝั่งแสง (สีทอง)
        // owner 2 = Player2 = ฝั่งมืด (สีม่วง)
        return unit.owner === 1 ? 'LIGHT' : 'DARK';
    };

    // ── ฟังก์ชันเลือกรูปภาพตาม unit ในตำแหน่งนั้น ──
    const getUnitImage = (col: number, row: number): string | undefined => {
        const unit = units.find(u => u.hp > 0 && u.row === row && u.col === col);
        if (!unit) return undefined; // ไม่มี unit = ไม่แสดงรูป
        // P1 ใช้รูป goldenGrass, P2 ใช้รูป archer
        return unit.owner === 1 ? goldenGrass : archer;
    };

    return (
        <Center style={{
            minHeight: '100vh',
            backgroundColor: 'transparent',
            flexDirection: 'column',
            gap: 16
        }}>

            {/* ── HUD แสดงข้อมูลเกม ── */}
            <Box style={{
                display: 'flex',
                gap: 32,
                padding: '8px 24px',
                background: 'rgba(0,0,0,0.6)',
                borderRadius: 12,
                border: '1px solid rgba(255,255,255,0.1)'
            }}>
                {/* Budget ของ Player 1 (สีทอง) */}
                <Text fw={700} style={{ color: '#FAB005' }}>
                    P1: ${p1Budget}
                </Text>

                {/* Turn ปัจจุบัน / Turn สูงสุด */}
                <Text fw={700} style={{ color: 'rgba(255,255,255,0.8)' }}>
                    Turn {currentTurn} / {maxTurns}
                </Text>

                {/* Budget ของ Player 2 (สีม่วง) */}
                <Text fw={700} style={{ color: '#7048E8' }}>
                    P2: ${p2Budget}
                </Text>
            </Box>

            {/* ── กระดาน Hex ── */}
            <Box style={{ display: 'flex', flexDirection: 'row' }}>
                {/* วน cols ตาม boardCols จาก API (ไม่ hardcode) */}
                {Array.from({ length: boardCols }).map((_, c) => (
                    <Box
                        key={`col-${c}`}
                        style={{
                            display: 'flex',
                            flexDirection: 'column',
                            // ทำให้ hex ซ้อนกันแนวนอน
                            marginLeft: c === 0 ? '0px' : '-16px',
                            // สลับ offset คอลัมน์คี่/คู่ เพื่อให้ได้รูปแบบ hex grid
                            marginTop: c % 2 === 0 ? '28px' : '0px'
                        }}
                    >

                        {/* วน rows ตาม boardRows จาก API (ไม่ hardcode) */}
                        {Array.from({ length: boardRows }).map((_, r) => (
                            <Hexagon
                                key={`hex-${c}-${r}`}
                                // ส่ง state เพื่อกำหนดสี hex
                                state={getHexState(c, r)}
                                // ส่งรูป unit ถ้ามี unit อยู่ในช่องนี้
                                image={getUnitImage(c, r)}
                                onClick={() => console.log(`col:${c} row:${r}`)}
                            />
                        ))}
                    </Box>
                ))}
            </Box>
        </Center>
    );
}