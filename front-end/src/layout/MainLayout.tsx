import {Outlet, useLocation} from "react-router-dom";
import { BackgroundImage, Box, Container} from "@mantine/core";
import myBackground from "../assets/background_LightDark.png";
import GearMenu from "../components/GearMenu";

export default function MainLayout() {
    const location = useLocation();
    const isHome = location.pathname === "/";

    return(
        <Box>
            <BackgroundImage
                src={myBackground}
                style={{
                    minHeight: "100vh",
                    width: "100%",
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                    backgroundAttachment: "fixed"
                }}
            >
                {!isHome && <GearMenu />}
                <Container size="lg" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', justifyContent: 'center' }}>
                    <Outlet />
                </Container>
            </BackgroundImage>
        </Box>
    );
}