import { Howl } from 'howler'; // นำเข้า Howl จากไลบรารี howler เพื่อใช้ในการจัดการเสียง

const sfxCache: Record<string, Howl> = {};

// ฟังก์ชันสำหรับสร้างและแคช Howl instance สำหรับ SFX แต่ละไฟล์ เพื่อให้ไม่ต้องโหลดซ้ำทุกครั้งที่เล่นเสียงเดียวกัน
function getSFX(src: string, volume = 0.6): Howl {
    if (!sfxCache[src]) { // ถ้าไฟล์เสียงนี้ยังไม่มีในแคช ให้สร้าง Howl instance ใหม่และเก็บไว้ในแคช
        sfxCache[src] = new Howl({ src: [src], volume });
    }
    return sfxCache[src];
}

// ฟังก์ชันสำหรับเล่นเสียง SFX โดยจะเช็คก่อนว่าผู้ใช้ได้ปิดเสียง SFX ไว้หรือไม่ ถ้าไม่ได้ปิดก็จะเล่นเสียงนั้นๆ
export function playSFX(src: string, volume = 0.6) {
    if (sessionStorage.getItem("sfxMuted") === "true") return;
    getSFX(src, volume).play();
}

// เสียงที่ใช้บ่อย export ไว้เลย
export const SFX = {
    YES_YES: '/sfx/yes-yes.mp3',
    WRYYY: '/sfx/Wryyy.mp3',
};
