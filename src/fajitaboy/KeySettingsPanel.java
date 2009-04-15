package fajitaboy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import static fajitaboy.constants.PanelConstants.*;

/**
 * Key change stuff.
 */
@SuppressWarnings("serial")
public class KeySettingsPanel extends JPanel {

    /** Applet. */
    private FajitaBoy fajitaBoy;

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

        JLabel middle = new JLabel();
        middle.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        middle.setOpaque(true);

        up = new InputField("Up");
        down = new InputField("Down");
        left = new InputField("Left");
        right = new InputField("Right");
        a = new InputField("A");
        b = new InputField("B");
        start = new InputField("Start");
        select = new InputField("Select");

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        add(up);
        add(left);
        add(middle);
        add(right);
        add(down);
        add(a);
        add(b);
        add(start);
        add(select);

        Color joypadColor = new Color(150, 160, 160);
        up.setBackground(joypadColor);
        down.setBackground(joypadColor);
        left.setBackground(joypadColor);
        right.setBackground(joypadColor);
        middle.setBackground(joypadColor);

        Color buttonColor = new Color(200, 80, 160);
        a.setBackground(buttonColor);
        b.setBackground(buttonColor);

        Color miscColor = new Color(190, 200, 200);
        start.setBackground(miscColor);
        select.setBackground(miscColor);

        SpringLayout.Constraints boxCons = layout.getConstraints(this);

        // --------------------------------------------------------------------
        // -- Joypad layout stuff, based on the center box called middle.

        SpringLayout.Constraints joypadCons = layout.getConstraints(middle);
        joypadCons.setX(Spring.sum(Spring.constant(buttonWidth + 1), boxCons
                .getConstraint(SpringLayout.WEST)));
        joypadCons.setY(Spring.sum(Spring.constant(buttonHeight + padding),
                boxCons.getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints upCons = layout.getConstraints(up);
        upCons.setX(joypadCons.getConstraint(SpringLayout.WEST));
        upCons.setY(Spring.sum(Spring.constant(-buttonHeight), joypadCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints leftCons = layout.getConstraints(left);
        leftCons.setY(joypadCons.getConstraint(SpringLayout.NORTH));
        leftCons.setX(Spring.sum(Spring.constant(-buttonWidth), joypadCons
                .getConstraint(SpringLayout.WEST)));
        SpringLayout.Constraints rightCons = layout.getConstraints(right);
        rightCons.setY(joypadCons.getConstraint(SpringLayout.NORTH));
        rightCons.setX(joypadCons.getConstraint(SpringLayout.EAST));
        SpringLayout.Constraints downCons = layout.getConstraints(down);
        downCons.setX(joypadCons.getConstraint(SpringLayout.WEST));
        downCons.setY(joypadCons.getConstraint(SpringLayout.SOUTH));

        // --------------------------------------------------------------------
        // -- A and B button layout. Based on the joypad layout

        SpringLayout.Constraints bCons = layout.getConstraints(b);
        bCons.setX(Spring.sum(Spring.constant(padding), rightCons
                .getConstraint(SpringLayout.EAST)));
        bCons.setY(Spring.sum(Spring.constant(padding), joypadCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints aCons = layout.getConstraints(a);
        aCons.setX(bCons.getConstraint(SpringLayout.EAST));
        aCons.setY(Spring.sum(Spring.constant(-2 * padding), joypadCons
                .getConstraint(SpringLayout.NORTH)));

        // --------------------------------------------------------------------
        // -- Start/Select button layout.

        SpringLayout.Constraints startCons = layout.getConstraints(start);
        startCons.setX(Spring.constant(2 * buttonWidth));
        startCons.setY(Spring.sum(Spring.constant(2 * padding), downCons
                .getConstraint(SpringLayout.SOUTH)));
        SpringLayout.Constraints selectCons = layout.getConstraints(select);
        selectCons.setX(startCons.getConstraint(SpringLayout.EAST));
        selectCons.setY(startCons.getConstraint(SpringLayout.NORTH));

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

        /** Name. */
        private String button;

        /**
         * Constructor.
         * 
         * @param btn
         *            button name
         */
        public InputField(final String btn) {
            super(btn);
            setFont(new Font("Terminal", Font.PLAIN, 9));
            setMargin(new Insets(0, 0, 0, 0));
            setPreferredSize(new Dimension(buttonWidth, buttonHeight));
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

        /** {@inheritDoc} */
        public void keyPressed(final KeyEvent e) {
        }

        /** {@inheritDoc} */
        public void keyReleased(final KeyEvent e) {
            KeyInputController controller = fajitaBoy.getKIC();
            if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
                controller.setKey(e.getKeyCode(), button);
                System.out.println("Set:" + button + " to " + e.getKeyCode());
                refreshLabels();
            }
        }

        /** {@inheritDoc} */
        public void keyTyped(final KeyEvent e) {
        }
    }
}
