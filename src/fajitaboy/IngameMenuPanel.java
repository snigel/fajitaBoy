package fajitaboy;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import fajitaboy.gb.Oscillator;

/**
 * A tabbed ingame menu.
 */
@SuppressWarnings("serial")
public class IngameMenuPanel extends JTabbedPane {

    private final static String MAINLABEL = " Game  ";
    private final static String KEYSLABEL = " Keys  ";
    private final static String SOUNDLABEL = " Sound  ";

    /** Buttons. */
    private IngameMenuMain main;
    /** Config keys. */
    private KeySettingsPanel keys;
    /** Config sound. */
    private SoundSettingsPanel sound;

    /**
     * Default constructor.
     * 
     * @param fb
     *            applet
     */
    public IngameMenuPanel(final FajitaBoy fb) {

        setOpaque(true);

        setPreferredSize(new Dimension(215, 200));
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        main = new IngameMenuMain(fb);
        keys = new KeySettingsPanel(fb);
        sound = new SoundSettingsPanel();

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        addTab(MAINLABEL, main);
        addTab(KEYSLABEL, keys);
        addTab(SOUNDLABEL, sound);

        validate();
    }

    /** Refreshes labels on keybinding buttons. */
    public final void refreshLabels() {
        keys.refreshLabels();
    }

    /** Refreshes buttons and things in menu. */
    public final void refresh() {
        keys.refreshLabels();
        sound.refreshSlider();
    }

    /**
     * Sets sound panel oscillator for audiocontrol.
     * 
     * @param oscillator
     *            to fix sound
     */
    public final void setOscillator(Oscillator oscillator) {
        sound.setOscillator(oscillator);
    }

}
