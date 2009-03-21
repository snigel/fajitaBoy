package fajitaboy;

import static fajitaboy.constants.LCDConstants.GB_LCD_H;
import static fajitaboy.constants.LCDConstants.GB_LCD_W;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.*;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The panel in which the emulator screen will be shown.
 * 
 * @author Marcus Johansson, Peter Olsson
 * 
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel {

    private int zoom = 1;
    private BufferedImage bufferImage;
    private Color[] palette = { new Color(0xFF, 0xFF, 0xFF),
                new Color(0xAA, 0xAA, 0xAA), 
                new Color(55, 55, 55),
                new Color(0, 0, 0) };

    /**
     * Creates a new GamePanel
     * 
     * @param zoom
     *            How much to enlarge the game screen.
     */
    public GamePanel(final int zoom) {
        this.zoom = zoom;
        //this.setFocusable(true);
        //requestFocusInWindow();
        setIgnoreRepaint(true);
    }

    /**
     * Draws the screen on the panel.
     * 
     * @param scr
     *            The screen matrix to draw
     */
    public void draw(int[][] scr) {
        Graphics g = getGraphics();

        for (int y = 0; y < scr.length; y++) {
            for (int x = 0; x < scr[y].length; x++) {
                g.setColor(palette[scr[y][x]]);
                g.fillRect(x * zoom, y * zoom, zoom, zoom);
            }
        }
    }
    
    /**
     * Changes the zoom.
     * @param newZoom The new zoom value.
     */
    public void setZoom(final int newZoom) {
        zoom = newZoom;
    }
}
