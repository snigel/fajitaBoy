package fajitaboy.applet;

import static fajitaboy.constants.LCDConstants.*;
import java.awt.*;
import java.awt.image.*;

import fajitaboy.VideoReciever;

/**
 * The panel in which the emulator screen will be shown.
 * 
 */
@SuppressWarnings("serial")
public class GamePanelMultiplayer extends GamePanel {
    private int[] pixels;
    private int zoom;
    private Image image;

    protected ColorModel colorModel;
    
    public GPVideoReciever vr1;
    public GPVideoReciever vr2;
    
    boolean vr1updated;
    boolean vr2updated;
    
    class GPVideoReciever implements VideoReciever {
    	
    	GamePanelMultiplayer gpm;
    	int id;
    	
    	public GPVideoReciever(GamePanelMultiplayer gpm, int id) {
    		this.gpm = gpm;
    		this.id = id;
    	}
    	
		public void transmitVideo(int[][] data) {
			gpm.transmitVideo(data, id);
		}

		public void enableVideo(boolean enable) {			
		}
    }

    public GamePanelMultiplayer(int zoom) {
    	vr1 = new GPVideoReciever(this,1);
    	vr2 = new GPVideoReciever(this,2);
    	vr1updated = false;
    	vr2updated = false;
    	
        setIgnoreRepaint(true);
        setPreferredSize(new Dimension(LCD_W * zoom * 2, LCD_H * zoom));

        pixels = new int[160 * 144 * 2];
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
    public final void transmitVideo(final int[][] data, int player) {
    	
    	if (!enabled)
    		return;
    		
    	// refresh pixel data
    	if ( player == 1 ) {
    		int n = 0;
    		for (int i = 0; i < 144; i++) {
    			for (int j = 0; j < 160; j++) {
    				pixels[n] = data[i][j];
    				n++;
    			}
    			n += 160;
    		}
    		vr1updated = true;
    	} else if ( player == 2 ) {
    		int n = 160;
    		for (int i = 0; i < 144; i++) {
    			for (int j = 0; j < 160; j++) {
    				pixels[n] = data[i][j];
    				n++;
    			}
    			n += 160;
    		}
    		vr2updated = true;
    	}

    	if ( vr1updated && vr2updated ) {
    		// create image with new pixels
    		image = Toolkit.getDefaultToolkit().createImage(
    				new MemoryImageSource(160*2, 144, colorModel, pixels, 0, 160*2));
    		// draw the image
    		Graphics g = getGraphics();
    		if(g != null) {
    			g.drawImage(image, 0, 0, 160 * zoom * 2, 144 * zoom, null);
    		}
    		vr1updated = false;
    		vr2updated = false;
    	}
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, 160 * zoom * 2, 144 * zoom, null);
    }
    
    /**
     * Sets the new zoom.
     * @param zoom new zoom
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}
