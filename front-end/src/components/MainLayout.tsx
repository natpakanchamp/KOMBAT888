// src/layout/MainLayout.tsx
import { Outlet, useLocation } from "react-router-dom"; // หรือ "react-router"
import { useBGM } from "../hooks/useBGM";

export default function MainLayout() {
    // ดึง pathname ปัจจุบันออกมา
    const location = useLocation();
    const { pathname } = location;

    // เช็คว่าปัจจุบันอยู่ในหน้าต่อสู้ (Battle) หรือไม่
    // เราใช้ .startsWith เพราะหน้า Battle จะมี roomId ต่อท้าย เช่น /battle/room123
    const isBattlePage = pathname.startsWith("/battle");

    // จัดดการระบบเพลง
    // เพลงหน้าเมนู: จะเล่นก็ต่อเมื่อ "ไม่ใช่" หน้าต่อสู้
    useBGM("/bgm/battle-theme.mp3", {
        loop: true,
        volume: 0.5,
        pause: isBattlePage, // ถ้าเป็นหน้าต่อสู้ ให้หยุดเพลงนี้
    });

    // เพลงหน้าต่อสู้: จะเล่นก็ต่อเมื่อ "เป็น" หน้าต่อสู้
    useBGM("/bgm/battle-page", {
        loop: true,
        volume: 0.6,
        pause: !isBattlePage, // ถ้าไม่ใช่หน้าต่อสู้ ให้หยุดเพลงนี้
    });

    return (
        <div className="game-container">
            <Outlet />
        </div>
    );
}