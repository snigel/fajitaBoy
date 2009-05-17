package fajitaboy.applet;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import fajitaboy.FajitaBoy;
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
        setPreferredSize(fajitaBoy.getPreferredSize());

        singleplayerButton = new JButton("Singleplayer");
        multiplayerButton = new JButton("Multiplayer");

        multiplayerButton.setPreferredSize(singleplayerButton
                .getPreferredSize());

        singleplayerButton.addActionListener(this);
        multiplayerButton.addActionListener(this);

        add(singleplayerButton);
        add(multiplayerButton);

        URL myurl = fajitaBoy.getClass().getResource("resources/fajitaboi.gif");

        ImageIcon icon = new ImageIcon(myurl);
        JLabel ic = new JLabel(icon);
        JLabel text = new JLabel("FajitaBoy");

        text.setFont(new Font("Verdana", Font.BOLD, 50));

        add(ic);
        add(text);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        SpringLayout.Constraints constraints = layout.getConstraints(this);

        SpringLayout.Constraints singleCons = layout
                .getConstraints(singleplayerButton);
        singleCons.setX((Spring.sum(Spring.sum(Spring.constant(-30), Spring
                .minus(Spring.width(singleplayerButton))), constraints
                .getConstraint(SpringLayout.EAST))));
        singleCons.setY(Spring.sum(Spring.constant(120), constraints
                .getConstraint(SpringLayout.NORTH)));

        SpringLayout.Constraints multiCons = layout
                .getConstraints(multiplayerButton);
        multiCons.setX(singleCons.getConstraint(SpringLayout.WEST));
        multiCons.setY(Spring.sum(Spring.constant(5), singleCons
                .getConstraint(SpringLayout.SOUTH)));

        SpringLayout.Constraints imageCons = layout.getConstraints(ic);
        imageCons.setX(Spring.sum(Spring.constant(10), constraints
                .getConstraint(SpringLayout.WEST)));
        imageCons.setY(Spring.sum(Spring.constant(-200), constraints
                .getConstraint(SpringLayout.SOUTH)));

        SpringLayout.Constraints textCons = layout.getConstraints(text);
        textCons.setX(Spring.sum(Spring.constant(22), constraints
                .getConstraint(SpringLayout.WEST)));
        textCons.setY(Spring.sum(Spring.constant(10), constraints
                .getConstraint(SpringLayout.NORTH)));

        validate();
    }

    /** {@inheritDoc} */
    public final void actionPerformed(final ActionEvent e) {

        if (e.getSource() == singleplayerButton) {
            fajitaBoy.changeGameState(GameState.SINGLEPLAYER_LOADSCREEN);
        } else if (e.getSource() == multiplayerButton) {
            fajitaBoy.changeGameState(GameState.MULTIPLAYER_LOADSCREEN);
        } else if (e.getSource() == multiplayerButton) {
            System.out.println("Not implemented! :<");
        }
    }

}
