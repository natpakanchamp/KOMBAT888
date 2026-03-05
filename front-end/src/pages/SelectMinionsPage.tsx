// src/pages/SelectionPage.tsx
import { useNavigate } from 'react-router-dom';
import { Center, Group, Title, Stack, Box, UnstyledButton, Image } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { useState } from 'react';
import { UnitCard } from '../components/UnitCard';
import { SetupModal } from '../components/SetupModal'; // อย่าลืมสร้างไฟล์นี้ตามโค้ดก่อนหน้านะครับ

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

export default function SelectMinionsPage() {
    const navigate = useNavigate();

    // Logic สำหรับ Modal
    const [opened, { open, close }] = useDisclosure(false);
    const [selectedUnitType, setSelectedUnitType] = useState(''); // เก็บชื่อตัวละครที่ถูกคลิก

    // ฟังก์ชันเมื่อกดปุ่ม "เลือกใช้งาน" บนการ์ด
    const handleSelectUnit = (type: string) => {
        setSelectedUnitType(type); // Fix ชื่อตัวละครไว้ใน State
        open(); // เปิด Modal ขึ้นมา
    };

    const handleSaveStrategy = (strategy: string) => {
        console.log(`Unit: ${selectedUnitType}, Strategy: ${strategy}`);
        // คุณสามารถนำข้อมูลนี้ไปเก็บใน Database หรือ State รวมของทีมได้ที่นี่
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

            <Center style={{ paddingTop: '15vh' }}>
                <Stack align="center" gap={60}>
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

                    {/* กลุ่มการ์ดที่เรียงเป็นแถวเดียวไม่ตกบรรทัด */}
                    <Group
                        justify="center"
                        align="flex-end"
                        gap="md"
                        wrap="nowrap" // บังคับให้อยู่แถวเดียวกันเสมอ
                        style={{
                            width: '100%',
                            maxWidth: 1200,
                            margin: '0 auto',
                            padding: '0 20px',
                        }}
                    >
                        <UnitCard
                            strategy="Saber"
                            description="Melee class with balanced stats."
                            charImg={saber}
                            backImg={sword}
                            onSelect={() => handleSelectUnit("Saber")}
                        />
                        <UnitCard
                            strategy="Archer"
                            description="Ranged specialist with high critical."
                            charImg={archer}
                            backImg={bow}
                            onSelect={() => handleSelectUnit("Archer")}
                        />
                        <UnitCard
                            strategy="Lancer"
                            description="Agile warrior with long-range pokes."
                            charImg={lancer}
                            backImg={lancer_card}
                            onSelect={() => handleSelectUnit("Lancer")}
                        />
                        <UnitCard
                            strategy="Caster"
                            description="Magical unit with powerful area spells."
                            charImg={caster}
                            backImg={caster_card}
                            onSelect={() => handleSelectUnit("Caster")}
                        />
                        <UnitCard
                            strategy="Berserker"
                            description="High damage dealer with low defense."
                            charImg={berserker}
                            backImg={berserker_card}
                            onSelect={() => handleSelectUnit("Berserker")}
                        />
                    </Group>
                </Stack>
            </Center>

            {/* ปุ่ม GO BACK มุมซ้ายล่าง */}
            <Box
                style={{
                    position: 'fixed',
                    bottom: '30px',
                    left: '30px',
                    zIndex: 100,
                }}
            >
                <UnstyledButton onClick={() => navigate('/')}>
                    <Image
                        src={goBackBtn}
                        w={180} // ปรับขนาดให้พอดีกับหน้าจอ
                        style={{ transition: "transform 0.2s" }}
                        onMouseEnter={(e) => (e.currentTarget.style.transform = "scale(1.1)")}
                        onMouseLeave={(e) => (e.currentTarget.style.transform = "scale(1)")}
                    />
                </UnstyledButton>
            </Box>
        </Box>
    );
}