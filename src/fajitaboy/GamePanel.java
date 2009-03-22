package fajitaboy;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * The panel in which the emulator screen will be shown.
 * @author Marcus Johansson, Peter Olsson
 */
@SuppressWarnings("serial")
public final class GamePanel extends JPanel implements DrawsGameboyScreen {

    /**
     * How many times to zoom.
     */
    private int zoom = 1;

    /**
     * Colors to use for the different color codes.
     * This implementation doesn't care about the
     * palettes at address FF47-FF49.
     */
    private Color[] palette = {
            new Color(0xFF, 0xFF, 0xFF),
            new Color(0xAA, 0xAA, 0xAA),
            new Color(55, 55, 55),
            new Color(0, 0, 0) };

    /**
     * Creates a new GamePanel.
     * @param zoom
     *            How much to enlarge the game screen.
     */
    public GamePanel(final int zoom) {
        this.zoom = zoom;
        // this.setFocusable(true);
        // requestFocusInWindow();
        setIgnoreRepaint(true);
    }

    /**
     * Draws the screen on the panel.
     * @param data
     *            The screen data to draw
     */
    public void drawGameboyScreen(final int[][] data) {
        Graphics g = getGraphics();

        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[y].length; x++) {
                g.setColor(palette[data[y][x]]);
                g.fillRect(x * zoom, y * zoom, zoom, zoom);
            }
        }
    }

    /**
     * Changes the zoom.
     * @param newZoom
     *            The new zoom value.
     */
    public void setZoom(final int newZoom) {
        zoom = newZoom;
    }

}
