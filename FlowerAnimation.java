import javax.swing.*;
import java.awt.*;

public class FlowerAnimation extends JPanel implements Runnable {
    private int frameIndex = 0;
    private final int totalFrames = 10;

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
        FlowerAnimation m = new FlowerAnimation();

        JFrame f = new JFrame();
        f.add(m);
        f.setTitle("Graphics");
        f.setSize(600, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

        (new Thread(m)).start();
    }

    public void paintComponent(Graphics g){
        
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

    private void plot(Graphics g, int x, int y, int size) {
        // g.fillRect(x, y, 1, 1);
        g.fillRect(x, y, size, size);
    }
}