import { useEffect, useRef } from 'react';
import { Howl, Howler } from 'howler';
import bgmFile from '../../public/bgm/battle-theme.mp3';

let sharedHowl: Howl | null = null;

export function getHowl(): Howl {
    if (!sharedHowl) {
        const isMuted = sessionStorage.getItem("bgmMuted") === "true";
        sharedHowl = new Howl({
            src: [bgmFile],
            loop: true,
            volume: 0.4,
            html5: true,
            mute: isMuted,
        });
    }
    return sharedHowl;
}

export const useBGM = (shouldPlay: boolean) => {
    const playingRef = useRef(false);

    useEffect(() => {
        const howl = getHowl();

        if (shouldPlay && !playingRef.current) {
            // apply mute state จาก sessionStorage ก่อนเล่น
            Howler.mute(sessionStorage.getItem("bgmMuted") === "true");
            howl.play();
            playingRef.current = true;
        } else if (!shouldPlay && playingRef.current) {
            howl.stop();
            playingRef.current = false;
        }
    }, [shouldPlay]);
};
