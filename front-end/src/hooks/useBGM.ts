import { useEffect, useRef } from 'react';
import { Howl } from 'howler';

const bgmFile = '/bgm/Giorno_Theme_normal.mp3';

let sharedHowl: Howl | null = null;
let battleHowl: Howl | null = null;

// ตั้งค่า battleHowl จาก BattlePage เพื่อให้ GearMenu mute ได้
export function setBattleHowl(h: Howl | null) { battleHowl = h; }

// คืน BGM instance ที่กำลังใช้งานอยู่ (battle หรือ main)
export function getActiveBGM(): Howl { return battleHowl ?? getHowl(); }

// ฟังก์ชันสำหรับสร้างและแชร์ Howl instance สำหรับ BGM
export function getHowl(): Howl {
    if (!sharedHowl) { // สร้าง Howl instance ครั้งแรกและเก็บไว้ในตัวแปร sharedHowl เพื่อให้ทุกหน้าสามารถใช้ร่วมกันได้
        const isMuted = sessionStorage.getItem("bgmMuted") === "true";
        sharedHowl = new Howl({
            src: [bgmFile],
            loop: true,
            volume: 0.2,
            mute: isMuted,
        });
    }
    return sharedHowl;
}

// ฟังก์ชันสำหรับค่อยๆ ลดเสียงและหยุดเพลง
export function fadeOutBGM(durationMs = 3000) {
    const howl = getHowl(); // ใช้ shared Howl instance
    const startVol = howl.volume(); // เก็บระดับเสียงเริ่มต้นไว้เพื่อคำนวณการลดเสียง
    const steps = 30; // จำนวนขั้นตอนในการลดเสียง
    const interval = durationMs / steps; // เวลาระหว่างแต่ละขั้นตอนการลดเสียง
    const decrement = startVol / steps; // จำนวนเสียงที่จะลดในแต่ละขั้นตอน
    let current = startVol;

    // ใช้ setInterval เพื่อค่อยๆ ลดเสียงลงทีละนิดจนกว่าจะถึง 0 แล้วหยุดเพลง
    const timer = setInterval(() => {
        current -= decrement;
        // ถ้าระดับเสียงลดลงถึง 0 หรือต่ำกว่า ให้หยุดเพลงและเคลียร์ interval
        if (current <= 0) {
            howl.volume(0);
            howl.stop();
            howl.volume(startVol); // reset volume for next play
            clearInterval(timer);
        } else {
            howl.volume(current);
        }
    }, interval);
}

// Hook สำหรับจัดการ BGM ในแต่ละหน้า
// shouldPlay: ตัวบ่งชี้ว่าควรเล่นเพลงนี้หรือไม่ bgmStarted && !isBattle
export const useBGM = (shouldPlay: boolean) => {
    // ใช้ useRef เพื่อเก็บสถานะการเล่นเพลงปัจจุบัน เพื่อป้องกันการเรียก play/stop ซ้ำๆ เมื่อ shouldPlay ไม่เปลี่ยนแปลง
    const playingRef = useRef(false);

    useEffect(() => {
        // howl จะถูกสร้างครั้งเดียวและแชร์กันในทุกหน้า เป็น singleton เพื่อให้การจัดการเสียงง่ายขึ้น
        const howl = getHowl();

        // ควบคุมการเล่นเพลงตาม shouldPlay และสถานะปัจจุบัน
        if (shouldPlay && !playingRef.current) {
            howl.mute(sessionStorage.getItem("bgmMuted") === "true");
            howl.play();
            playingRef.current = true;
            // ถ้าเพลงถูกหยุดด้วย fadeOutBGM แล้ว shouldPlay ยังเป็น true อยู่ เราจะต้องรีสตาร์ทเพลงใหม่
        } else if (!shouldPlay && playingRef.current) {
            howl.stop();
            playingRef.current = false;
        }
    }, [shouldPlay]);
};
