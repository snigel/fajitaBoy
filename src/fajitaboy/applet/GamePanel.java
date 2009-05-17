package fajitaboy.applet;

import static fajitaboy.constants.LCDConstants.*;
import java.awt.*;
import java.awt.image.*;

import javax.swing.JPanel;

import fajitaboy.VideoReciever;

/**
 * The panel in which the emulator screen will be shown.
 * 
 */
public class GamePanel extends JPanel implements VideoReciever {
    private int[] pixels;
    private int zoom;
    private Image image;

    protected ColorModel colorModel;

    public GamePanel() {
    }
    
    public GamePanel(int zoom) {
        setIgnoreRepaint(true);
        setPreferredSize(new Dimension(LCD_W * zoom, LCD_H * zoom));

        pixels = new int[160 * 144];
        this.zoom = zoom;
        colorModel = new DirectColorModel(24, 0x0000FF, 0x00FF00, 0xFF0000);
    }

    /**
     * Draws the screen on the panel.
     * 
     * @param data
     *            The screen matrix to draw
     */
    public final void transmitVideo(final int[][] data) {
    	
    	// refresh pixeldata
        int n = 0;
        for (int i = 0; i < 144; i++) {
            for (int j = 0; j < 160; j++) {
                pixels[n] = data[i][j];
                n++;
            }
        }

        // create image with new pixels
        image = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(160, 144, colorModel, pixels, 0, 160));
        // draw the image
        Graphics g = getGraphics();
        if(g != null) {
        	g.drawImage(image, 0, 0, 160 * zoom, 144 * zoom, null);
        }
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, 160 * zoom, 144 * zoom, null);
    }
    
    /**
     * Sets the new zoom.
     * @param zoom new zoom
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}
