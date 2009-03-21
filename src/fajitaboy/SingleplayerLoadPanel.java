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
 * 
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

        loadFileButton.addActionListener(this);
        startGameButton.addActionListener(this);
        fileStringField.addActionListener(this);

        fileStringField.setText("YOUR ROM FILE PATH HERE :D");

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

            if (!path.exists()) {
                errorMsg("File doesn't exist, try again.");
                return;
            }
            if (!path.isFile()) {
                errorMsg("No file selected, try again.");
                return;
            }
            if (!path.canRead()) {
                errorMsg("File cannot be read, try again.");
                return;
            }

            fajitaBoy.startGame(fileStringField.getText());
        }

    }

    /**
     * Shows an error message.
     * 
     * @param msg
     *            What to show in the box
     */
    private void errorMsg(final String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

}
