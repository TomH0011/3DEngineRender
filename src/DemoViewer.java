import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DemoViewer {

    public static void main(String[] args) {

        // ------------------------------
        // Define 3D Model (List of Triangles)
        // ------------------------------
        List<Triangle> tris = new ArrayList<>();

        tris.add(new Triangle(new Vertex(100, 100, 100), new Vertex(-100, -100, 100), new Vertex(-100, 100, -100), Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100), new Vertex(-100, -100, 100), new Vertex(100, -100, -100), Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100), new Vertex(100, -100, -100), new Vertex(100, 100, 100), Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100), new Vertex(100, -100, -100), new Vertex(-100, -100, 100), Color.BLUE));

        // ------------------------------
        // Setup JFrame and Layout
        // ------------------------------
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // ------------------------------
        // Create Sliders (Heading + Pitch)
        // ------------------------------
        JSlider headingSlider = new JSlider(0, 360, 180); // Horizontal rotation
        pane.add(headingSlider, BorderLayout.SOUTH);

        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0); // Vertical rotation
        pane.add(pitchSlider, BorderLayout.EAST);

        // ------------------------------
        // Render Panel (Custom JPanel)
        // ------------------------------
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                // Clear background
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Move origin to center
                g2.translate(getWidth() / 2, getHeight() / 2);

                // Get slider values as radians
                double heading = Math.toRadians(headingSlider.getValue());
                double pitch = Math.toRadians(pitchSlider.getValue());

                // Create transformation matrices
                Matrix3 headingTransform = new Matrix3(new double[]{
                        Math.cos(heading), 0, Math.sin(heading),
                        0, 1, 0,
                        -Math.sin(heading), 0, Math.cos(heading)
                });

                Matrix3 pitchTransform = new Matrix3(new double[]{
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch),
                        0, -Math.sin(pitch), Math.cos(pitch)
                });

                Matrix3 transform = headingTransform.multiply(pitchTransform);

                // Draw each transformed triangle
                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();

                    g2.setColor(t.color);
                    g2.draw(path);
                }

                BufferedImage img =
                        new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    // since we are not using Graphics2D anymore,
                    // we have to do translation manually
                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    // compute rectangular bounds for triangle
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1,
                            Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1,
                            Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea =
                            (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 =
                                    ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 =
                                    ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 =
                                    ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                img.setRGB(x, y, t.color.getRGB());
                            }
                        }
                    }

                }

                g2.drawImage(img, 0, 0, null);
            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);

        // ------------------------------
        // Final Frame Setup
        // ------------------------------
        frame.setSize(400, 400);
        frame.setVisible(true);

        // Repaint on slider movement
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
    }

    // ------------------------------
    // Supporting Classes
    // ------------------------------

    private static class Vertex {
        double x, y, z;

        Vertex(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }
    }

    private static class Triangle {
        Vertex v1, v2, v3;
        Color color;

        Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
            this.v1 = v1; this.v2 = v2; this.v3 = v3; this.color = color;
        }
    }

    private static class Matrix3 {
        double[] values;

        Matrix3(double[] values) {
            this.values = values;
        }

        Matrix3 multiply(Matrix3 other) {
            double[] result = new double[9];
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    for (int i = 0; i < 3; i++) {
                        result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
                    }
                }
            }
            return new Matrix3(result);
        }

        Vertex transform(Vertex in) {
            return new Vertex(
                    in.x * values[0] + in.y * values[3] + in.z * values[6],
                    in.x * values[1] + in.y * values[4] + in.z * values[7],
                    in.x * values[2] + in.y * values[5] + in.z * values[8]
            );
        }
    }
}
