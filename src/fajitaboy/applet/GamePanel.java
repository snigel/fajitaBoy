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
@SuppressWarnings("serial")
public class GamePanel extends JPanel implements VideoReciever {
    private int[] pixels;
    private int zoom;
    private Image image;
    protected boolean enabled;

    protected ColorModel colorModel;

    public GamePanel() {
    }
    
    public GamePanel(int zoom) {
        setPreferredSize(new Dimension(LCD_W * zoom, LCD_H * zoom));
        pixels = new int[160 * 144];
        this.zoom = zoom;
        colorModel = new DirectColorModel(24, 0x0000FF, 0x00FF00, 0xFF0000);
        enabled = true;
    }

    /**
     * Draws the screen on the panel.
     * 
     * @param data
     *            The screen matrix to draw
     */
    public final void transmitVideo(final int[][] data) {
    	
    	if ( enabled == false )
    		return;
    	
      	int minX = Integer.MAX_VALUE;
    	int minY = Integer.MAX_VALUE; 
    	int maxX = Integer.MIN_VALUE; 
    	int maxY = Integer.MIN_VALUE;
    		
    	// refresh pixeldata
        int n = 0;
        for (int y = 0; y < 144; y++) {
            for (int x = 0; x < 160; x++) {
                if(pixels[n] != data[y][x]) {
                	if (x > maxX) {
                		maxX = x;
                	}
                	if (x < minX) {
                		minX = x;
                	}
                	if (y > maxY) {
                		maxY = y;
                	}
                	if (y < minY) {
                		minY = y;
                	}
                	
                    pixels[n] = data[y][x];
                }
                n++;
            }
        }
        
        if (minX > maxX || minY > maxY) {
        	return;
        }
        maxX++;
        maxY++;
        int dx = maxX - minX;
        int dy = maxY - minY;
        // create image with new pixels
        image = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(dx, dy, colorModel, pixels, minX + minY*160, 160));

        
        // draw the image
        Graphics g = getGraphics();
        if(g != null) {
        	g.drawImage(image,minX*zoom,minY*zoom,dx*zoom,dy*zoom,null);
        }
    }

    public void paint(Graphics g) {
        image = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(160, 144, colorModel, pixels, 0, 160));    	
        g.drawImage(image, 0, 0, 160 * zoom, 144 * zoom, null);
    }
    
    /**
     * Sets the new zoom.
     * @param zoom new zoom
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
    
    public void enableVideo(boolean enable) {
    	enabled = enable;
    }
}
