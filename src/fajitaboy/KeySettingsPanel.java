package fajitaboy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class KeySettingsPanel extends JPanel implements ActionListener {

    /** Applet. */
    private FajitaBoy fajitaBoy;

    private InputField left, right, up, down;
    private InputField a, b, start, select;

    private JButton save, load, reset;

    private Dimension buttonSize;

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

        buttonSize = new Dimension(BUTTONWIDTH, BUTTONHEIGHT);

        JLabel middle = new JLabel();
        middle.setPreferredSize(buttonSize);
        middle.setOpaque(true);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        up = new InputField("Up");
        down = new InputField("Down");
        left = new InputField("Left");
        right = new InputField("Right");
        a = new InputField("A");
        b = new InputField("B");
        start = new InputField("Start");
        select = new InputField("Select");

        save = initButton("Store", "Save current keybindings. (Cookie)");
        load = initButton("Load", "Load saved keybindings.");
        reset = initButton("Reset", "Resets keys to default bindings.");

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
        joypadCons.setX(Spring.sum(Spring.constant(BUTTONWIDTH + 1), boxCons
                .getConstraint(SpringLayout.WEST)));
        joypadCons.setY(Spring.sum(Spring.constant(BUTTONHEIGHT
                + LAYOUT_PADDING), boxCons.getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints upCons = layout.getConstraints(up);
        upCons.setX(joypadCons.getConstraint(SpringLayout.WEST));
        upCons.setY(Spring.sum(Spring.constant(-BUTTONHEIGHT), joypadCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints leftCons = layout.getConstraints(left);
        leftCons.setY(joypadCons.getConstraint(SpringLayout.NORTH));
        leftCons.setX(Spring.sum(Spring.constant(-BUTTONWIDTH), joypadCons
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
        bCons.setX(Spring.sum(Spring.constant(LAYOUT_PADDING), rightCons
                .getConstraint(SpringLayout.EAST)));
        bCons.setY(Spring.sum(Spring.constant(LAYOUT_PADDING), joypadCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints aCons = layout.getConstraints(a);
        aCons.setX(bCons.getConstraint(SpringLayout.EAST));
        aCons.setY(Spring.sum(Spring.constant(-2 * LAYOUT_PADDING), joypadCons
                .getConstraint(SpringLayout.NORTH)));

        // --------------------------------------------------------------------
        // -- Start/Select button layout.

        SpringLayout.Constraints startCons = layout.getConstraints(start);
        startCons.setX(Spring.constant(2 * BUTTONWIDTH + 20));
        startCons.setY(Spring.sum(Spring.constant(-10), downCons
                .getConstraint(SpringLayout.SOUTH)));
        SpringLayout.Constraints selectCons = layout.getConstraints(select);
        selectCons.setX(startCons.getConstraint(SpringLayout.EAST));
        selectCons.setY(startCons.getConstraint(SpringLayout.NORTH));

        // --------------------------------------------------------------------
        // -- Save, load, reset buttons.

        SpringLayout.Constraints saveCons = layout.getConstraints(save);
        saveCons.setX(Spring.sum(Spring.constant(-185), boxCons
                .getConstraint(SpringLayout.EAST)));
        saveCons.setY(boxCons.getConstraint(SpringLayout.SOUTH));
        SpringLayout.Constraints loadCons = layout.getConstraints(load);
        loadCons.setX(saveCons.getConstraint(SpringLayout.EAST));
        loadCons.setY(saveCons.getConstraint(SpringLayout.NORTH));
        SpringLayout.Constraints resetCons = layout.getConstraints(reset);
        resetCons.setX(loadCons.getConstraint(SpringLayout.EAST));
        resetCons.setY(loadCons.getConstraint(SpringLayout.NORTH));

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
     * Creates and inits a button.
     * 
     * @param name
     *            Text on the button.
     * @param tooltip
     *            Mouseover text.
     * @return the button
     */
    private JButton initButton(final String name, final String tooltip) {
        JButton button = new JButton(name);
        button.setPreferredSize(buttonSize);
        button.setToolTipText(tooltip);
        button.addActionListener(this);
        button.setFont(FB_SMALLFONT);
        button.setMargin(new Insets(0, 0, 0, 0));
        add(button);
        return button;
    }

    /**
     * Captures keyinputs and updates keymap thingy.
     */
    private class InputField extends JButton implements KeyListener {

        /** Name. */
        private String buttonText;

        /**
         * Constructor.
         * 
         * @param text
         *            button name
         */
        public InputField(final String text) {
            super(text);
            setFont(FB_SMALLFONT);
            setMargin(new Insets(0, 0, 0, 0));
            setPreferredSize(buttonSize);
            setToolTipText("Click to change binding for " + text + " button.");

            buttonText = text;

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
                controller.setKey(e.getKeyCode(), buttonText);
                refreshLabels();
            }
        }

        /** {@inheritDoc} */
        public void keyTyped(final KeyEvent e) {
        }
    }

    /** {@inheritDoc} */
    public final void actionPerformed(final ActionEvent e) {
        KeyInputController controller = fajitaBoy.getKIC();
        if (e.getSource() == save) {
            controller.exportKeys();
        } else if (e.getSource() == load) {
            controller.importKeys();
        } else if (e.getSource() == reset) {
            controller.reset();
        }

        refreshLabels();

    }
}
