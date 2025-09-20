import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class Assignment1_66050194_66050803 extends JPanel implements Runnable {

    private final int WIDTH = 600, HEIGHT = 600;
    private boolean isOvumAnimationActive = true;
    private Ovum ovum;
    private ArrayList<Sperm> sperms = new ArrayList<>();

    private boolean glitchActive = false;
    private int glitchTimer = 0;
    private final int glitchDuration = 30;
    private BufferedImage noiseImage;
    private double ovumAnimationTime = 0;

    private int frameIndex = 0;
    private final int totalFrames = 36;
    private long lastFrameTime;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Assignment1_66050194_66050803");
        Assignment1_66050194_66050803 panel = new Assignment1_66050194_66050803();
        frame.add(panel);
        frame.pack();
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        (new Thread(panel)).start();
    }

    public Assignment1_66050194_66050803() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);

        ovum = new Ovum(WIDTH / 2, HEIGHT / 2);
        noiseImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        setupOvumAnimation();

        lastFrameTime = System.currentTimeMillis();
    }

    private void setupOvumAnimation() {
        sperms.clear();
        addSperm(-50, HEIGHT - 150, WIDTH / 2, HEIGHT / 2);
        addSperm(WIDTH / 2, HEIGHT + 50, WIDTH / 2, HEIGHT / 2);
        addSperm(WIDTH + 50, HEIGHT - 150, WIDTH / 2, HEIGHT / 2);
        addSperm(-50, 150, WIDTH / 2, HEIGHT / 2);
        addSperm(WIDTH / 2, -50, WIDTH / 2, HEIGHT / 2);
        addSperm(WIDTH + 50, 150, WIDTH / 2, HEIGHT / 2);
        glitchActive = false;
        glitchTimer = 0;
        ovumAnimationTime = 0;
        ovum.reset();
    }

    private void addSperm(double sx, double sy, double tx, double ty) {
        Sperm s = new Sperm(sx, sy);
        double dx = tx - sx;
        double dy = ty - sy;
        double len = Math.sqrt(dx * dx + dy * dy);
        s.vx = dx / len;
        s.vy = dy / len;
        sperms.add(s);
    }

    @Override
    public void run() {
        while (true) {
            if (isOvumAnimationActive) {
                long now = System.currentTimeMillis();
                long delta = now - lastFrameTime;
                lastFrameTime = now;
                updateOvumAnimation(delta);
                repaint();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                updateStickmanAnimation();
                repaint();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateOvumAnimation(long delta) {
        ovumAnimationTime += delta / 1000.0;
        ovum.wobble();

        if (glitchActive) {
            glitchTimer++;
            if (glitchTimer >= glitchDuration) {
                isOvumAnimationActive = false;
                frameIndex = 0;
            }
        } else {
            for (int i = 0; i < sperms.size(); i++) {
                Sperm s = sperms.get(i);
                if (!s.stopped && !s.penetrated) {
                    s.update(ovumAnimationTime);
                    if (checkCollision(s, ovum)) {
                        if (i == 4) {
                            glitchActive = true;
                            glitchTimer = 0;
                            s.penetrated = true;
                        } else {
                            s.stopped = true;
                        }
                    }
                } else {
                    s.update(ovumAnimationTime);
                }
            }
        }
    }

    private void updateStickmanAnimation() {
        frameIndex++;
        if (frameIndex >= totalFrames) {
            isOvumAnimationActive = true;
            setupOvumAnimation();
        }
    }

    private boolean checkCollision(Sperm s, Ovum ovum) {
        double dx = s.x - ovum.x;
        double dy = s.y - ovum.y;
        return dx * dx + dy * dy < ovum.radius * ovum.radius;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isOvumAnimationActive) {
            if (glitchActive) {
                drawTVStatic(g2);
            } else {
                int wobbleX = (int) (5 * Math.sin(ovum.angle));
                int wobbleY = (int) (5 * Math.cos(ovum.angle));
                int headWidth = 100;
                int headHeight = 80;

                g2.setColor(Color.WHITE);
                midpointEllipse(g2, (int) getWidth() / 2 + wobbleX, (int) getHeight() / 2 + wobbleY, headWidth,headHeight);

                g2.setStroke(new BasicStroke(3f));
                g2.setColor(Color.BLACK);
                midpointEllipse(g2, (int) getWidth() / 2 + wobbleX, (int) getHeight() / 2 + wobbleY, headWidth,headHeight);

                BufferedImage eyeImg = new BufferedImage(1200, 601, BufferedImage.TYPE_INT_ARGB);

                int eyeWidth = headWidth / 12; // 100/12 ≈ 8
                int eyeHeight = headHeight / 6; // 80/6 ≈ 13
                int leftEyeX = getWidth() / 2 - 25 + wobbleX; // 100/6 ≈ 17
                int rightEyeX = getWidth() / 2 + 25 + wobbleX;
                int eyeY = getHeight() / 2 - headHeight / 16 + wobbleY; // 80/16 ≈ 5

                drawEye(g2, eyeImg, leftEyeX, eyeY, eyeWidth, eyeHeight, Color.BLACK);
                drawEye(g2, eyeImg, rightEyeX, eyeY, eyeWidth, eyeHeight, Color.BLACK);

                for (Sperm s : sperms) {
                    s.draw(g2, ovumAnimationTime, this);
                }
            }
        } else {
            drawStickmanAnimation(g2);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawStickmanAnimation(Graphics2D g2) {
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double stickmanX = centerX;
        double stickmanY = centerY;

        int headWidth = 100;
        int headHeight = 80;
        int bodyHeight = 100;
        int armLength = 60;
        int legLength = 100;
        int bodyTopY = (int) stickmanY + headHeight;
        int bodyBottomY = bodyTopY + bodyHeight;

        BufferedImage eyeImg = new BufferedImage(1200, 601, BufferedImage.TYPE_INT_ARGB);
        int eyeWidth = headWidth / 12;
        int eyeHeight = headHeight / 6; 
        int eyeOffsetX = 25; 
        int eyeOffsetY = headHeight / 16;

        if (frameIndex >= 16 && frameIndex <= 30) {
            int stepFrame = frameIndex - 16;
            stickmanX += stepFrame * 30;
            if (stepFrame % 2 != 0) {
                stickmanX += 30;
            }
            double angleDeg = (stepFrame % 2 == 0) ? 80 : 100;
            g2.rotate(Math.toRadians(angleDeg - 90), stickmanX, stickmanY + headHeight / 2);
            
            // head
            midpointEllipse(g2, (int) stickmanX, (int) stickmanY, headWidth, headHeight);
           
            // eye
            drawEye(g2, eyeImg, (int) stickmanX - eyeOffsetX, centerY - eyeOffsetY, eyeWidth, eyeHeight, Color.BLACK);
            drawEye(g2, eyeImg, (int) stickmanX + eyeOffsetX, centerY - eyeOffsetY, eyeWidth, eyeHeight, Color.BLACK);

            //body
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bresenhamLine(g2, (int) stickmanX, bodyTopY, (int) stickmanX, bodyBottomY);
            bresenhamLine(g2, (int) stickmanX, bodyTopY, (int) stickmanX - armLength, bodyTopY + 50);
            bresenhamLine(g2, (int) stickmanX, bodyTopY, (int) stickmanX + armLength, bodyTopY + 50);

            int legStartY = bodyBottomY;
            if (stepFrame % 2 == 0) {
                bresenhamLine(g2, (int) stickmanX, legStartY, (int) stickmanX - 30, legStartY + legLength);
                bresenhamLine(g2, (int) stickmanX, legStartY, (int) stickmanX + 40, legStartY + legLength - 20);
            } else {
                bresenhamLine(g2, (int) stickmanX, legStartY, (int) stickmanX - 30, legStartY + legLength);
                bresenhamLine(g2, (int) stickmanX, legStartY, (int) stickmanX + 40, legStartY + legLength);
            }

            g2.rotate(Math.toRadians(-(angleDeg - 90)), stickmanX, stickmanY + headHeight / 2);
            return;
        }

        if (frameIndex > 30 && frameIndex <= 36) {
            int dropFrames = frameIndex - 30;
            int startY = -100;
            int endY = centerY;
            double progress = dropFrames / 6.0;
            int headY = (int) (startY + (endY - startY) * progress);
            midpointEllipse(g2, centerX, headY, headWidth, headHeight);

            drawEye(g2, eyeImg, centerX - eyeOffsetX, headY - eyeOffsetY, eyeWidth, eyeHeight, Color.BLACK);
            drawEye(g2, eyeImg, centerX + eyeOffsetX, headY - eyeOffsetY, eyeWidth, eyeHeight, Color.BLACK);
            return;
        }

        if (frameIndex >= 0) {
            midpointEllipse(g2, centerX, centerY, headWidth, headHeight);
            drawEye(g2, eyeImg, centerX - eyeOffsetX, centerY - eyeOffsetY, eyeWidth, eyeHeight, Color.BLACK);
            drawEye(g2, eyeImg, centerX + eyeOffsetX, centerY - eyeOffsetY, eyeWidth, eyeHeight, Color.BLACK);
        }

        if (frameIndex >= 3) {
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int bodyProgress = Math.min(bodyHeight, (frameIndex - 3 + 1) * (bodyHeight / 3));
            bresenhamLine(g2, centerX, bodyTopY, centerX, bodyTopY + bodyProgress);
        }

        if (frameIndex >= 6) {
            int armProgress = Math.min(armLength, (frameIndex - 6 + 1) * (armLength / 2));
            bresenhamLine(g2, centerX, bodyTopY, centerX - armProgress, bodyTopY + 50);
        }

        if (frameIndex >= 8) {
            int armProgress = Math.min(armLength, (frameIndex - 8 + 1) * (armLength / 2));
            bresenhamLine(g2, centerX, bodyTopY, centerX + armProgress, bodyTopY + 50);
        }

        if (frameIndex >= 10) {
            int legProgress = Math.min(legLength, (frameIndex - 10 + 1) * (legLength / 2));
            bresenhamLine(g2, centerX, bodyBottomY, centerX - 20, bodyBottomY + legProgress);
        }

        if (frameIndex >= 12) {
            int legProgress = Math.min(legLength, (frameIndex - 12 + 1) * (legLength / 2));
            bresenhamLine(g2, centerX, bodyBottomY, centerX + 20, bodyBottomY + legProgress);
        }
    }

    public void drawEye(Graphics2D g2, BufferedImage tempImg, int centerX, int centerY, int width, int height,Color fillColor) {
        Graphics2D tg = tempImg.createGraphics();
        tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tg.setColor(Color.BLACK);
        midpointEllipse(tg, centerX, centerY, width / 2, height / 2);

        floodFill(tempImg, centerX, centerY, fillColor, Color.BLACK);

        g2.drawImage(tempImg, 0, 0, null);
        tg.dispose();
    }

    public BufferedImage floodFill(BufferedImage m, int x, int y, Color targetColour, Color replacementColour) {
        if (targetColour.equals(replacementColour)) return m;
        
        int width = m.getWidth();
        int height = m.getHeight();
        
        Color currentColor = new Color(m.getRGB(x, y), true);
        if (!currentColor.equals(targetColour)) return m;

        Queue<Point> Q = new LinkedList<>();
        m.setRGB(x, y, replacementColour.getRGB());
        Q.add(new Point(x, y));

        while (!Q.isEmpty()) {
            Point current = Q.poll();
            int cx = current.x;
            int cy = current.y;

            // SOUTH
            if (cy + 1 < height && new Color(m.getRGB(cx, cy + 1), true).equals(targetColour)) {
                m.setRGB(cx, cy + 1, replacementColour.getRGB());
                Q.add(new Point(cx, cy + 1));
            }

            // NORTH
            if (cy - 1 >= 0 && new Color(m.getRGB(cx, cy - 1), true).equals(targetColour)) {
                m.setRGB(cx, cy - 1, replacementColour.getRGB());
                Q.add(new Point(cx, cy - 1));
            }

            // EAST
            if (cx + 1 < width && new Color(m.getRGB(cx + 1, cy), true).equals(targetColour)) {
                m.setRGB(cx + 1, cy, replacementColour.getRGB());
                Q.add(new Point(cx + 1, cy));
            }

            // WEST
            if (cx - 1 >= 0 && new Color(m.getRGB(cx - 1, cy), true).equals(targetColour)) {
                m.setRGB(cx - 1, cy, replacementColour.getRGB());
                Q.add(new Point(cx - 1, cy));
            }
        }

        return m;
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
        g2.fillRect(x, y, size, size);
    }

    public void bresenhamLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            plot(g2, x1, y1, 8);
            if (x1 == x2 && y1 == y2) {
                break;
            }
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void drawTVStatic(Graphics2D g2d) {
        WritableRaster raster = noiseImage.getRaster();
        int[] rgb = new int[3];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int gray = rnd.nextInt(256);
                if (rnd.nextDouble() < 0.005)
                    gray = 0;
                else if (rnd.nextDouble() < 0.005)
                    gray = 255;
                rgb[0] = gray;
                rgb[1] = gray;
                rgb[2] = gray;
                raster.setPixel(x, y, rgb);
            }
        }

        g2d.drawImage(noiseImage, 0, 0, null);
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
        for (int y = 0; y < HEIGHT; y += 2) {
            g2d.setColor(Color.BLACK);
            bresenhamLine(g2d, 0, y, WIDTH, y);
        }

        g2d.setComposite(old);
        int bands = 3;
        for (int i = 0; i < bands; i++) {
            int bandH = 20 + rnd.nextInt(35);
            int y = rnd.nextInt(HEIGHT - bandH);
            int shift = rnd.nextInt(-12, 13);
            BufferedImage band = noiseImage.getSubimage(0, y, WIDTH, bandH);
            g2d.drawImage(band, shift, y, null);
        }

        float radius = Math.max(WIDTH, HEIGHT);
        Point2D center = new Point2D.Float(WIDTH / 2f, HEIGHT / 2f);
        float[] dist = { 0f, 0.8f, 1f };
        Color[] colors = { new Color(0, 0, 0, 0), new Color(0, 0, 0, 80), new Color(0, 0, 0, 180) };
        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
        Paint oldPaint = g2d.getPaint();
        g2d.setPaint(rgp);
        g2d.setPaint(oldPaint);
    }
}

class Ovum {
    int x, y;
    int radius = 100;
    double angle = 0;

    public Ovum(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void wobble() {
        angle += 0.1;
    }

    public void reset() {
        angle = 0;
    }
}

class Sperm {
    double x, y;
    double vx = 0, vy = 0;
    double speed = 2.5;
    int headA = 8;
    int headB = 4;
    List<PointD> tailCtrl = new ArrayList<>();
    double phase = Math.random() * Math.PI * 2;
    double age = 0;
    boolean stopped = false;
    boolean penetrated = false;

    public Sperm(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        for (int i = 1; i <= 6; i++)
            tailCtrl.add(new PointD(x, y));
    }

    public void update(double t) {
        age = t;
        if (!stopped && !penetrated) {
            x += vx * speed;
            y += vy * speed;
        }

        double nx = -vx, ny = -vy;
        double px = -ny, py = nx;

        for (int i = 0; i < tailCtrl.size(); i++) {
            double baseDist = (i + 2) * 12;
            double bx = x + nx * baseDist;
            double by = y + ny * baseDist;
            double wave = Math.sin(age * 6.0 - i * 0.6 + phase) * (4 + i * 0.6);
            double cx = bx + px * wave;
            double cy = by + py * wave;
            tailCtrl.get(i).x = cx;
            tailCtrl.get(i).y = cy;
        }
    }

    public void draw(Graphics2D g2, double time, Assignment1_66050194_66050803 panel) {
        if (penetrated) return;
        
        AffineTransform old = g2.getTransform();
        List<PointD> ctrl = new ArrayList<>();
        ctrl.add(new PointD(x - vx * 6, y - vy * 6));
        ctrl.addAll(tailCtrl);
        List<Point> sampled = new ArrayList<>();
       
        for (int i = 0; i <= ctrl.size() - 4; i++) {
            PointD p0 = ctrl.get(i), p1 = ctrl.get(i + 1), p2 = ctrl.get(i + 2), p3 = ctrl.get(i + 3);
            int steps = 12;
            
            for (int s = 0; s <= steps; s++) {
                double tt = s / (double) steps;
                PointD pt = bsplinePoint(p0, p1, p2, p3, tt);
                sampled.add(new Point((int) Math.round(pt.x), (int) Math.round(pt.y)));
            }
        }

        for (int i = 1; i < sampled.size(); i++) {
            g2.setColor(new Color(0, 0, 0));
            Point p1 = sampled.get(i - 1);
            Point p2 = sampled.get(i);
            Stroke oldS = g2.getStroke();
            float w = (float) Math.max(0.6, 3.0 - i * 0.08f);
            g2.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            bresenhamLine(g2, p1.x, p1.y, p2.x, p2.y);
            g2.setStroke(oldS);
        }

        double ang = Math.atan2(vy, vx);
        g2.rotate(ang, x, y);
        g2.setColor(Color.BLACK);
        midpointEllipse(g2, (int) Math.round(x) - headA, (int) Math.round(y) - headB, headA * 2, headB * 2);
        g2.setTransform(old);
    }

    private void plot(Graphics2D g2, int x, int y, int size) {
        g2.fillRect(x, y, size, size);
    }

    public void bresenhamLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            plot(g2, x1, y1, 2);
            if (x1 == x2 && y1 == y2) {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void midpointEllipse(Graphics2D g2, int xc, int yc, int a, int b) {
        int x, y, d;

        x = 0;
        y = b;
        d = Math.round(b * b - a * a * b + a * a / 4);
        while (b * b * x <= a * a * y) {
            plot(g2, x + xc, y + yc, 2);
            plot(g2, -x + xc, y + yc, 2);
            plot(g2, x + xc, -y + yc, 2);
            plot(g2, -x + xc, -y + yc, 2);

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
            plot(g2, x + xc, y + yc, 2);
            plot(g2, -x + xc, y + yc, 2);
            plot(g2, x + xc, -y + yc, 2);
            plot(g2, -x + xc, -y + yc, 2);

            y++;

            if (d >= 0) {
                x--;
                d = d - 2 * b * b * x;
            }
            d = d + 2 * a * a * y + a * a;
        }
    }

    private static PointD bsplinePoint(PointD p0, PointD p1, PointD p2, PointD p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double B0 = (1 - 3 * t + 3 * t2 - t3) / 6.0;
        double B1 = (4 - 6 * t2 + 3 * t3) / 6.0;
        double B2 = (1 + 3 * t + 3 * t2 - 3 * t3) / 6.0;
        double B3 = t3 / 6.0;
        double x = B0 * p0.x + B1 * p1.x + B2 * p2.x + B3 * p3.x;
        double y = B0 * p0.y + B1 * p1.y + B2 * p2.y + B3 * p3.y;
        
        return new PointD(x, y);
    }
}

class PointD {
    double x, y;

    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }
}