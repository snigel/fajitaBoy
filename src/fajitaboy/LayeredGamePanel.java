package fajitaboy;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;

/**
 * Game panel + eventual menus.
 */
@SuppressWarnings("serial")
public class LayeredGamePanel extends JLayeredPane {

    /** Panel where stuff is drawn. */
    private GamePanel gamePanel;
    /** Panel drawn ontop of the game panel. */
    private JComponent overlapingPane;

    /**
     * Constructor.
     * 
     * @param gp
     *            game panel
     */
    public LayeredGamePanel(final GamePanel gp) {

        gamePanel = gp;
        Dimension gamePanelDimension = gamePanel.getPreferredSize();
        gamePanel.setBounds(0, 0, gamePanelDimension.width,
                gamePanelDimension.height);
        add(gamePanel, JLayeredPane.DEFAULT_LAYER);

    }

    /**
     * Sets a component to be ontop of the game, ie the menu.
     * 
     * @param jc
     *            component
     */
    public final void setOverlapingPane(final JComponent jc) {
        if (overlapingPane != null) {
            remove(overlapingPane);
        }
        Dimension gd = gamePanel.getPreferredSize();
        Dimension cd = jc.getPreferredSize();

        int x = (gd.width - cd.width) / 2;
        int y = (gd.height - cd.height) / 2;

        jc.setBounds(x, y, cd.width, cd.height);
        overlapingPane = jc;
        add(jc, JLayeredPane.POPUP_LAYER);
        validate();

    }

    /**
     * Removes any overlapping stuff, like the menu.
     */
    public final void removeOverlapingPane() {
        if (overlapingPane != null) {
            remove(overlapingPane);
        }
        validate();
    }

}
