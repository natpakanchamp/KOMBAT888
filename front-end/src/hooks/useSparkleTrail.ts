import { useEffect } from 'react';

export const useSparkleTrail = () => {
    useEffect(() => {
        // ชุดรูปภาพจาก Bright Blue Star ที่เจ้านายส่งมา
        const sparkleImages = [
            "https://cursor-trails.custom-cursor.com/uploads/light_symmetry_triangle_pattern_decorative_light_effect1_28fe8b0ee6.png",
            "https://cursor-trails.custom-cursor.com/uploads/light_symmetry_triangle_pattern_decorative_light_effect_2_1ae87a689b.png",
            "https://cursor-trails.custom-cursor.com/uploads/light_symmetry_triangle_pattern_decorative_light_effect_3_5db0effead.png"
        ];

        let lastTime = 0;

        const createSparkle = (x: number, y: number) => {
            const sparkle = document.createElement("img");

            // สุ่มเลือกรูปจาก 3 แบบ
            const randomImg = sparkleImages[Math.floor(Math.random() * sparkleImages.length)];
            sparkle.src = randomImg;

            const size = Math.random() * 30 + 30; // ขนาดประมาณ 15-30px กำลังสวยครับ

            Object.assign(sparkle.style, {
                position: 'fixed',
                left: `${x}px`,
                top: `${y}px`,
                width: `${size}px`,
                height: 'auto',
                pointerEvents: 'none',
                zIndex: '100000',
                transform: 'translate(-50%, -50%)',
                // ปรับค่าความสว่างให้ดู "Bright Blue" ยิ่งขึ้น
                filter: 'contrast(1.2) brightness(1.2) drop-shadow(0 0 5px rgba(0, 162, 255, 0.5))',
                opacity: '1'
            });

            document.body.appendChild(sparkle);

            // --- เปลี่ยนการเคลื่อนที่จาก "ฟู่กระจาย" เป็น "ร่วงและกะพริบ" ---
            const driftX = (Math.random() - 0.5) * 30; // ปัดซ้ายขวานิดเดียว
            const driftY = Math.random() * 50 + 20;    // ให้ร่วงลงมาข้างล่างเหมือนละอองดาว
            const rotation = Math.random() * 180;      // หมุนตัวนิดหน่อย

            const animation = sparkle.animate([
                {
                    opacity: 1,
                    transform: 'translate(-50%, -50%) scale(1) rotate(0deg)'
                },
                {
                    opacity: 0,
                    // เปลี่ยนเป็นเคลื่อนที่ลง (Drift Down) แทนการกระจายออกรอบตัว
                    transform: `translate(calc(-50% + ${driftX}px), calc(-50% + ${driftY}px)) scale(0.2) rotate(${rotation}deg)`
                }
            ], {
                duration: 800 + Math.random() * 400, // สุ่มความช้าเร็วให้ดูเป็นธรรมชาติ
                easing: 'ease-out'
            });

            animation.onfinish = () => sparkle.remove();
        };

        const handleMouseMove = (e: MouseEvent) => {
            const now = Date.now();
            // ปรับจูนความถี่: 50ms จะให้ประกายที่ดูเรียบหรู ไม่รกจนเกินไปครับ
            if (now - lastTime > 40) {
                createSparkle(e.clientX, e.clientY);
                lastTime = now;
            }
        };

        window.addEventListener('mousemove', handleMouseMove);
        return () => window.removeEventListener('mousemove', handleMouseMove);
    }, []);
};