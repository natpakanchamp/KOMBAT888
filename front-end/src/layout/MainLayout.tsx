import {Outlet} from "react-router-dom";
import { BackgroundImage, Box, Container} from "@mantine/core";
import myBackground from "../assets/background_LightDark.png";

export default function MainLayout() {
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
                <Container size="lg" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', justifyContent: 'center' }}>
                    <Outlet />
                </Container>
            </BackgroundImage>
        </Box>
    );
}