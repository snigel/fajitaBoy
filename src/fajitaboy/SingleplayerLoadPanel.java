package fajitaboy;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Singleplayer panel where the user cna pick a rom and start a singleplayer
 * game.
 * 
 * @author Peter Olsson, Marcus Johansson
 */
@SuppressWarnings("serial")
public class SingleplayerLoadPanel extends JPanel implements ActionListener {

    /** Input field for rom path. */
    private JTextField fileStringField;
    /** Browse files button. */
    private JButton loadFileButton;
    /** Button for starting the game. */
    private JButton startGameButton;
    /** Reference to the global filechooser. */
    private JFileChooser fileChooser;
    /** Reference to the parent FajitaBoy. */
    private FajitaBoy fajitaBoy;

    /**
     * Standard constructor.
     * 
     * @param fb
     *            Reference to the FajitaBoy
     * @param jfc
     *            Reference to the filechooser
     */
    public SingleplayerLoadPanel(final FajitaBoy fb, final JFileChooser jfc) {

        fileChooser = jfc;
        fajitaBoy = fb;
        loadFileButton = new JButton("Browse...");
        startGameButton = new JButton("Start Game");
        fileStringField = new JTextField(20);

        setOpaque(true);

        loadFileButton.addActionListener(this);
        startGameButton.addActionListener(this);
        fileStringField.addActionListener(this);

        fileStringField.setText("/tetris.gb");

        add(fileStringField);
        add(loadFileButton);
        add(startGameButton);
    }

    /**
     * @inheritDoc
     * @param e
     *            ActionEvent
     */
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
                fajitaBoy.startGame(fileStringField.getText());
            }
        }

    }

}
