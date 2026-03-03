// src/pages/SelectionPage.tsx
import { useNavigate } from 'react-router-dom'
import {Center, Group, Title, Stack, Box, UnstyledButton, Image} from '@mantine/core';
import { UnitCard } from '../components/UnitCard';
import cardBack from '../assets/black.jfif';
import goBackBtn from "../assets/goBack.png";
import saber from "../assets/saber.png"
import archer from "../assets/archer.png"
import lancer from "../assets/lancer.png"
import caster from "../assets/caster.png"
import berserker from "../assets/berserker.png"
import sword from "../assets/sword.png"
import bow from "../assets/bow.png"

export default function SelectMinionsPage() {

    const navigate = useNavigate();

    return (
        <Center style={{ paddingTop: '5vh' }}>
            <Stack align="center" gap={50} style={{ marginTop: '-200px' }}>
                <Title c="white" order={1} style={{ textShadow: '0 0 10px rgba(255,255,255,0.5)' }}>
                    CHOOSE YOUR UNIT
                </Title>

                <Group
                    justify="center"  // จัดให้อยู่ตรงกลางแนวนอน
                    align="flex-end" // ให้ฐานการ์ดเท่ากัน (กรณีขยับการ์ดขึ้นตอน Hover)
                    gap="xl"         // ระยะห่างระหว่างการ์ดแต่ละใบ
                    wrap="nowrap"    // 👈 บังคับห้ามตกบรรทัดเด็ดขาด!
                    style={{
                        width: '100%',
                        padding: '0 40px'
                    }}
                >
                    {/* เรียกใช้ UnitCard และส่ง Props ต่างๆ เข้าไป */}
                    <UnitCard
                        name="Saber"
                        description=""
                        charImg={saber}
                        backImg={sword}
                    />
                    <UnitCard
                        name="Archer"
                        description=""
                        charImg={archer}
                        backImg={bow}
                    />
                    <UnitCard
                        name="Lancer"
                        description=""
                        charImg={lancer}
                        backImg={cardBack}
                    />
                    <UnitCard
                        name="Caster"
                        description=""
                        charImg={caster}
                        backImg={cardBack}
                    />
                    <UnitCard
                        name="Berserker"
                        description=""
                        charImg={berserker}
                        backImg={cardBack}
                    />
                </Group>
            </Stack>

            <Box
                style={{
                    position: 'fixed',    // 👈 ทำให้ปุ่มลอยคงที่
                    bottom: '20px',       // ห่างจากขอบล่าง 20px
                    left: '0px',         // ห่างจากขอบซ้าย 20px
                    zIndex: 100,          // อยู่เหนือชั้นอื่นๆ
                }}
            >
                <UnstyledButton
                    onClick={() => navigate('/')}
                >
                    <Image
                        src={goBackBtn}
                        w={400}
                        style={{ transition: "transform 0.2s" }}
                        onMouseEnter={(e) =>
                            (e.currentTarget.style.transform = "scale(1.05)")}
                        onMouseLeave={(e) =>
                            (e.currentTarget.style.transform = "scale(1)")}/>
                </UnstyledButton>
            </Box>
        </Center>
    );
}