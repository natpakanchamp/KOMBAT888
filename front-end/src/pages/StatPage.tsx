// src/pages/StatPage.tsx
import { Box, Center, Stack, Text, Group, Paper, Button, Divider, Loader } from '@mantine/core';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import background_LightDark from '../assets/background_LightDark.png';

// ── Types ─────────────────────────────────────────────────────────────────────
interface LocationState {
    roomId: string;
    p1Name: string;
    p2Name: string;
    maxTurns: number;
}

interface PlayerSummary {
    remainingBudget: number;
    remainingMinion: number;
    totalHp: number;
    ownedHexs: number;
    turn: number;
}

interface ApiSummary {
    winner: 'PLAYER1' | 'PLAYER2' | 'DRAW' | 'ONGOING';
    loser: string;
    totalTurn: number;
    player1: PlayerSummary;
    player2: PlayerSummary;
}

// ── Constants ─────────────────────────────────────────────────────────────────
const GOLD   = 'rgba(250, 176, 5, 1)';
const GOLD_D = 'rgba(250, 176, 5, 0.15)';
const PUR    = 'rgba(112, 72, 232, 1)';
const PUR_D  = 'rgba(112, 72, 232, 0.15)';

// ── Stat Row (comparison) ─────────────────────────────────────────────────────
function StatRow({
    label,
    v1,
    v2,
    format = String,
}: {
    label: string;
    v1: number;
    v2: number;
    format?: (n: number) => string;
}) {
    const p1Leads = v1 > v2;
    const p2Leads = v2 > v1;

    return (
        <Group justify="space-between" align="center" style={{ width: '100%' }}>
            <Text
                fw={p1Leads ? 800 : 400}
                style={{
                    width: 120,
                    textAlign: 'right',
                    color: p1Leads ? GOLD : 'rgba(255,255,255,0.55)',
                    fontSize: p1Leads ? 22 : 18,
                }}
            >
                {format(v1)}
            </Text>

            <Text
                style={{
                    flex: 1,
                    textAlign: 'center',
                    color: 'rgba(255,255,255,0.4)',
                    textTransform: 'uppercase',
                    letterSpacing: 2,
                    fontSize: 11,
                }}
            >
                {label}
            </Text>

            <Text
                fw={p2Leads ? 800 : 400}
                style={{
                    width: 120,
                    textAlign: 'left',
                    color: p2Leads ? PUR : 'rgba(255,255,255,0.55)',
                    fontSize: p2Leads ? 22 : 18,
                }}
            >
                {format(v2)}
            </Text>
        </Group>
    );
}

