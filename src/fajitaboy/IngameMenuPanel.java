package fajitaboy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;

import fajitaboy.FajitaBoy.GameState;
import static fajitaboy.constants.PanelConstants.*;

@SuppressWarnings("serial")
public class IngameMenuPanel extends JPanel implements ActionListener {

    FajitaBoy fajitaBoy;

    JButton resume;
    JButton saveState;
    JButton loadState;
    JButton newRom;
    JButton reset;
    JButton mainmenu;

    public IngameMenuPanel(final FajitaBoy fb) {

        fajitaBoy = fb;
        setOpaque(true);

        setPreferredSize(new Dimension(170, 130));
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setLayout(new GridLayout(4, 1));

        resume = new JButton("Resume game");
        saveState = new JButton("Save state");
        loadState = new JButton("Load state");
        newRom = new JButton("Load new ROM");
        reset = new JButton("Reset ROM");
        mainmenu = new JButton("Quit to menu");

        // newRom.setToolTipText(text)

        newRom.setFont(FB_INGAMEFONT);
        saveState.setFont(FB_INGAMEFONT);
        loadState.setFont(FB_INGAMEFONT);
        reset.setFont(FB_INGAMEFONT);
        mainmenu.setFont(FB_INGAMEFONT);
        resume.setFont(FB_INGAMEFONT);

        newRom.setMargin(new Insets(1, 1, 1, 1));

        newRom.addActionListener(this);
        saveState.addActionListener(this);
        loadState.addActionListener(this);
        reset.addActionListener(this);
        mainmenu.addActionListener(this);
        resume.addActionListener(this);

        add(resume);
        // add(saveState);
        // add(loadState);
        add(newRom);
        add(reset);
        add(mainmenu);
    }

    /**
     * @inheritDoc
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == newRom) {
            fajitaBoy.changeGameState(GameState.SINGLEPLAYER_LOADSCREEN);
        } else if (e.getSource() == reset) {
            fajitaBoy.resetEmulator();
            fajitaBoy.changeGameState(GameState.PLAYGAME);
        } else if (e.getSource() == mainmenu) {
            fajitaBoy.changeGameState(GameState.STARTSCREEN);
        } else if (e.getSource() == resume) {
            fajitaBoy.changeGameState(GameState.PLAYGAME);
        }/*
          * else if (e.getSource() == saveState) { fajitaBoy.saveState(); } else
          * if (e.getSource() == loadState) { fajitaBoy.loadState(); }
          */
    }

}
