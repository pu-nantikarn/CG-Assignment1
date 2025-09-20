import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OvumAnimation extends JPanel implements Runnable {
    final int WIDTH = 600, HEIGHT = 600;
    private Thread animator;
    private boolean running = true;
    private Ovum ovum;
    private ArrayList<Sperm> sperms = new ArrayList<>();

    // ---- จอซ่า (แทนแฟลช) ----
    private boolean glitchActive = false;
    private int glitchTimer = 0;
    private final int glitchDuration = 66; // ~2 วินาทีที่ 30fps
    private BufferedImage noiseImage; // buffer สำหรับ noise

    private int frameCount = 0;
    private double time = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ovum Animation");
        OvumAnimation panel = new OvumAnimation();
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.start();
    }

    public OvumAnimation() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        ovum = new Ovum(WIDTH / 2, HEIGHT / 2);

        // เตรียม buffer สำหรับ noise 1 ครั้ง
        noiseImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // สร้างสเปิร์มจาก 6 ทิศทาง (vx, vy ถูก normalize)
        addSperm(-50, HEIGHT - 150, WIDTH / 2, HEIGHT / 2); // bottom-left
        addSperm(WIDTH / 2, HEIGHT + 50, WIDTH / 2, HEIGHT / 2); // bottom-center
        addSperm(WIDTH + 50, HEIGHT - 150, WIDTH / 2, HEIGHT / 2); // bottom-right
        addSperm(-50, 150, WIDTH / 2, HEIGHT / 2); // top-left
        addSperm(WIDTH / 2, -50, WIDTH / 2, HEIGHT / 2); // top-center (ชนะ)
        addSperm(WIDTH + 50, 150, WIDTH / 2, HEIGHT / 2); // top-right
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

    public void start() {
        animator = new Thread(this);
        animator.start();
    }

    public void run() {
        while (running) {
            updateAnimation();
            repaint();

            try {
                Thread.sleep(30); // ~33 fps
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAnimation() {
        frameCount++;
        time += 0.03;

        if (glitchActive) {
            glitchTimer++;
            if (glitchTimer >= glitchDuration) {
                running = false;
            }
        }

        for (int i = 0; i < sperms.size(); i++) {
            Sperm s = sperms.get(i);
            if (!s.stopped && !s.penetrated) {
                s.update(time);
                if (checkCollision(s, ovum)) {
                    if (i == 4) { // ตัวชนะ (top-center)
                        glitchActive = true;
                        glitchTimer = 0;
                        s.penetrated = true;
                    } else {
                        s.stopped = true;
                    }
                }
            } else {
                s.update(time);
            }
        }

        ovum.wobble();
    }

    private boolean checkCollision(Sperm s, Ovum ovum) {
        double dx = s.x - ovum.x;
        double dy = s.y - ovum.y;
        return dx * dx + dy * dy < ovum.radius * ovum.radius;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (glitchActive) {
            drawTVStatic(g2); // จอซ่าสมจริง
            Toolkit.getDefaultToolkit().sync();
            return;
        }

        ovum.draw(g2);
        for (Sperm s : sperms) {
            s.draw(g2, time);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    // ---------- วาด TV Static/Glitch ----------
    private void drawTVStatic(Graphics2D g2d) {
        // 1) เติม noise ลงใน noiseImage
        WritableRaster raster = noiseImage.getRaster();
        int[] rgb = new int[3];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // ปรับ contrast และสัดส่วนเกรนเล็กน้อยด้วยการ bias ค่า
        // หมายเหตุ: ลูปเต็มพิกเซล 600x600 = 360k พิกเซล
        // ต่อเฟรมพอไหวสำหรับเดสก์ท็อปทั่วไป
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int gray = rnd.nextInt(256);
                // ใส่โอกาสเกิดเกรนดำ/ขาวจัด (salt & pepper) เพิ่มมิติ
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

        // 2) วาด noise
        g2d.drawImage(noiseImage, 0, 0, null);

        // 3) สแกนไลน์แนวนอน (โปร่งใส)
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
        for (int y = 0; y < HEIGHT; y += 2) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(0, y, WIDTH, y);
        }
        g2d.setComposite(old);

        // 4) แถบสัญญาณรบกวนเคลื่อนที่ (glitch bands)
        int bands = 3;
        for (int i = 0; i < bands; i++) {
            int bandH = 20 + rnd.nextInt(35);
            int y = rnd.nextInt(HEIGHT - bandH);
            int shift = rnd.nextInt(-12, 13);
            // ตัดซับอิมเมจช่วงแถบแล้วเลื่อนซ้าย/ขวาเล็กน้อย
            BufferedImage band = noiseImage.getSubimage(0, y, WIDTH, bandH);
            g2d.drawImage(band, shift, y, null);
        }

        // 5) Vignette ขอบมืด ให้ความรู้สึกหลอดภาพ
        float radius = Math.max(WIDTH, HEIGHT);
        Point2D center = new Point2D.Float(WIDTH / 2f, HEIGHT / 2f);
        float[] dist = { 0f, 0.8f, 1f };
        Color[] colors = { new Color(0, 0, 0, 0), new Color(0, 0, 0, 80), new Color(0, 0, 0, 180) };
        RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
        Paint oldPaint = g2d.getPaint();
        g2d.setPaint(rgp);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setPaint(oldPaint);
    }

    // ---------- midpointEllipse helper ----------
    public static void midpointEllipse(Graphics2D g, int xc, int yc, int a, int b) {
        int x = 0, y = b;
        int a2 = a * a, b2 = b * b;
        int fx = 0, fy = 2 * a2 * y;
        int p = (int) (b2 - a2 * b + 0.25 * a2);
        while (fx < fy) {
            plotEllipsePoints(g, xc, yc, x, y);
            x++;
            fx += 2 * b2;
            if (p < 0) {
                p += b2 + fx;
            } else {
                y--;
                fy -= 2 * a2;
                p += b2 + fx - fy;
            }
        }
        p = (int) (b2 * (x + 0.5) * (x + 0.5) + a2 * (y - 1) * (y - 1) - a2 * b2);
        while (y >= 0) {
            plotEllipsePoints(g, xc, yc, x, y);
            y--;
            fy -= 2 * a2;
            if (p >= 0) {
                p += a2 - fy;
            } else {
                x++;
                fx += 2 * b2;
                p += a2 - fy + fx;
            }
        }
    }

    private static void plotEllipsePoints(Graphics2D g, int xc, int yc, int x, int y) {
        g.fillRect(xc + x, yc + y, 1, 1);
        g.fillRect(xc - x, yc + y, 1, 1);
        g.fillRect(xc + x, yc - y, 1, 1);
        g.fillRect(xc - x, yc - y, 1, 1);
    }
}

// ---------- Ovum ----------
class Ovum {
    int x, y;
    int radius = 100;
    boolean transforming = false;
    double angle = 0;

    double eyeOpenLevel = 0.0;

    public Ovum(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void wobble() {
        angle += 0.1;
        // กระพริบตาแบบ sine นุ่มนวล
        eyeOpenLevel = (Math.sin(angle) + 1) / 2;
        if (eyeOpenLevel > 1)
            eyeOpenLevel = 1;
        if (eyeOpenLevel < 0)
            eyeOpenLevel = 0;
    }

    public void draw(Graphics2D g) {
        int wobbleX = (int) (5 * Math.sin(angle));
        int wobbleY = (int) (5 * Math.cos(angle));

        // ตัว ovum
        g.setColor(Color.WHITE);
        g.fillOval(x - radius + wobbleX, y - radius + wobbleY, radius * 2, radius * 2);

        g.setColor(Color.BLACK); // สีขอบ
        g.setStroke(new BasicStroke(3f));
        g.drawOval(x - radius + wobbleX, y - radius + wobbleY, radius * 2, radius * 2);

        // ดวงตา (วงรีกว้างกว่าสูง)
        g.setColor(Color.BLACK);
        int eyeWidth = 20;
        int maxEyeHeight = 25;
        int eyeHeight = Math.max(1, (int) (maxEyeHeight * eyeOpenLevel)); // ไม่ให้ 0 เพื่อเห็นเส้นหลับตาบางๆ

        // ตาซ้าย
        g.fillOval(
                x - 30 - eyeWidth / 2 + wobbleX,
                y - 20 - eyeHeight / 2 + wobbleY,
                eyeWidth,
                eyeHeight);

        // ตาขวา
        g.fillOval(
                x + 30 - eyeWidth / 2 + wobbleX,
                y - 20 - eyeHeight / 2 + wobbleY,
                eyeWidth,
                eyeHeight);

    }
}

// ---------- Sperm ----------
class Sperm {
    // head position (center)
    double x, y;
    // normalized heading vector
    double vx = 0, vy = 0;
    double speed = 2.5;

    // head ellipse radii
    int headA = 10; // radius in motion direction (x)
    int headB = 6; // radius perpendicular (y)

    // tail control points for B-spline
    List<PointD> tailCtrl = new ArrayList<>();
    double phase = Math.random() * Math.PI * 2;
    double age = 0;

    // flags
    boolean stopped = false; // stopped because collided with ovary
    boolean penetrated = false; // penetrated (top-center)

    public Sperm(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        // create some tail control placeholders (will be updated each frame)
        for (int i = 1; i <= 6; i++)
            tailCtrl.add(new PointD(x, y));
    }

    // update position and tail control points
    public void update(double t) {
        age = t;
        if (!stopped && !penetrated) {
            x += vx * speed;
            y += vy * speed;
        }
        // recompute tail control points behind head, with waving offsets
        double nx = -vx, ny = -vy; // backward direction
        double px = -ny, py = nx; // perpendicular for wave
        for (int i = 0; i < tailCtrl.size(); i++) {
            double baseDist = (i + 1) * 12;
            double bx = x + nx * baseDist;
            double by = y + ny * baseDist;
            double wave = Math.sin(age * 6.0 - i * 0.6 + phase) * (4 + i * 0.6);
            double cx = bx + px * wave;
            double cy = by + py * wave;
            tailCtrl.get(i).x = cx;
            tailCtrl.get(i).y = cy;
        }
    }

    // draw tail (B-spline) then head (midpoint ellipse)
    public void draw(Graphics2D g2, double time) {
        if (penetrated) {
            // do not draw any longer once penetrated (so it disappears at flash)
            return;
        }
        // save transform
        AffineTransform old = g2.getTransform();

        // build control list: head point plus tail control points
        List<PointD> ctrl = new ArrayList<>();
        ctrl.add(new PointD(x - vx * 6, y - vy * 6));
        ctrl.addAll(tailCtrl);

        // sample cubic uniform B-spline segments (4 control points per segment)
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

        // draw tail as lines with fading alpha and decreasing thickness
        for (int i = 1; i < sampled.size(); i++) {
            int alpha = (int) Math.max(30, 220 - i * 6);
            alpha = Math.min(255, alpha);
            g2.setColor(new Color(0, 0, 0, alpha));
            Point p1 = sampled.get(i - 1);
            Point p2 = sampled.get(i);
            Stroke oldS = g2.getStroke();
            float w = (float) Math.max(0.6, 3.0 - i * 0.08f);
            g2.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            g2.setStroke(oldS);
        }

        // draw head rotated to velocity
        double ang = Math.atan2(vy, vx);
        g2.rotate(ang, x, y);
        g2.setColor(Color.DARK_GRAY);
        g2.fillOval((int) Math.round(x) - headA, (int) Math.round(y) - headB, headA * 2, headB * 2);

        // restore transform
        g2.setTransform(old);
    }

    // cubic uniform B-spline evaluator
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