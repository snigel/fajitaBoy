package fajitaboy;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;

public class LayeredGamePanel extends JLayeredPane {

    private GamePanel gamePanel;
    private JComponent overlapingPane;

    public LayeredGamePanel(GamePanel gp) {

        gamePanel = gp;
        Dimension gamePanelDimension = gamePanel.getPreferredSize();
        gamePanel.setBounds(0, 0, gamePanelDimension.width,
                gamePanelDimension.height);
        add(gamePanel, JLayeredPane.DEFAULT_LAYER);
    }

    public void setOverlapingPane(JComponent jc) {
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

    public void removeOverlapingPane() {
        if (overlapingPane != null) {
            remove(overlapingPane);
        }
        validate();
    }

}
