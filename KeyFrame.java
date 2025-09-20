import javax.swing.*;
import java.awt.*;

public class KeyFrame extends JPanel implements Runnable {
    private int personX;
    private int personY;
    private int frameWidth = 600;
    private int frameHeight = 600;
    private int frameIndex = 0;

    public KeyFrame() {
        personX = frameWidth / 2;
        personY = frameHeight / 2;
    }

    @Override
    public void run() {
        while (true) {
            frameIndex++;
            personX += 5; // เดินไปขวา

            // ถ้าคนออกนอกเฟรมแล้วหยุด
            if (personX > frameWidth + 50) break;

            repaint();

            try {
                Thread.sleep(100); // หน่วงความเร็วการเดิน
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, frameWidth, frameHeight);

        g.setColor(Color.BLACK);

        int headSize = 20;

        // วาดหัว
        g.drawOval(personX - headSize / 2, personY - 40, headSize, headSize);
        // วาดลำตัว
        g.drawLine(personX, personY - 20, personX, personY + 20);

        // กำหนดมุมแกว่งแขนขาตาม frameIndex
        int swing = (frameIndex % 20 < 10) ? (frameIndex % 10) * 3 : (9 - (frameIndex % 10)) * 3;

        // แขน
        g.drawLine(personX, personY - 10, personX - 15, personY + 10 + swing);
        g.drawLine(personX, personY - 10, personX + 15, personY + 10 - swing);

        // ขา
        g.drawLine(personX, personY + 20, personX - 15, personY + 40 - swing);
        g.drawLine(personX, personY + 20, personX + 15, personY + 40 + swing);
    }

    public static void main(String[] args) {
        KeyFrame m = new KeyFrame();
        JFrame f = new JFrame();
        f.add(m);
        f.setTitle("Walking Animation");
        f.setSize(600, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        (new Thread(m)).start();
    }
}
