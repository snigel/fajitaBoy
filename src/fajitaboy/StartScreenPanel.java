package fajitaboy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import fajitaboy.FajitaBoy.GameState;

/**
 * Panel for the startup screen, will show a choice between single and
 * multiplayer game.
 * 
 * @author Peter Olsson, Marcus Johansson
 */
@SuppressWarnings("serial")
public class StartScreenPanel extends JPanel implements ActionListener {

    /** Startscreen's reference to FajitaBoy. Needed to change state */
    private FajitaBoy fajitaBoy;

    /** Button for going to singleplayer mode. */
    private JButton singleplayerButton;
    /** Button for going to multiplayer mode. */
    private JButton multiplayerButton;

    /**
     * Standard constructor.
     * 
     * @param fb
     *            Reference to parent FajitaBoy
     */
    public StartScreenPanel(final FajitaBoy fb) {
        fajitaBoy = fb;

        setOpaque(true);

        singleplayerButton = new JButton("Singleplayer");
        multiplayerButton = new JButton("Multiplayer");

        singleplayerButton.addActionListener(this);
        multiplayerButton.addActionListener(this);

        add(singleplayerButton);
        add(multiplayerButton);
    }

    /**
     * @inheritDoc
     * @param e
     *            ActionEvent
     */
    public final void actionPerformed(final ActionEvent e) {

        if (e.getSource() == singleplayerButton) {
            fajitaBoy.changeGameState(GameState.SINGLEPLAYER_LOADSCREEN);
        } else if (e.getSource() == multiplayerButton) {
            System.out.println("Not implemented! :<");
        }
    }

}
