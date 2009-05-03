package fajitaboy;

import java.awt.image.DirectColorModel;
/**
 * GamePanel that support colors.
 */
public class ColorGamePanel extends GamePanel implements DrawsGameboyScreen {

    /**
     * Creates a new ColorGamePanel.
     * @param zoom the zoom
     */
    public ColorGamePanel(int zoom) {
       super(zoom);
       colorModel = new DirectColorModel(15, 0x0000001F, 0x000003E0, 0x00007C00);
    }
    
}
