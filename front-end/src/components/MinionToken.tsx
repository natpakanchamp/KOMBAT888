// src/components/MinionToken.tsx
// แสดงตัวละคร (Unit) บน Hex — ใช้รูปภาพจริง

import LightSaber from '../assets/Light/Light_Saber.png';
import LightArcher from '../assets/Light/Light_Archer.png';
import LightLancer from '../assets/Light/Light_Lancer.png';
import LightCaster from '../assets/Light/Light_Caster.png';
import LightBerserker from '../assets/Light/Light_Berserker.png';

import  DarkSaber from '../assets/Dark/Dark_Saber.png';
import DarkArcher from '../assets/Dark/Dark_Archer.png';
import DarkLancer from '../assets/Dark/Dark_Lancer.png';
import DarkCaster from '../assets/Dark/Dark_Caster.png';
import DarkBerserker from '../assets/Dark/Dark_Berserker.png';

const LIGHT_IMAGES: Record<number, string> = {
    1: LightSaber,
    2: LightArcher,
    3: LightLancer,
    4: LightCaster,
    5: LightBerserker,
};

const DARK_IMAGES: Record<number, string> = {
    1: DarkSaber,
    2: DarkArcher,
    3: DarkLancer,
    4: DarkCaster,
    5: DarkBerserker,
};

// const DARK_IMAGES: Record<number, string> = { ... };

const LIGHT_MINION_ICONS: Record<number, string> = {
    1: '🗡️',  // Saber (fallback)
    2: '🏹',  // Archer
    3: '🔱',  // Lancer
    4: '🪄',  // Caster
    5: '🪓',  // Berserker
};

const DARK_MINION_ICONS: Record<number, string> = {
    1: '⚔️',  // Dark Saber (fallback)
    2: '🎯',  // Dark Archer
    3: '🔱',  // Dark Lancer (ใช้เหมือนกัน)
    4: '🪄',  // Dark Caster (ใช้เหมือนกัน)
    5: '🪓',  // Dark Berserker (ใช้เหมือนกัน)
};

export interface UnitData {
    id: number;
    type: number;
    owner: number;
    hp: number;
    maxHp: number;
    row: number;
    col: number;
}

interface MinionTokenProps {
    unit: UnitData;
}

export function MinionToken({ unit }: MinionTokenProps) {
    const isLight = unit.owner === 1;
    const image = isLight ? LIGHT_IMAGES[unit.type] : DARK_IMAGES[unit.type]; // Dark ยังไม่มีรูป
    const fallbackIcon = LIGHT_MINION_ICONS[unit.type] ?? DARK_MINION_ICONS[unit.type];
    const hpPct = Math.max(0, Math.min(100, (unit.hp / unit.maxHp) * 100));
    const hpColor = hpPct > 50 ? '#4caf50' : hpPct > 25 ? '#ff9800' : '#f44336';

    return (
        <div style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -60%)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 1,
            pointerEvents: 'none',
            zIndex: 5,
        }}>
            {/* ตัวละคร */}
            {image ? (
                <img
                    src={image}
                    alt={`Unit ${unit.id}`}
                    style={{
                        width: 200,
                        height: 200,
                        objectFit: 'contain',
                        filter: 'none',
                        // เพิ่ม drop-shadow ให้เห็นชัด
                        dropShadow: isLight
                            ? '0 0 4px rgba(250,176,5,0.8)'
                            : '0 0 4px rgba(112,72,232,0.8)',
                    }}
                />
            ) : (
                // Fallback วงกลม emoji สำหรับ Dark (ยังไม่มีรูป)
                <div style={{
                    width: 28,
                    height: 28,
                    borderRadius: '50%',
                    background: 'radial-gradient(circle, rgba(112,72,232,0.9), rgba(60,30,150,0.9))',
                    border: '2px solid #7048E8',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 13,
                    boxShadow: '0 0 6px rgba(112,72,232,0.7)',
                }}>
                    {fallbackIcon}
                </div>
            )}

            {/* HP bar */}
            <div style={{
                width: 30,
                height: 3,
                background: 'rgba(0,0,0,0.5)',
                borderRadius: 2,
                overflow: 'hidden',
                transform: 'translateY(-60px)',
            }}>
                <div style={{
                    width: `${hpPct}%`,
                    height: '100%',
                    background: hpColor,
                    transition: 'width 0.3s ease',
                }} />
            </div>
        </div>
    );
}
