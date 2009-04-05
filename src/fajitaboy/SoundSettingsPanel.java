package fajitaboy;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Volume control etc.
 */
@SuppressWarnings("serial")
public class SoundSettingsPanel extends JPanel {

    /**
     * Constructor.
     */
    public SoundSettingsPanel() {
        super();
        JLabel text = new JLabel("Sound is cool.");
        add(text);
        validate();
    }
}
