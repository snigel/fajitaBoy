package fajitaboy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;

import fajitaboy.FajitaBoy.GameState;

/**
 * Key change stuff.
 */
@SuppressWarnings("serial")
public class KeySettingsPanel extends JPanel {

    /** Applet. */
    private FajitaBoy fajitaBoy;

    private JPanel joypad;
    private JPanel middle;
    private JPanel buttons;

    private InputField left, right, up, down;
    private InputField a, b, start, select;

    /**
     * Constructor.
     * 
     * @param fb
     *            applet
     */
    public KeySettingsPanel(final FajitaBoy fb) {
        fajitaBoy = fb;

        setOpaque(true);
        setPreferredSize(new Dimension(270, 130));
        setLayout(new BorderLayout());

        joypad = new JPanel();
        middle = new JPanel();
        buttons = new JPanel();

        up = new InputField("Up");
        down = new InputField("Down");
        left = new InputField("Left");
        right = new InputField("Right");
        a = new InputField("A");
        b = new InputField("B");
        start = new InputField("Start");
        select = new InputField("Select");

        joypad.setPreferredSize(new Dimension(80, 80));
        joypad.setLayout(new BorderLayout());
        joypad.add(up, BorderLayout.NORTH);
        joypad.add(left, BorderLayout.WEST);
        joypad.add(right, BorderLayout.EAST);
        joypad.add(down, BorderLayout.SOUTH);

        middle.setPreferredSize(new Dimension(80, 40));
        middle.setLayout(new GridLayout(1, 2));
        middle.add(start);
        middle.add(select);

        buttons.setPreferredSize(new Dimension(80, 40));
        buttons.setLayout(new GridLayout(1, 2));
        buttons.add(a);
        buttons.add(b);

        add(joypad, BorderLayout.WEST);
        add(middle, BorderLayout.SOUTH);
        add(buttons, BorderLayout.EAST);

        validate();
    }

    /**
     * Refreshes the labels on keysettings buttons.
     */
    public final void refreshLabels() {
        left.setText(getT("Left"));
        right.setText(getT("Right"));
        up.setText(getT("Up"));
        down.setText(getT("Down"));
        a.setText(getT("A"));
        b.setText(getT("B"));
        start.setText(getT("Start"));
        select.setText(getT("Select"));
    }

    /**
     * Get the keyboard key bound to a fajitabutton.
     * 
     * @param button
     *            fajitabutton
     * @return keyboard key
     */
    private String getT(final String button) {
        KeyInputController controller = fajitaBoy.getKIC();
        return controller.getKey(button);
    }

    /**
     * Captures keyinputs and updates keymap thingy.
     */
    private class InputField extends JButton implements KeyListener {

        /** name. */
        String button;

        /**
         * Constructor.
         * 
         * @param btn
         *            button name
         */
        public InputField(final String btn) {
            super(btn);
            setFont(new Font("Verdana", Font.PLAIN, 10));
            setPreferredSize(new Dimension(30, 20));
            button = btn;

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
         * @inheritDoc
         * @param e
         *            keyevent
         */
        public void keyPressed(final KeyEvent e) {
        }

        /**
         * @inheritDoc
         * @param e
         *            keyevent
         */
        public void keyReleased(final KeyEvent e) {
            KeyInputController controller = fajitaBoy.getKIC();
            if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
                controller.setKey(e.getKeyCode(), button);
                System.out.println("Set:"+button+" to "+e.getKeyCode());
                refreshLabels();
            } else {
                fajitaBoy.changeGameState(GameState.PLAYGAME);
            }
        }

        /**
         * @inheritDoc
         * @param e
         *            keyevent
         */
        public void keyTyped(final KeyEvent e) {
        }

    }

}
