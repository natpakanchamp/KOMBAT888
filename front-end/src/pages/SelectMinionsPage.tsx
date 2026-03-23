// src/pages/SelectionPage.tsx
import { useNavigate, useLocation } from 'react-router-dom';
import { Center, Group, Title, Stack, Box, UnstyledButton, Image, Text, Button } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { useState } from 'react';
import { UnitCard } from '../components/UnitCard';
import { SetupModal } from '../components/SetupModal';

// Import Assets
import goBackBtn from "../assets/goBack.png";
import saber from "../assets/saber.png";
import archer from "../assets/archer.png";
import lancer from "../assets/lancer.png";
import caster from "../assets/caster.png";
import berserker from "../assets/berserker.png";

// Import Card Backs / Icons
import sword from "../assets/sword.png";
import bow from "../assets/bow.png";
import lancer_card from "../assets/lancer_card.png";
import berserker_card from "../assets/berserker_card.png";
import caster_card from "../assets/caster_card.png";

type SelectedMinion = { type: string; strategy: string };

export default function SelectMinionsPage() {
    const navigate = useNavigate();
    const location = useLocation();

    // Check if coming from a waiting room
    const roomId = (location.state as any)?.roomId as string | undefined;
    const fromRoom = (location.state as any)?.fromRoom === true;

    // Load existing selections from localStorage if returning
    const storageKey = roomId ? `minions_${roomId}` : null;
    const [selectedMinions, setSelectedMinions] = useState<SelectedMinion[]>(() => {
        if (!storageKey) return [];
        try {
            const saved = localStorage.getItem(storageKey);
            return saved ? JSON.parse(saved) : [];
        } catch { return []; }
    });

    // Modal logic
    const [opened, { open, close }] = useDisclosure(false);
    const [selectedUnitType, setSelectedUnitType] = useState('');

    const handleSelectUnit = (type: string) => {
        setSelectedUnitType(type);
        open();
    };

    const handleSaveStrategy = (strategy: string) => {
        const newMinion: SelectedMinion = { type: selectedUnitType, strategy };

        setSelectedMinions(prev => {
            // Replace if same type already selected, otherwise add
            const filtered = prev.filter(m => m.type !== selectedUnitType);
            const updated = [...filtered, newMinion];

            // Save to localStorage if in room context
            if (storageKey) {
                localStorage.setItem(storageKey, JSON.stringify(updated));
            }

            return updated;
        });
    };

    const handleBackToRoom = () => {
        if (roomId) {
            // Save and go back to waiting room
            if (storageKey) {
                localStorage.setItem(storageKey, JSON.stringify(selectedMinions));
            }
            navigate(`/waitingRoom/${roomId}`, { state: { created: true, user: localStorage.getItem("username") ?? "" } });
        } else {
            navigate('/');
        }
    };

    const isSelected = (type: string) => selectedMinions.some(m => m.type === type);
    const hasStrategy = (type: string) => selectedMinions.some(m => m.type === type && m.strategy !== "");

    const handleFlip = (type: string, flipped: boolean) => {
        if (flipped) {
            setSelectedMinions(prev => {
                if (prev.some(m => m.type === type)) return prev;
                const updated = [...prev, { type, strategy: "" }];
                if (storageKey) localStorage.setItem(storageKey, JSON.stringify(updated));
                return updated;
            });
        } else {
            setSelectedMinions(prev => {
                const updated = prev.filter(m => m.type !== type);
                if (storageKey) localStorage.setItem(storageKey, JSON.stringify(updated));
                return updated;
            });
        }
    };

    return (
        <Box style={{ minHeight: '100vh', position: 'relative', overflow: 'hidden' }}>

            {/* Modal สำหรับกรอก Strategy */}
            <SetupModal
                opened={opened}
                onClose={close}
                unitType={selectedUnitType}
                onSave={handleSaveStrategy}
            />

            <Center style={{ paddingTop: '12vh' }}>
                <Stack align="center" gap={40}>
                    <Title
                        c="white"
                        order={1}
                        style={{
                            textShadow: '0 0 15px rgba(255,255,255,0.3)',
                            fontSize: '3rem',
                            letterSpacing: '2px'
                        }}
                    >
                        CHOOSE YOUR UNIT
                    </Title>

                    {/* Selected minions summary (only in room context) */}
                    {fromRoom && (
                        <Box
                            style={{
                                padding: "10px 24px",
                                borderRadius: 10,
                                background: selectedMinions.length > 0 ? "rgba(250,176,5,0.8)" : "rgba(255,255,255,0.55)",
                                border: selectedMinions.length > 0 ? "1px solid rgba(250,176,5,0.2)" : "1px solid rgba(255,255,255,0.08)",
                                textAlign: "center",
                            }}
                        >
                            <Text size="xs" style={{ color: "rgba(0,0,0,0.5)", letterSpacing: 1, textTransform: "uppercase" }}>
                                Selected: {selectedMinions.length > 0 ? selectedMinions.map(m => m.type).join(", ") : "None"}
                            </Text>
                        </Box>
                    )}

                    {/* Card group */}
                    <Group
                        justify="center"
                        align="flex-end"
                        gap="md"
                        wrap="nowrap"
                        style={{
                            width: '100%',
                            maxWidth: 1200,
                            margin: '0 auto',
                            padding: '0 20px',
                        }}
                    >
                        {[
                            { type: "Saber", desc: "Melee class with balanced stats.", charImg: saber, backImg: sword },
                            { type: "Archer", desc: "Ranged specialist with high critical.", charImg: archer, backImg: bow },
                            { type: "Lancer", desc: "Agile warrior with long-range pokes.", charImg: lancer, backImg: lancer_card },
                            { type: "Caster", desc: "Magical unit with powerful area spells.", charImg: caster, backImg: caster_card },
                            { type: "Berserker", desc: "High damage dealer with low defense.", charImg: berserker, backImg: berserker_card },
                        ].map(unit => (
                            <UnitCard
                                key={unit.type}
                                strategy={unit.type}
                                description={unit.desc}
                                charImg={unit.charImg}
                                backImg={unit.backImg}
                                onSelect={() => handleSelectUnit(unit.type)}
                                onFlip={(flipped) => handleFlip(unit.type, flipped)}
                                isSelected={isSelected(unit.type)}
                                hasStrategy={hasStrategy(unit.type)}
                                initialFlipped={isSelected(unit.type)}
                            />
                        ))}
                    </Group>
                </Stack>
            </Center>

            {/* Bottom left: GO BACK button */}
            <Box
                style={{
                    position: 'fixed',
                    bottom: '30px',
                    left: '30px',
                    zIndex: 100,
                }}
            >
                {fromRoom ? (
                    <Button
                        size="lg"
                        radius="md"
                        onClick={handleBackToRoom}
                        styles={{
                            root: {
                                letterSpacing: 2,
                                textTransform: "uppercase" as const,
                                fontWeight: 700,
                                background: selectedMinions.length > 0
                                    ? "linear-gradient(180deg, rgba(210,145,80,1) 0%, rgba(120,70,35,1) 100%)"
                                    : "linear-gradient(180deg, rgba(100,100,100,0.8), rgba(60,60,60,0.8))",
                                border: "1px solid rgba(255,215,170,0.18)",
                                boxShadow: "0 10px 30px rgba(0,0,0,0.5)",
                            },
                        }}
                    >
                        BACK TO ROOM {selectedMinions.length > 0 ? `(${selectedMinions.length})` : ""}
                    </Button>
                ) : (
                    <UnstyledButton onClick={() => navigate('/')}>
                        <Image
                            src={goBackBtn}
                            w={180}
                            style={{ transition: "transform 0.2s" }}
                            onMouseEnter={(e) => (e.currentTarget.style.transform = "scale(1.1)")}
                            onMouseLeave={(e) => (e.currentTarget.style.transform = "scale(1)")}
                        />
                    </UnstyledButton>
                )}
            </Box>
        </Box>
    );
}
