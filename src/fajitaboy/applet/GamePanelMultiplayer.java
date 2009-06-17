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
    
    private boolean vr1updated;
    private boolean vr2updated;
    
    private int minX;
    private int minY; 
    private int maxX; 
    private int maxY;

    
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

        setPreferredSize(new Dimension(LCD_W * zoom * 2, LCD_H * zoom));

        pixels = new int[160 * 144 * 2];
        this.zoom = zoom;
        colorModel = new DirectColorModel(24, 0x0000FF, 0x00FF00, 0xFF0000);
        enabled = true;
        
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE; 
        maxX = Integer.MIN_VALUE; 
        maxY = Integer.MIN_VALUE;
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
    			n += 160;
    		}
    		vr1updated = true;
    	} else if ( player == 2 ) {
    		int n = 160;
    		for (int y = 0; y < 144; y++) {
    			for (int x = 0; x < 160; x++) {
    			    if(pixels[n] != data[y][x]) {
    			        int dx = x + 160;
                        if (dx > maxX) {
                            maxX = dx;
                        }
                        if (dx < minX) {
                            minX = dx;
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
    			n += 160;
    		}
    		vr2updated = true;
    	}

    	if ( vr1updated && vr2updated ) {
            if (minX > maxX || minY > maxY) {
                return;
            }
            maxX++;
            maxY++;
            int dx = maxX - minX;
            int dy = maxY - minY;
            
    		// create image with new pixels
            image = Toolkit.getDefaultToolkit().createImage(
                    new MemoryImageSource(dx, dy, colorModel, pixels, minX + minY*160*2, 160*2));            
            
    		// draw the image
    		Graphics g = getGraphics();
    		if(g != null) {
    		    g.drawImage(image,minX*zoom,minY*zoom,dx*zoom,dy*zoom,null);
    		}
    		vr1updated = false;
    		vr2updated = false;
    		
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE; 
            maxX = Integer.MIN_VALUE; 
            maxY = Integer.MIN_VALUE;
    	}
    }

    public void paint(Graphics g) {
        image = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(160*2, 144, colorModel, pixels, 0, 160*2));
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
