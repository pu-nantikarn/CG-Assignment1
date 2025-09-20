import javax.swing.*;
import java.awt.*;

public class SunAnimation extends JPanel implements Runnable {
    private int frameIndex = 0;
    private final int totalFrames = 15;

    @Override
    public void run() {
        while (true) {
            frameIndex++;
            if (frameIndex >= totalFrames)
                frameIndex = 0;

            repaint();

            try {
                Thread.sleep(500); // 0.5 วินาทีต่อเฟรม
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SunAnimation m = new SunAnimation();

        JFrame f = new JFrame();
        f.add(m);
        f.setTitle("Graphics");
        f.setSize(600, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        (new Thread(m)).start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        g.setColor(Color.BLUE);

        int panelCenterX = getWidth() / 2;
        int panelCenterY = getHeight() / 2;

        // คำนวณตำแหน่งและรัศมีสำหรับเฟรมนี้
        int startX = -200, startY = -200, startR = 250;
        int endX = panelCenterX, endY = panelCenterY, endR = 90;

      int xc, yc, r;

        if (frameIndex <= 6) {
            // เฟรม 0-5 : เคลื่อนที่และลดรัศมี
            double t = frameIndex / 6.0;
            xc = (int) (startX + t * (endX - startX));
            yc = (int) (startY + t * (endY - startY));
            r = (int) (startR + t * (endR - startR));
        } else {
            // เฟรม 6-9 : อยู่ตรงกลาง + ดุ๊กดิ๊ก
            xc = endX;
            yc = endY;
            r = endR;
        }

        // วาดวงกลมโดย midpointCircle
        midpointCircle(g, xc, yc, r);

        // เพิ่มดวงตาตั้งแต่เฟรม 6 เป็นต้นไป
        if (frameIndex >= 7) {
            g.setColor(Color.BLACK);

            // ตำแหน่งตาเล็กน้อยจากจุดกึ่งกลาง (relative to xc,yc)
            int eyeYOffset = r / 4;
            int eyeXOffset = r / 3;

            
        int eyeWidth = 12;

        // ความสูงตาขยายขึ้นตามเฟรม (เฟรม 6-9)
        int maxEyeHeight = 20;
        int framesToOpen = totalFrames - 7; // 4 เฟรม: 6,7,8,9

        int currentEyeHeight = 5; // ปิดตาเริ่มต้น
        int f = frameIndex - 7;

        if (f <= framesToOpen) {
            currentEyeHeight = 1 + (int) ((maxEyeHeight - 1) * (f / (double) framesToOpen));
        } else {
            currentEyeHeight = maxEyeHeight;
        }

        // วาดตาซ้าย - ใช้ fillOval ให้เป็นวงรี ถมสีดำ
        g.fillOval(xc - eyeXOffset - eyeWidth/2, yc - eyeYOffset - currentEyeHeight, eyeWidth, currentEyeHeight);

        // วาดตาขวา
        g.fillOval(xc + eyeXOffset - eyeWidth/2, yc - eyeYOffset - currentEyeHeight, eyeWidth, currentEyeHeight);
        }

    }

    private void midpointCircle(Graphics g, int xc, int yc, int r) {
        int x = 0;
        int y = r;
        int d = 1 - r;
        while (x <= y) {
            plot(g, x + xc, y + yc, 3);
            plot(g, -x + xc, y + yc, 3);
            plot(g, x + xc, -y + yc, 3);
            plot(g, -x + xc, -y + yc, 3);
            plot(g, y + xc, x + yc, 3);
            plot(g, -y + xc, x + yc, 3);
            plot(g, y + xc, -x + yc, 3);
            plot(g, -y + xc, -x + yc, 3);

            x++;

            if (d >= 0) {
                y--;
                d = d - 2 * y;
            }
            d = d + 2 * x;
        }
    }

    private void midpointEllipse(Graphics g, int xc, int yc, int a, int b) {
        int x, y, d;

        // region 1
        x = 0;
        y = b;
        d = Math.round(b * b - a * a * b + a * a / 4);
        while (b * b * x <= a * a * y) {
            plot(g, x + xc, y + yc, 3);
            plot(g, -x + xc, y + yc, 3);
            plot(g, x + xc, -y + yc, 3);
            plot(g, -x + xc, -y + yc, 3);

            x++;

            if (d >= 0) {
                y--;
                d = d - 2 * a * a * y;
            }
            d = d + 2 * b * b * x + b * b;

        }

        // region 2
        x = a;
        y = 0;
        d = Math.round(a * a - b * b * a + b * b / 4);
        while (b * b * x >= a * a * y) {
            plot(g, x + xc, y + yc, 3);
            plot(g, -x + xc, y + yc, 3);
            plot(g, x + xc, -y + yc, 3);
            plot(g, -x + xc, -y + yc, 3);

            y++;

            if (d >= 0) {
                x--;
                d = d - 2 * b * b * x;
            }
            d = d + 2 * a * a * y + a * a;
        }
    }

    private void plot(Graphics g, int x, int y, int size) {
        // g.fillRect(x, y, 1, 1);
        g.fillRect(x, y, size, size);
    }

}
