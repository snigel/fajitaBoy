package fajitaboy.applet;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;

/**
 * Game panel, possibly + menus.
 */
@SuppressWarnings("serial")
public class LayeredGamePanel extends JLayeredPane {

    /** Panel where stuff is drawn. */
    private GamePanel gamePanel;
    /** Panel drawn ontop of the game panel. */
    private JComponent overlapingPane;

    /** Panel width. */
    private int width;

    /** Panel height. */
    private int height;

    int lcdW;

    int lcdH;

    /**
     * Constructor.
     * 
     * @param gp game panel
     */
    public LayeredGamePanel(final GamePanel gp, int lcdW, int lcdH) {
        this.lcdW = lcdW;
        this.lcdH = lcdH;
        gamePanel = gp;
        Dimension gamePanelDimension = gamePanel.getPreferredSize();
        width = gamePanelDimension.width;
        height = gamePanelDimension.height;
        gamePanel.setBounds(0, 0, width, height);
        add(gamePanel, JLayeredPane.DEFAULT_LAYER);

    }

    /**
     * Sets a component to be ontop of the game, ie the menu.
     * 
     * @param jc component
     */
    public final void setOverlapingPane(final JComponent jc) {
        if (overlapingPane != null) {
            remove(overlapingPane);
        }
        Dimension cd = jc.getPreferredSize();

        int x = (width - cd.width) / 2;
        int y = (height - cd.height) / 2;

        jc.setBounds(x, y, cd.width, cd.height);
        overlapingPane = jc;
        add(jc, JLayeredPane.POPUP_LAYER);
        validate();

    }

    /** Removes any overlapping stuff, like the menu. */
    public final void removeOverlapingPane() {
        if (overlapingPane != null) {
            remove(overlapingPane);
        }
        validate();
    }

    /**
     * Updates the size, and updates the zoom and position of the overlapping
     * panel.
     * 
     * @param width the width
     * @param height the height
     */
    public final void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
        int zoom = Math.min(width / lcdW, height / lcdH);
        int x = (width - lcdW * zoom) / 2;
        int y = (height - lcdH * zoom) / 2;
        System.out.println("x: " + x + "y: " + y);
        gamePanel.setBounds(x, y, width, height);
        gamePanel.setZoom(zoom);

        if (overlapingPane != null) {
            Dimension cd = overlapingPane.getPreferredSize();

            x = (width - cd.width) / 2;
            y = (height - cd.height) / 2;

            overlapingPane.setBounds(x, y, cd.width, cd.height);
        }
        validate();
    }

}
