import {BackgroundImage} from "@mantine/core";
import errorBackground from "../assets/error404.png";

export default function ErrorPage() {
    return (
        <BackgroundImage
            src={errorBackground}
            style={{
                minHeight: "100vh",
                width: "100%",
                backgroundSize: "cover",
                backgroundPosition: "center",
                backgroundAttachment: "fixed"
            }}
        >
        </BackgroundImage>
    );
}