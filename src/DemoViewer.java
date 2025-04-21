import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class DemoViewer {

    public static void main(String[] args) {

//        An array of vertices for our triangles
        List<Triangle> tris = new ArrayList<>();

        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));
//        initialise the viewing frame
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

//         Handle Rotations/Sliding

//        Slider for horizontal rotation
        JSlider headingSlider = new JSlider(0, 360, 180); // min max val
        pane.add(headingSlider, BorderLayout.SOUTH);



//        Slider for vertical position
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

//        Display the actual frame
        JPanel renderPanel = new JPanel() {



            public void paintComponent(Graphics g) {


                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0,0, getWidth(), getHeight());
                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);

                double heading = Math.toRadians(headingSlider.getValue());
                Matrix3 transform = new Matrix3(new double[]{
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });

                for (Triangle t : tris) {
//                    Path2D path = new Path2D.Double();
//                    path.moveTo(t.v1.x, t.v1.y);
//                    path.lineTo(t.v2.x, t.v2.y);
//                    path.lineTo(t.v3.x, t.v3.y);
//                    path.closePath();
//                    g2.draw(path);
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);

                }
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);
        frame.setSize(400, 400);
        frame.setVisible(true);

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());


    }
//    Creating Vertex and Triangle Types

//    Vertex
    private static class Vertex {
        double x;
        double y;
        double z;
        Vertex(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;

        }
    }
//    Triangle
    private static class Triangle {
        Vertex v1;
        Vertex v2;
        Vertex v3;
        Color color;
        Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.color = color;
        }

    }
//    Matrix Multiplication for rotations
    private static class Matrix3 {
        double[] values;
        Matrix3(double[] values) {
            this.values = values;
        }
        Matrix3 multiply(Matrix3 other) {
            double[] result = new double[9];
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    for (int i = 0; i< 3; i++) {
                        result[row * 3 + col] +=
                                this.values[row *3 + i] * other.values[i * 3 + col];
                    }
                }
            }
            return new Matrix3(result);
        }
//        Handles the rotating of the prism
        Vertex transform(Vertex in) {
            return new Vertex(
                    in.x * values[0] + in.y * values[3] + in.z * values[6],
                    in.x * values[1] + in.y * values[4] + in.z * values[7],
                    in.x * values[2] + in.y * values[5] + in.z * values[8]);
        }
    }

}
