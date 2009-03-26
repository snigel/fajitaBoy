package fajitaboy;

import java.awt.*;
import java.awt.image.*;

import javax.swing.JPanel;

/**
 * The panel in which the emulator screen will be shown.
 *
 */
public class GamePanel extends JPanel implements DrawsGameboyScreen {
    private int[] pixels;
    private int zoom;

    private ColorModel colorModel;
    private byte[] palette;

    public GamePanel(int zoom) {
        setIgnoreRepaint(true);

        pixels = new int[160 * 144];
        this.zoom = zoom;
        // set pallette
        palette = new byte[4];
        palette[0] = (byte) 0xFF;
        palette[1] = (byte) 0xAA;
        palette[2] = (byte) 0x55;
        palette[3] = (byte) 0x00;
        colorModel = new IndexColorModel(2, 4, palette, palette, palette);
    }

    /**
     * Draws the screen on the panel.
     * @param data
     *            The screen matrix to draw
     */
    public final void drawGameboyScreen(final int[][] data) {

        // refresh pixeldata
        int n = 0;
        for (int i = 0; i < 144; i++) {
            for (int j = 0; j < 160; j++) {
                pixels[n] = data[i][j];
                n++;
            }
        }

        // create image with new pixels
        Image image = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(160, 144, colorModel, pixels, 0, 160));

        // draw the image
        Graphics g = getGraphics();
        g.drawImage(image, 0, 0, 160 * zoom, 144 * zoom, null);
    }
}
