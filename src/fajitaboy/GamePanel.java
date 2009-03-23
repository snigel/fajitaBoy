package fajitaboy;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.JPanel;

// Instantiate this class and then use the draw() method to draw the
// generated on the graphics context.
public class GamePanel extends JPanel implements DrawsGameboyScreen {
	GamePanelPixels gpp;
	int[] pixels;
	int zoom;
	
	public GamePanel( int zoom ) {
		pixels = new int[160*144];
		this.zoom = zoom;
		gpp = new GamePanelPixels(pixels);
	}

	void MyCanvas() {
		// Add a listener for resize events
		addComponentListener(new ComponentAdapter() {
			// This method is called when the component's size changes
			public void componentResized(ComponentEvent evt) {
				Component c = (Component)evt.getSource();

				// Get new size
				Dimension newSize = c.getSize();

				// Regenerate the image
				//gpp = new GamePanelPixels(pixels);
				c.repaint();
			}
		});
	}
	
	public void paint(Graphics g) {
		if (gpp != null) {
			gpp.draw(g, 0, 0, 160*zoom, 144*zoom);
		}
	}

	public void drawGameboyScreen(int[][] data) {
		int n = 0;
		for( int i = 0; i < 144; i++ ) {
			for( int j = 0; j < 160; j++ ) {
				pixels[n] = data[i][j];
				n++;
			}
		}
		//gpp = new GamePanelPixels(pixels);
		gpp.refresh(pixels);
		repaint();
	}
}

class GamePanelPixels {
	// Holds the generated image
	Image image;

	// 16-color model
	ColorModel colorModel;
	private byte[] palette;

	public GamePanelPixels(int[] data) {
		int width = 160;
		int height = 144;
		
		palette = new byte[4];
		palette[0] = (byte) 0xFF;
		palette[1] = (byte) 0xAA;
		palette[2] = (byte) 0x55;
		palette[3] = (byte) 0x00;
		colorModel = new IndexColorModel(2, 4, palette, palette, palette);
		
		refresh(data);
	}
	
	public void refresh(int[] data) {
		image = Toolkit.getDefaultToolkit().createImage( 
				new MemoryImageSource(160, 144,
						colorModel, data, 0, 160));
	}
	
	public void draw(Graphics g, int x, int y, int w, int h) {
		g.drawImage(image, x, y, w, h, null);
	}
}