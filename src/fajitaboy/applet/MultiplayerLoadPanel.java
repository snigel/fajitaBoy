package fajitaboy.applet;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import fajitaboy.FajitaBoy;
import fajitaboy.FajitaBoy.GameState;
import static fajitaboy.constants.PanelConstants.*;

/**
 * Singleplayer panel where the user cna pick a rom and start a singleplayer
 * game.
 * 
 * @author Peter Olsson, Marcus Johansson
 */
@SuppressWarnings("serial")
public class MultiplayerLoadPanel extends JPanel implements ActionListener {

    /** Input field for rom path. */
    private JTextField fileStringField;
    /** Browse files button. */
    private JButton loadFileButton;
    /** Button for starting the game. */
    private JButton startGameButton;
    /** Button for going back to main menu. */
    private JButton backButton;
    /** Reference to the global filechooser. */
    private JFileChooser fileChooser;
    /** Reference to the parent FajitaBoy. */
    private FajitaBoy fajitaBoy;

    /**
     * Standard constructor.
     * 
     * @param fb Reference to the FajitaBoy
     * @param jfc Reference to the filechooser
     */
    public MultiplayerLoadPanel(final FajitaBoy fb, final JFileChooser jfc) {

        fileChooser = jfc;
        fajitaBoy = fb;
        loadFileButton = new JButton("Browse...");
        startGameButton = new JButton("Start Game");
        backButton = new JButton("Back");
        fileStringField = new JTextField(12);

        setOpaque(true);
        setPreferredSize(fajitaBoy.getPreferredSize());

        loadFileButton.addActionListener(this);
        startGameButton.addActionListener(this);
        fileStringField.addActionListener(this);
        backButton.addActionListener(this);

        backButton.setPreferredSize(startGameButton.getPreferredSize());

        fileStringField.setText("/tetris.gb");

        JLabel title = new JLabel("Multiplayer Game");
        JLabel loadText = new JLabel("Select a ROM path");

        title.setFont(new Font("Verdana", Font.BOLD, 20));

        add(title);
        add(loadText);
        add(fileStringField);
        add(loadFileButton);
        add(startGameButton);
        add(backButton);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        SpringLayout.Constraints constraints = layout.getConstraints(this);

        SpringLayout.Constraints titleCons = layout.getConstraints(title);
        titleCons.setX(Spring.sum(Spring.constant(10), constraints
                .getConstraint(SpringLayout.WEST)));
        titleCons.setY(Spring.sum(Spring.constant(10), constraints
                .getConstraint(SpringLayout.NORTH)));

        SpringLayout.Constraints loadCons = layout.getConstraints(loadText);
        loadCons.setX(titleCons.getConstraint(SpringLayout.WEST));
        loadCons.setY(Spring.sum(Spring.constant(15), titleCons
                .getConstraint(SpringLayout.SOUTH)));

        SpringLayout.Constraints pathCons = layout
                .getConstraints(fileStringField);
        pathCons.setX(titleCons.getConstraint(SpringLayout.WEST));
        pathCons.setY(Spring.sum(Spring.constant(5), loadCons
                .getConstraint(SpringLayout.SOUTH)));

        SpringLayout.Constraints browseCons = layout
                .getConstraints(loadFileButton);
        browseCons.setX(Spring.sum(Spring.constant(5), pathCons
                .getConstraint(SpringLayout.EAST)));
        browseCons.setY(Spring.sum(Spring.constant(-3), pathCons
                .getConstraint(SpringLayout.NORTH)));

        SpringLayout.Constraints backCons = layout.getConstraints(backButton);
        backCons.setX(Spring.sum(Spring.constant(30), constraints
                .getConstraint(SpringLayout.WEST)));
        backCons.setY(Spring.sum(Spring.constant(-50), constraints
                .getConstraint(SpringLayout.SOUTH)));

        SpringLayout.Constraints startCons = layout
                .getConstraints(startGameButton);
        startCons.setX(Spring.sum(Spring.constant(-150), constraints
                .getConstraint(SpringLayout.EAST)));
        startCons.setY(backCons.getConstraint(SpringLayout.NORTH));

    }

    /**
     * Loads a the previously loaded path from a cookie.
     */
    public final void loadPath() {
        String path = fajitaBoy.getCookieJar().get(COOKIE_PATH);

        if (path == null) {
            return; // No previous path found.
        }
        fileStringField.setText(path);
        File file = new File(path);
        if (FajitaBoy.checkFile(file)) {
            fileChooser.setSelectedFile(file);
        }
    }

    /** {@inheritDoc} */
    public final void actionPerformed(final ActionEvent e) {

        if (e.getSource() == loadFileButton) {
            File file;

            // TODO update fileChooser to fileStringField

            int retVal = fileChooser.showOpenDialog(this);

            if (retVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                fileStringField.setText(file.getAbsolutePath());
                System.out.println(file.getAbsolutePath());
            }

        } else if (e.getSource() == startGameButton
                || e.getSource() == fileStringField) {
            File path = new File(fileStringField.getText());

            if (FajitaBoy.checkFile(path)) {
                fajitaBoy.getCookieJar().put(COOKIE_PATH,
                        fileStringField.getText());
                fajitaBoy.startGameMultiplayer(fileStringField.getText());
            }
        } else if (e.getSource() == backButton) {
            fajitaBoy.changeGameState(GameState.STARTSCREEN);
        }
    }
}
