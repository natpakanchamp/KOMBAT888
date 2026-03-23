import { useEffect, useRef } from 'react';
import { Howl } from 'howler';

const bgmFile = '/bgm/Giorno_Theme_normal.mp3';

let sharedHowl: Howl | null = null;

export function getHowl(): Howl {
    if (!sharedHowl) {
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

export function fadeOutBGM(durationMs = 3000) {
    const howl = getHowl();
    const startVol = howl.volume();
    const steps = 30;
    const interval = durationMs / steps;
    const decrement = startVol / steps;
    let current = startVol;

    const timer = setInterval(() => {
        current -= decrement;
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

export const useBGM = (shouldPlay: boolean) => {
    const playingRef = useRef(false);

    useEffect(() => {
        const howl = getHowl();

        if (shouldPlay && !playingRef.current) {
            howl.mute(sessionStorage.getItem("bgmMuted") === "true");
            howl.play();
            playingRef.current = true;
        } else if (!shouldPlay && playingRef.current) {
            howl.stop();
            playingRef.current = false;
        }
    }, [shouldPlay]);
};