// ── Stat Item (single card) ───────────────────────────────────────────────────
function StatItem({
    icon,
    label,
    value,
    color,
    highlight,
}: {
    icon: string;
    label: string;
    value: number | string;
    color: string;
    highlight: boolean;
}) {
    return (
        <Group justify="space-between" align="center">
            <Text style={{ fontSize: 13, color: 'rgba(255,255,255,0.3)', letterSpacing: 1 }}>
                {icon}&nbsp;&nbsp;{label}
            </Text>
            <Text
                style={{
                    fontSize: highlight ? 20 : 16,
                    color: highlight ? color : 'rgba(255,255,255,0.4)',
                    textShadow: highlight ? `0 0 12px ${color}` : 'none',
                }}
            >
                {value}
            </Text>
        </Group>
    );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function StatPage() {
    const location = useLocation();
    const navigate = useNavigate();

    const locState = location.state as LocationState | null;
    const roomId  = locState?.roomId  ?? null;
    const p1Name  = locState?.p1Name  ?? 'Player 1';
    const p2Name  = locState?.p2Name  ?? 'Player 2';
    const maxTurns = locState?.maxTurns ?? 0;

    const [summary, setSummary] = useState<ApiSummary | null>(null);
    const [loading, setLoading] = useState(!!roomId);
    const [error,   setError]   = useState<string | null>(null);

    useEffect(() => {
        if (!roomId) return;
        fetch(`/api/game/${roomId}/summary`)
            .then(r => {
                if (!r.ok) throw new Error(`HTTP ${r.status}`);
                return r.json() as Promise<ApiSummary>;
            })
            .then(data => { setSummary(data); setLoading(false); })
            .catch(e  => { setError(e.message); setLoading(false); });
    }, [roomId]);

    // Demo fallback เมื่อไม่มี roomId (เช่น เปิดหน้าตรงๆ)
    const data: ApiSummary = summary ?? {
        winner: 'PLAYER1',
        loser:  'PLAYER2',
        totalTurn: 20,
        player1: { remainingBudget: 2250, remainingMinion: 2, totalHp: 420, ownedHexs: 38, turn: 20 },
        player2: { remainingBudget: 750,  remainingMinion: 1, totalHp: 180, ownedHexs: 26, turn: 20 },
    };

    const isDraw   = data.winner === 'DRAW';
    const winner   = isDraw ? 0 : data.winner === 'PLAYER1' ? 1 : 2;
    const winnerName  = isDraw ? 'DRAW' : winner === 1 ? p1Name : p2Name;
    const winnerColor = isDraw ? 'rgba(255,255,255,0.85)' : winner === 1 ? GOLD : PUR;

    const p1 = data.player1;
    const p2 = data.player2;

    return (
        <Box
            style={{
                minHeight: '100vh',
                width: '100%',
                position: 'relative',
                backgroundImage: `url(${background_LightDark})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                backgroundAttachment: 'fixed',
                overflow: 'hidden',
            }}
        >
            {/* Dark overlay */}
            <Box
                style={{
                    position: 'fixed',
                    inset: 0,
                    background: 'rgba(0,0,0,0.72)',
                    backdropFilter: 'blur(10px)',
                    zIndex: 1,
                }}
            />

            {/* Faction ambient glow */}
            {!isDraw && (
                <Box
                    style={{
                        position: 'fixed',
                        inset: 0,
                        background: winner === 1
                            ? 'radial-gradient(ellipse 60% 40% at 30% 50%, rgba(250,176,5,0.08) 0%, transparent 70%)'
                            : 'radial-gradient(ellipse 60% 40% at 70% 50%, rgba(112,72,232,0.1) 0%, transparent 70%)',
                        zIndex: 2,
                        pointerEvents: 'none',
                    }}
                />
            )}

            {/* Content */}
            <Center style={{ minHeight: '100vh', position: 'relative', zIndex: 3, padding: '40px 20px' }}>

                {/* Loading */}
                {loading && (
                    <Stack align="center" gap={12}>
                        <Loader color="gray" size="lg" />
                        <Text style={{ color: 'rgba(255,255,255,0.4)', letterSpacing: 2, fontSize: 13 }}>
                            Loading results...
                        </Text>
                    </Stack>
                )}

                {/* Error */}
                {!loading && error && (
                    <Stack align="center" gap={16}>
                        <Text style={{ color: 'rgba(255,80,80,0.85)', fontSize: 16 }}>
                            Failed to load summary: {error}
                        </Text>
                        <Button onClick={() => navigate('/')} variant="subtle" color="gray">
                            Back to Home
                        </Button>
                    </Stack>
                )}

                {/* Main content */}
                {!loading && !error && (
                    <Stack align="center" gap={32} style={{ width: '100%', maxWidth: 740 }}>

                        {/* ── Game Over Banner ── */}
                        <Stack align="center" gap={4}>
                            <Text
                                style={{
                                    fontSize: 14,
                                    letterSpacing: 8,
                                    color: 'rgba(255,255,255,0.35)',
                                    textTransform: 'uppercase',
                                }}
                            >
                                ⚔ &nbsp; GAME OVER &nbsp; ⚔
                            </Text>

                            <Text
                                style={{
                                    fontSize: isDraw ? 52 : 62,
                                    fontWeight: 900,
                                    color: winnerColor,
                                    letterSpacing: 4,
                                    lineHeight: 1,
                                    textShadow: isDraw
                                        ? '0 0 30px rgba(255,255,255,0.3)'
                                        : winner === 1
                                        ? '0 0 30px rgba(250,176,5,0.5)'
                                        : '0 0 30px rgba(112,72,232,0.6)',
                                }}
                            >
                                {isDraw ? 'DRAW' : winnerName.toUpperCase()}
                            </Text>

                            <Text
                                style={{
                                    fontSize: 16,
                                    letterSpacing: 6,
                                    color: isDraw ? 'rgba(255,255,255,0.45)' : winnerColor,
                                    opacity: 0.75,
                                }}
                            >
                                {isDraw ? 'No Victor This Day' : 'VICTORY'}
                            </Text>
                        </Stack>

                        {/* ── Player Cards ── */}
                        <Group align="stretch" gap={0} style={{ width: '100%' }}>

                            {/* Player 1 — LIGHT */}
                            <Paper
                                style={{
                                    flex: 1,
                                    background: winner === 1
                                        ? `linear-gradient(135deg, rgba(30,25,0,0.9) 0%, ${GOLD_D} 100%)`
                                        : 'rgba(15,14,20,0.85)',
                                    border: `1.5px solid ${winner === 1 ? 'rgba(250,176,5,0.5)' : 'rgba(255,255,255,0.08)'}`,
                                    borderRadius: '16px 0 0 16px',
                                    padding: '28px 24px',
                                    position: 'relative',
                                    overflow: 'hidden',
                                }}
                            >
                                {winner === 1 && (
                                    <Text style={{ position: 'absolute', top: 10, right: 16, fontSize: 28 }}>👑</Text>
                                )}
                                <Stack align="center" gap={16}>
                                    <Box
                                        style={{
                                            background: GOLD_D,
                                            border: `1px solid rgba(250,176,5,0.3)`,
                                            borderRadius: 20,
                                            padding: '3px 14px',
                                        }}
                                    >
                                        <Text style={{ color: GOLD, fontSize: 11, letterSpacing: 3 }}>LIGHT</Text>
                                    </Box>

                                    <Text
                                        style={{
                                            fontSize: winner === 1 ? 28 : 22,
                                            color: winner === 1 ? GOLD : 'rgba(255,255,255,0.6)',
                                            letterSpacing: 2,
                                            textAlign: 'center',
                                            lineHeight: 1.2,
                                            textShadow: winner === 1 ? `0 0 20px rgba(250,176,5,0.5)` : 'none',
                                        }}
                                    >
                                        {p1Name}
                                    </Text>

                                    <Divider style={{ width: '100%', borderColor: 'rgba(255,255,255,0.08)' }} />

                                    <Stack gap={10} style={{ width: '100%' }}>
                                        <StatItem label="Hexes"    value={p1.ownedHexs}                      color={GOLD} highlight={p1.ownedHexs >= p2.ownedHexs} />
                                        <StatItem label="Budget"   value={p1.remainingBudget.toLocaleString()} color={GOLD} highlight={p1.remainingBudget >= p2.remainingBudget} />
                                        <StatItem label="Minions"  value={p1.remainingMinion}                 color={GOLD} highlight={p1.remainingMinion >= p2.remainingMinion} />
                                        <StatItem label="Total HP" value={p1.totalHp}                         color={GOLD} highlight={p1.totalHp >= p2.totalHp} />
                                    </Stack>
                                </Stack>
                            </Paper>

                            {/* VS Divider */}
                            <Box
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: 56,
                                    background: 'rgba(10,10,15,0.95)',
                                    border: '1.5px solid rgba(255,255,255,0.06)',
                                    borderLeft: 'none',
                                    borderRight: 'none',
                                    flexShrink: 0,
                                }}
                            >
                                <Text
                                    style={{
                                        fontSize: 18,
                                        color: 'rgba(255,255,255,0.2)',
                                        letterSpacing: 2,
                                        writingMode: 'vertical-rl',
                                        textOrientation: 'mixed',
                                    }}
                                >
                                    VS
                                </Text>
                            </Box>

                            {/* Player 2 — DARK */}
                            <Paper
                                style={{
                                    flex: 1,
                                    background: winner === 2
                                        ? `linear-gradient(135deg, rgba(15,10,30,0.9) 0%, ${PUR_D} 100%)`
                                        : 'rgba(15,14,20,0.85)',
                                    border: `1.5px solid ${winner === 2 ? 'rgba(112,72,232,0.5)' : 'rgba(255,255,255,0.08)'}`,
                                    borderRadius: '0 16px 16px 0',
                                    padding: '28px 24px',
                                    position: 'relative',
                                    overflow: 'hidden',
                                }}
                            >
                                {winner === 2 && (
                                    <Text style={{ position: 'absolute', top: 10, left: 16, fontSize: 28 }}>👑</Text>
                                )}
                                <Stack align="center" gap={16}>
                                    <Box
                                        style={{
                                            background: PUR_D,
                                            border: `1px solid rgba(112,72,232,0.3)`,
                                            borderRadius: 20,
                                            padding: '3px 14px',
                                        }}
                                    >
                                        <Text style={{ color: PUR, fontSize: 11, letterSpacing: 3 }}>DARK</Text>
                                    </Box>

                                    <Text
                                        style={{
                                            fontSize: winner === 2 ? 28 : 22,
                                            color: winner === 2 ? PUR : 'rgba(255,255,255,0.6)',
                                            letterSpacing: 2,
                                            textAlign: 'center',
                                            lineHeight: 1.2,
                                            textShadow: winner === 2 ? `0 0 20px rgba(112,72,232,0.6)` : 'none',
                                        }}
                                    >
                                        {p2Name}
                                    </Text>

                                    <Divider style={{ width: '100%', borderColor: 'rgba(255,255,255,0.08)' }} />

                                    <Stack gap={10} style={{ width: '100%' }}>
                                        <StatItem label="Hexes"    value={p2.ownedHexs}                      color={PUR} highlight={p2.ownedHexs >= p1.ownedHexs} />
                                        <StatItem label="Budget"   value={p2.remainingBudget.toLocaleString()} color={PUR} highlight={p2.remainingBudget >= p1.remainingBudget} />
                                        <StatItem label="Minions"  value={p2.remainingMinion}                 color={PUR} highlight={p2.remainingMinion >= p1.remainingMinion} />
                                        <StatItem label="Total HP" value={p2.totalHp}                         color={PUR} highlight={p2.totalHp >= p1.totalHp} />
                                    </Stack>
                                </Stack>
                            </Paper>
                        </Group>

                        {/* ── Stat Comparison Row ── */}
                        <Paper
                            style={{
                                width: '100%',
                                background: 'rgba(12,11,18,0.88)',
                                border: '1px solid rgba(255,255,255,0.07)',
                                borderRadius: 14,
                                padding: '20px 28px',
                            }}
                        >
                            <Stack gap={14}>
                                <StatRow label="Hexes Captured"   v1={p1.ownedHexs}         v2={p2.ownedHexs} />
                                <Divider style={{ borderColor: 'rgba(255,255,255,0.06)' }} />
                                <StatRow label="Budget Remaining" v1={p1.remainingBudget}    v2={p2.remainingBudget} format={n => n.toLocaleString()} />
                                <Divider style={{ borderColor: 'rgba(255,255,255,0.06)' }} />
                                <StatRow label="Minions Alive"    v1={p1.remainingMinion}    v2={p2.remainingMinion} />
                                <Divider style={{ borderColor: 'rgba(255,255,255,0.06)' }} />
                                <StatRow label="Total HP"         v1={p1.totalHp}            v2={p2.totalHp} />
                                <Divider style={{ borderColor: 'rgba(255,255,255,0.06)' }} />
                                <Group justify="center" gap={8}>
                                    <Text style={{ color: 'rgba(255,255,255,0.25)', fontSize: 11, letterSpacing: 2, textTransform: 'uppercase' }}>
                                        Turns Played
                                    </Text>
                                    <Text style={{ color: 'rgba(255,255,255,0.55)', fontSize: 16 }}>
                                        {data.totalTurn}{maxTurns > 0 ? ` / ${maxTurns}` : ''}
                                    </Text>
                                </Group>
                            </Stack>
                        </Paper>

                        {/* ── Action Buttons ── */}
                        <Group justify="center" gap={16}>
                            <Button
                                size="lg"
                                onClick={() => navigate('/')}
                                style={{
                                    background: 'rgba(255,255,255,0.06)',
                                    border: '1px solid rgba(255,255,255,0.15)',
                                    color: 'rgba(255,255,255,0.75)',
                                    letterSpacing: 2,
                                    fontSize: 14,
                                    padding: '12px 32px',
                                    borderRadius: 10,
                                    transition: 'background 0.2s',
                                }}
                                onMouseEnter={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.12)'; }}
                                onMouseLeave={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.06)'; }}
                            >
                                Main Menu
                            </Button>

                            <Button
                                size="lg"
                                onClick={() => navigate('/login')}
                                style={{
                                    background: isDraw
                                        ? 'linear-gradient(135deg, rgba(80,70,140,0.9), rgba(40,35,80,0.95))'
                                        : winner === 1
                                        ? 'linear-gradient(135deg, rgba(180,120,0,0.9), rgba(100,65,0,0.95))'
                                        : 'linear-gradient(135deg, rgba(80,50,180,0.9), rgba(50,30,130,0.95))',
                                    border: `1px solid ${isDraw ? 'rgba(150,130,255,0.3)' : winner === 1 ? 'rgba(250,176,5,0.4)' : 'rgba(112,72,232,0.4)'}`,
                                    color: isDraw ? 'rgba(255,255,255,0.9)' : winner === 1 ? GOLD : 'rgba(180,160,255,0.95)',
                                    letterSpacing: 2,
                                    fontSize: 14,
                                    padding: '12px 32px',
                                    borderRadius: 10,
                                }}
                            >
                                ⚔&nbsp;&nbsp;Play Again
                            </Button>
                        </Group>

                    </Stack>
                )}
            </Center>
        </Box>
    );
}
