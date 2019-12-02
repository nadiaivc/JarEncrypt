import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class GameWindow extends JFrame {

    private static GameWindow game_window;
    private static long last_frame_time;
    private static Image background;
    private static Image game_over;
    private static Image cat;
    private static float drop_left = 0;
    private static float drop_top = -100;
    private static float drop_v = 100;
    private static int score;

    public static void main(String[] args) throws IOException {
        start();
    }

    public static void start () throws IOException{
        background =ImageIO.read(GameWindow .class.

                getResourceAsStream("back.jpg"));
        game_over =ImageIO.read(GameWindow .class.

                getResourceAsStream("end.png"));
        cat =ImageIO.read(GameWindow .class.

                getResourceAsStream("cat.png"));

        game_window =new GameWindow();
        game_window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game_window.setLocation(900,300);

        game_window.setSize(800,800);
        last_frame_time =System.nanoTime();
        GameField game_field = new GameField();
        game_field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed (MouseEvent e){
                int x = e.getX();
                int y = e.getY();
                float drop_right = drop_left + cat.getWidth(null);
                float drop_bottom = drop_top + cat.getWidth(null);
                boolean is_drop = x >= drop_left && x <= drop_right && y >= drop_top && y <= drop_bottom;
                if (is_drop) {
                    drop_left = (int) (Math.random() * (game_window.getWidth() - cat.getWidth(null)));
                    drop_top = -100;
                    drop_v = drop_v + 10;
                    score++;
                    game_window.setTitle("Score: " + score);
                }
            }
        });
        game_window.add(game_field);
        game_window.setVisible(true);
    }

    private static void onRepaint (Graphics g) {
        long current_time = System.nanoTime();
        float delta_time = (current_time - last_frame_time) * 0.000000001f;
        last_frame_time = current_time;
        drop_top = drop_top + drop_v * delta_time;

        g.drawImage(background, 0, 0, null);
        g.drawImage(cat, (int) drop_left, (int) drop_top, null);

        if (drop_top > game_window.getHeight())  g.drawImage(game_over, 300, 100, null);
    }

    private static class GameField extends JPanel {
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponents(g);
            onRepaint(g);
            repaint();
        }
    }
}