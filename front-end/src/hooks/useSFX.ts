import { Howl } from 'howler';

const sfxCache: Record<string, Howl> = {};

function getSFX(src: string, volume = 0.6): Howl {
    if (!sfxCache[src]) {
        sfxCache[src] = new Howl({ src: [src], volume });
    }
    return sfxCache[src];
}

export function playSFX(src: string, volume = 0.6) {
    if (sessionStorage.getItem("sfxMuted") === "true") return;
    getSFX(src, volume).play();
}

// เสียงที่ใช้บ่อย export ไว้เลย
export const SFX = {
    YES_YES: '/sfx/yes-yes.mp3',
    WRYYY: '/sfx/Wryyy.mp3',
};
