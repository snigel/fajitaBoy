package fajitaboy.applet;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class FullScreenFrame extends JFrame {

    public FullScreenFrame(LayeredGamePanel layeredGamePanel) {

        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice screen = ge.getDefaultScreenDevice();
        DisplayMode display = screen.getDisplayMode();

        setContentPane(layeredGamePanel);
        layeredGamePanel.updateSize(display.getWidth(), display.getHeight());

        setResizable(false);
        setUndecorated(true);
        // setAlwaysOnTop(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setTitle("FajitaBoy");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setVisible(true);
        screen.setFullScreenWindow(this);

        validate();
        requestFocusInWindow();
    }
}
