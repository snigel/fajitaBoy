package fajitaboy.applet;

import static fajitaboy.constants.PanelConstants.BUTTONHEIGHT;
import static fajitaboy.constants.PanelConstants.BUTTONWIDTH;
import static fajitaboy.constants.PanelConstants.FB_SMALLFONT;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import fajitaboy.FajitaBoy;

/**
 * Captures keyinputs and updates keymap thingy.
 */
@SuppressWarnings("serial")
public class InputField extends JButton implements KeyListener {

    /** Name. */
    private String buttonText;
    /** FB. */
    private FajitaBoy fajitaBoy;

    /**
     * Constructor.
     * @param fb fajita
     * @param name keysettingsname
     */
    public InputField(final FajitaBoy fb, final String name) {
        super(name);
        
        fajitaBoy = fb;
        
        setFont(FB_SMALLFONT);
        setMargin(new Insets(0, 0, 0, 0));
        setPreferredSize(new Dimension(BUTTONWIDTH, BUTTONHEIGHT));
        setToolTipText("Click to change binding for " + name + " button.");

        buttonText = name;

        setOpaque(true);
        getInputMap().clear();

        setFocusable(true);
        MouseAdapter ma = new MouseAdapter() {
            public void mousePressed(final MouseEvent e) {
                requestFocus();
            }
        };
        addKeyListener(this);
        addMouseListener(ma);
    }

    /**
     * Refreshes the text on the button.
     */
    public final void refreshLabel() {
        KeyInputController controller = fajitaBoy.getKIC();
        setText(controller.getKey(buttonText));
    }

    /** {@inheritDoc} */
    public void keyPressed(final KeyEvent e) {
    }

    /** {@inheritDoc} */
    public final void keyReleased(final KeyEvent e) {
        KeyInputController controller = fajitaBoy.getKIC();
        if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
            controller.setKey(e.getKeyCode(), buttonText);
            fajitaBoy.refreshLabels();
        }
    }

    /** {@inheritDoc} */
    public void keyTyped(final KeyEvent e) {
    }
}
