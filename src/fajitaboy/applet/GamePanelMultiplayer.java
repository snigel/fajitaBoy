package fajitaboy.applet;

import static fajitaboy.constants.LCDConstants.*;
import java.awt.*;
import java.awt.image.*;

import javax.swing.JPanel;

import fajitaboy.EmulatorCore;
import fajitaboy.VideoReciever;

/**
 * The panel in which the emulator screen will be shown.
 * 
 */
public class GamePanelMultiplayer extends GamePanel {
    private int[] pixels;
    private EmulatorCore core1;
    private EmulatorCore core2;
    private int zoom;
    private Image image;

    protected ColorModel colorModel;
    
    public VideoReciever1 vr1;
    public VideoReciever2 vr2;
    
    boolean vr1updated;
    boolean vr2updated;
    
    class VideoReciever1 implements VideoReciever {
    	
    	GamePanelMultiplayer gpm;
    	
    	public VideoReciever1(GamePanelMultiplayer gpm) {
    		this.gpm = gpm;
    	}
    	
		public void transmitVideo(int[][] data) {
			gpm.transmitVideo(data, 1);
		}
    }
    
    
    class VideoReciever2 implements VideoReciever {
    	
    	GamePanelMultiplayer gpm;
    	
    	public VideoReciever2(GamePanelMultiplayer gpm) {
    		this.gpm = gpm;
    	}
    	
		public void transmitVideo(int[][] data) {
			gpm.transmitVideo(data, 2);
		}
    }

    public GamePanelMultiplayer(int zoom) {
    	vr1 = new VideoReciever1(this);
    	vr2 = new VideoReciever2(this);
    	vr1updated = false;
    	vr2updated = false;
    	
        setIgnoreRepaint(true);
        setPreferredSize(new Dimension(LCD_W * zoom * 2, LCD_H * zoom));

        pixels = new int[160 * 144 * 2];
        this.zoom = zoom;
        colorModel = new DirectColorModel(24, 0x0000FF, 0x00FF00, 0xFF0000);
    }

    /**
     * Draws the screen on the panel.
     * 
     * @param data
     *            The screen matrix to draw
     */
    public final void transmitVideo(final int[][] data, int player) {
    	
    	// refresh pixeldata
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
