package fajitaboy.applet;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import fajitaboy.FajitaBoy;

import static fajitaboy.constants.PanelConstants.*;

/**
 * Key change stuff.
 */
@SuppressWarnings("serial")
public class KeySettingsPanel extends JPanel implements ActionListener,
        ItemListener {

    /** Applet. */
    private FajitaBoy fajitaBoy;

    private JButton save, load, reset;

    private Dimension buttonSize;

    private InputField mute;
    private InputField pause;

    private JPanel cards;
    private JoypadSettings card1;
    private JoypadSettings card2;
    private JPanel card3;

    final static String PLAYER1PANEL = "Player1 keys";
    final static String PLAYER2PANEL = "Player2 keys";
    final static String SYSTEMPANEL = "System keys";

    /**
     * Constructor.
     * 
     * @param fb applet
     */
    public KeySettingsPanel(final FajitaBoy fb) {
        fajitaBoy = fb;

        setOpaque(true);
        setPreferredSize(new Dimension(270, 170));

        buttonSize = new Dimension(BUTTONWIDTH, BUTTONHEIGHT);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        SpringLayout.Constraints boxCons = layout.getConstraints(this);

        save = initButton("Store", "Save current keybindings. (Cookie)");
        load = initButton("Load", "Load saved keybindings.");
        reset = initButton("Reset", "Resets keys to default bindings.");

        String[] comboBoxItems = { PLAYER1PANEL, PLAYER2PANEL, SYSTEMPANEL };
        JComboBox cb = new JComboBox(comboBoxItems);
        cb.setFont(FB_SMALLFONT);
        cb.setEditable(false);
        cb.addItemListener(this);
        add(cb);

        card1 = new JoypadSettings(fb, "P1");
        card2 = new JoypadSettings(fb, "P2");
        card3 = initSysKey();

        cards = new JPanel(new CardLayout());
        cards.add(card1, PLAYER1PANEL);
        cards.add(card2, PLAYER2PANEL);
        cards.add(card3, SYSTEMPANEL);

        add(cards);

        SpringLayout.Constraints saveCons = layout.getConstraints(save);
        saveCons.setX(boxCons.getConstraint(SpringLayout.WEST));
        saveCons.setY(boxCons.getConstraint(SpringLayout.NORTH));
        SpringLayout.Constraints loadCons = layout.getConstraints(load);
        loadCons.setX(saveCons.getConstraint(SpringLayout.EAST));
        loadCons.setY(saveCons.getConstraint(SpringLayout.NORTH));
        SpringLayout.Constraints resetCons = layout.getConstraints(reset);
        resetCons.setX(loadCons.getConstraint(SpringLayout.EAST));
        resetCons.setY(loadCons.getConstraint(SpringLayout.NORTH));
        SpringLayout.Constraints comboCons = layout.getConstraints(cb);
        comboCons.setX(Spring.sum(Spring.constant(5), resetCons
                .getConstraint(SpringLayout.EAST)));
        comboCons.setY(Spring.sum(Spring.constant(5), resetCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints cardsCons = layout.getConstraints(cards);
        cardsCons.setX(boxCons.getConstraint(SpringLayout.WEST));
        cardsCons.setY(Spring.sum(Spring.constant(10), saveCons
                .getConstraint(SpringLayout.SOUTH)));

        validate();
    }

    /**
     * Creates and inits the System Key Settings panel.
     * 
     * @return panel
     */
    private JPanel initSysKey() {

        JPanel panel = new JPanel();

        pause = new InputField(fajitaBoy, "Pause");
        mute = new InputField(fajitaBoy, "Mute");
        JLabel pauseT = new JLabel("Pause");
        JLabel muteT = new JLabel("Mute");

        SpringLayout skl = new SpringLayout();
        panel.setLayout(skl);
        SpringLayout.Constraints skCons = skl.getConstraints(panel);

        panel.add(pauseT);
        panel.add(pause);
        panel.add(muteT);
        panel.add(mute);

        SpringLayout.Constraints ptCons = skl.getConstraints(pauseT);
        ptCons.setX(Spring.sum(Spring.constant(50), skCons
                .getConstraint(SpringLayout.WEST)));
        ptCons.setY(Spring.sum(Spring.constant(10), skCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints piCons = skl.getConstraints(pause);
        piCons.setX(Spring.sum(Spring.constant(10), ptCons
                .getConstraint(SpringLayout.EAST)));
        piCons.setY(Spring.sum(Spring.constant(-3), ptCons
                .getConstraint(SpringLayout.NORTH)));
        SpringLayout.Constraints mtCons = skl.getConstraints(muteT);
        mtCons.setX(ptCons.getConstraint(SpringLayout.WEST));
        mtCons.setY(Spring.sum(Spring.constant(15), ptCons
                .getConstraint(SpringLayout.SOUTH)));
        SpringLayout.Constraints miCons = skl.getConstraints(mute);
        miCons.setX(piCons.getConstraint(SpringLayout.WEST));
        miCons.setY(Spring.sum(Spring.constant(-3), mtCons
                .getConstraint(SpringLayout.NORTH)));
        panel.validate();

        return panel;
    }

    /**
     * Creates and inits a button.
     * 
     * @param name Text on the button.
     * @param tooltip Mouseover text.
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

    public void refreshLabels() {
        card1.refreshLabels();
        card2.refreshLabels();
        pause.refreshLabel();
        mute.refreshLabel();
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

    /** {@inheritDoc} */
    public void itemStateChanged(final ItemEvent e) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, (String) e.getItem());
    }

    private class JoypadSettings extends JPanel {

        private InputField left, right, up, down;
        private InputField a, b, start, select;

        public JoypadSettings(final FajitaBoy fb, final String player) {

            setPreferredSize(new Dimension(270, 190));
            JLabel middle = new JLabel();
            middle.setPreferredSize(buttonSize);
            middle.setOpaque(true);

            up = new InputField(fb, "Up" + player);
            down = new InputField(fb, "Down" + player);
            left = new InputField(fb, "Left" + player);
            right = new InputField(fb, "Right" + player);
            a = new InputField(fb, "A" + player);
            b = new InputField(fb, "B" + player);
            start = new InputField(fb, "Start" + player);
            select = new InputField(fb, "Select" + player);

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

            SpringLayout layout = new SpringLayout();
            setLayout(layout);

            SpringLayout.Constraints boxCons = layout.getConstraints(this);

            // ----------------------------------------------------------------
            // -- Joypad layout stuff, based on the center box called middle.

            SpringLayout.Constraints joypadCons = layout.getConstraints(middle);
            joypadCons.setX(Spring.sum(Spring.constant(BUTTONWIDTH + 1),
                    boxCons.getConstraint(SpringLayout.WEST)));
            joypadCons.setY(Spring.sum(Spring.constant(BUTTONHEIGHT
                    + LAYOUT_PADDING), boxCons
                    .getConstraint(SpringLayout.NORTH)));
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

            // ----------------------------------------------------------------
            // -- A and B button layout. Based on the joypad layout

            SpringLayout.Constraints bCons = layout.getConstraints(b);
            bCons.setX(Spring.sum(Spring.constant(LAYOUT_PADDING), rightCons
                    .getConstraint(SpringLayout.EAST)));
            bCons.setY(Spring.sum(Spring.constant(LAYOUT_PADDING), joypadCons
                    .getConstraint(SpringLayout.NORTH)));
            SpringLayout.Constraints aCons = layout.getConstraints(a);
            aCons.setX(bCons.getConstraint(SpringLayout.EAST));
            aCons.setY(Spring.sum(Spring.constant(-2 * LAYOUT_PADDING),
                    joypadCons.getConstraint(SpringLayout.NORTH)));

            // ----------------------------------------------------------------
            // -- Start/Select button layout.

            SpringLayout.Constraints startCons = layout.getConstraints(start);
            startCons.setX(Spring.constant(2 * BUTTONWIDTH + 20));
            startCons.setY(Spring.sum(Spring.constant(-10), downCons
                    .getConstraint(SpringLayout.SOUTH)));
            SpringLayout.Constraints selectCons = layout.getConstraints(select);
            selectCons.setX(startCons.getConstraint(SpringLayout.EAST));
            selectCons.setY(startCons.getConstraint(SpringLayout.NORTH));

        }

        /**
         * Refreshes the labels on keysettings buttons.
         */
        public final void refreshLabels() {
            left.refreshLabel();
            right.refreshLabel();
            up.refreshLabel();
            down.refreshLabel();
            a.refreshLabel();
            b.refreshLabel();
            start.refreshLabel();
            select.refreshLabel();
        }
    }
}
