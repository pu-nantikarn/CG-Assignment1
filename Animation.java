import javax.swing.*;
import java.awt.*;

public class Animation extends JPanel implements Runnable {
    private int frameIndex = 0;
    private final int totalFrames = 36;

    @Override
    public void run() {
        while (true) {
            frameIndex++;
            if (frameIndex >= totalFrames)
                frameIndex = 0;

            repaint();

            try {
                Thread.sleep(200); // 0.5 วินาทีต่อเฟรม
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Animation m = new Animation();

        JFrame f = new JFrame();
        f.add(m);
        f.setTitle("Graphics");
        f.setSize(600, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        (new Thread(m)).start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double stickmanX = centerX;
        double stickmanY = centerY;

        // ขนาดตัวละคร
        int headWidth = 100;
        int headHeight = 80;
        int bodyHeight = 100;
        int armLength = 60;
        int legLength = 100;

        int bodyTopY = (int) stickmanY + headHeight;
        int bodyBottomY = bodyTopY + bodyHeight;

        // --- ช่วงเดินออกจากเฟรม ---
        if (frameIndex >= 16 && frameIndex <= 30) {
            int stepFrame = frameIndex - 16;

            stickmanX += stepFrame * 30;

            // ถ้าขาลง ให้ขยับเพิ่มไปข้างหน้า
            if (stepFrame % 2 != 0) {
                stickmanX += 30;
            }

            double angleDeg = (stepFrame % 2 == 0) ? 80 : 100;

            g2.rotate(Math.toRadians(angleDeg - 90), stickmanX, stickmanY + headHeight / 2);

            // หัว
            midpointEllipse(g2, (int) stickmanX, (int) stickmanY, headWidth, headHeight);
            g2.fillOval((int) stickmanX - 25, (int) stickmanY - 10, 16, 20);
            g2.fillOval((int) stickmanX + 25, (int) stickmanY - 10, 16, 20);

            // ตัว
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) stickmanX, bodyTopY, (int) stickmanX, bodyBottomY);

            // แขน
            g2.drawLine((int) stickmanX, bodyTopY, (int) stickmanX - armLength, bodyTopY + 50);
            g2.drawLine((int) stickmanX, bodyTopY, (int) stickmanX + armLength, bodyTopY + 50);

            // ขา
            int legStartY = bodyBottomY;
            if (stepFrame % 2 == 0) {
                // ขาขวายก
                g2.drawLine((int) stickmanX, legStartY, (int) stickmanX - 30, legStartY + legLength);
                g2.drawLine((int) stickmanX, legStartY, (int) stickmanX + 40, legStartY + legLength - 20);
            } else {
                // ขาซ้ายยก
                g2.drawLine((int) stickmanX, legStartY, (int) stickmanX - 30, legStartY + legLength);
                g2.drawLine((int) stickmanX, legStartY, (int) stickmanX + 40, legStartY + legLength);
            }

            g2.rotate(Math.toRadians(-(angleDeg - 90)), stickmanX, stickmanY + headHeight / 2);
            return;
        }

        // --- ช่วงหัวตกลงมาหลังจากเดินหลุด ---
        if (frameIndex > 30 && frameIndex <= 36) {
            int dropFrames = frameIndex - 30;

            // จุดเริ่มตก: ด้านบนสุดของหน้าจอ
            int startY = -100;
            // จุดปลาย: กลางจอ (centerY)
            int endY = centerY;

            // คำนวณตำแหน่ง Y ของหัวตามเฟรม
            double progress = dropFrames / 6.0; // 0 ถึง 1
            int headY = (int) (startY + (endY - startY) * progress);

            // วาดหัว
            midpointEllipse(g2, centerX, headY, headWidth, headHeight);
            g2.fillOval(centerX - 25, headY - 10, 16, 20);
            g2.fillOval(centerX + 25, headY - 10, 16, 20);
            return;
        }

        // --- เฟรม 0–15: ยืนเฉย ---
        if (frameIndex >= 0) {
            midpointEllipse(g2, centerX, centerY, headWidth, headHeight);
            g2.fillOval(centerX - 25, centerY - 10, 16, 20);
            g2.fillOval(centerX + 25, centerY - 10, 16, 20);
        }
        // ตัวงอก 3 เฟรม
        if (frameIndex >= 3) {
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int bodyProgress = Math.min(bodyHeight, (frameIndex - 3 + 1) * (bodyHeight / 3));
            g2.drawLine(centerX, bodyTopY, centerX, bodyTopY + bodyProgress);
        }

        // แขนงอก 2 เฟรม
        if (frameIndex >= 6) {
            int armProgress = Math.min(armLength, (frameIndex - 6 + 1) * (armLength / 2));
            g2.drawLine(centerX, bodyTopY, centerX - armProgress, bodyTopY + 50);
        }
        if (frameIndex >= 8) {
            int armProgress = Math.min(armLength, (frameIndex - 8 + 1) * (armLength / 2));
            g2.drawLine(centerX, bodyTopY, centerX + armProgress, bodyTopY + 50);
        }

        // ขางอก 2 เฟรม
        if (frameIndex >= 10) {
            int legProgress = Math.min(legLength, (frameIndex - 10 + 1) * (legLength / 2));
            g2.drawLine(centerX, bodyBottomY, centerX - 20, bodyBottomY + legProgress);
        }
        if (frameIndex >= 12) {
            int legProgress = Math.min(legLength, (frameIndex - 12 + 1) * (legLength / 2));
            g2.drawLine(centerX, bodyBottomY, centerX + 20, bodyBottomY + legProgress);
        }
    }

    private void midpointEllipse(Graphics2D g2, int xc, int yc, int a, int b) {
        int x, y, d;

        x = 0;
        y = b;
        d = Math.round(b * b - a * a * b + a * a / 4);
        while (b * b * x <= a * a * y) {
            plot(g2, x + xc, y + yc, 6);
            plot(g2, -x + xc, y + yc, 6);
            plot(g2, x + xc, -y + yc, 6);
            plot(g2, -x + xc, -y + yc, 6);

            x++;

            if (d >= 0) {
                y--;
                d = d - 2 * a * a * y;
            }
            d = d + 2 * b * b * x + b * b;

        }

        x = a;
        y = 0;
        d = Math.round(a * a - b * b * a + b * b / 4);
        while (b * b * x >= a * a * y) {
            plot(g2, x + xc, y + yc, 6);
            plot(g2, -x + xc, y + yc, 6);
            plot(g2, x + xc, -y + yc, 6);
            plot(g2, -x + xc, -y + yc, 6);

            y++;

            if (d >= 0) {
                x--;
                d = d - 2 * b * b * x;
            }
            d = d + 2 * a * a * y + a * a;
        }
    }

    private void plot(Graphics2D g2, int x, int y, int size) {
        g2.fillRect(x , y, size, size);
    }
}
