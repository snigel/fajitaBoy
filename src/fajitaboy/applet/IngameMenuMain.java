package fajitaboy.applet;

import static fajitaboy.constants.PanelConstants.FB_INGAMEFONT;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import fajitaboy.FajitaBoy;
import fajitaboy.FajitaBoy.GameState;

/**
 * Buttons and stuff for ingame menu.
 */
@SuppressWarnings("serial")
public class IngameMenuMain extends JPanel implements ActionListener {

    /** Applet. */
    private FajitaBoy fajitaBoy;

    private JButton resume;
    private JButton saveState;
    private JButton loadState;
    private JButton newRom;
    private JButton reset;
    private JButton mainmenu;

    /**
     * Contructor.
     * 
     * @param fb
     *            applet
     */
    public IngameMenuMain(final FajitaBoy fb) {
        fajitaBoy = fb;

        setLayout(new GridLayout(6, 1));

        resume = new JButton("Resume game");
        saveState = new JButton("Save state");
        loadState = new JButton("Load state");
        newRom = new JButton("Load new ROM");
        reset = new JButton("Reset ROM");
        mainmenu = new JButton("Quit to menu");

        initKey(resume);
        initKey(saveState);
        initKey(loadState);
        initKey(newRom);
        initKey(reset);
        initKey(mainmenu);
    }

    /**
     * Repetitive button initialization.
     * 
     * @param b
     *            button to init
     */
    private void initKey(final JButton b) {
        b.setFont(FB_INGAMEFONT);
        b.setMargin(new Insets(1, 1, 1, 1));
        b.addActionListener(this);
        add(b);
    }

    /** {@inheritDoc} */
    public final void actionPerformed(final ActionEvent e) {

        if (e.getSource() == newRom) {
            fajitaBoy.changeGameState(GameState.LOADSCREEN);
        } else if (e.getSource() == reset) {
            fajitaBoy.getEmulator().reset();
            fajitaBoy.changeGameState(GameState.PLAYGAME);
        } else if (e.getSource() == mainmenu) {
            fajitaBoy.changeGameState(GameState.STARTSCREEN);
        } else if (e.getSource() == resume) {
            fajitaBoy.changeGameState(GameState.PLAYGAME);
        } else if (e.getSource() == saveState) {
            fajitaBoy.saveState();
        } else if (e.getSource() == loadState) {
            fajitaBoy.loadState();
        }
    }
}
