package fajitaboy.applet;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fajitaboy.Emulator;
import fajitaboy.FajitaBoy;

import static fajitaboy.constants.AudioConstants.*;
import static fajitaboy.constants.PanelConstants.*;

/**
 * Volume control etc.
 */
@SuppressWarnings("serial")
public class SoundSettingsPanel extends JPanel implements ChangeListener {

    /** Oscillator. */
    private Emulator emulator;

    /** Cookies. */
    private CookieJar cookieJar;

    /** Volume slider. */
    private JSlider volumeSlider;

    /** Boooool. */
    private boolean ignoreUpdate;

    /**
     * Constructor.
     * 
     * @param fb for Cookies
     */
    public SoundSettingsPanel(final FajitaBoy fb) {

        emulator = null;
        ignoreUpdate = false;

        volumeSlider = new JSlider(JSlider.VERTICAL, AUDIO_VOLUME_MIN,
                AUDIO_VOLUME_MAX, AUDIO_VOLUME_MAX);

        cookieJar = fb.getCookieJar();

        volumeSlider.setPreferredSize(new Dimension(100, 150));
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.addChangeListener(this);

        Hashtable<Integer, JLabel> labelTable;
        labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(AUDIO_VOLUME_MIN), new JLabel("Mute"));
        labelTable.put(new Integer(AUDIO_VOLUME_MAX), new JLabel("Max"));
        volumeSlider.setLabelTable(labelTable);

        add(volumeSlider);
        validate();
    }

    /**
     * Set oscillator for sound control.
     * 
     * @param osc oscillator
     */
    public final void setEmulator(Emulator emulator) {
        this.emulator = emulator;
        fetchCookie();
    }

    /** Refresh slider position. */
    public final void refreshSlider() {
        if (emulator == null) {
            return;
        }

        ignoreUpdate = true;
        if (!emulator.isAudioEnabled()) {
            volumeSlider.setValue(0);
        } else {
            volumeSlider.setValue((int) emulator.getVolume());
        }
    }

    /**
     * Saves volume as cookie.
     */
    private void putCookie() {
        String cookie = String.valueOf((int) emulator.getVolume());
        cookieJar.put(COOKIE_SOUND, cookie);
    }

    /**
     * Attempts to get a cookie from the cookieJar.
     */
    public final void fetchCookie() {
        try {
            String cookie = cookieJar.get(COOKIE_SOUND);
            if (cookie == null) {
                return;
            }
            int savedVolume = Integer.parseInt(cookie);
            if (savedVolume >= AUDIO_VOLUME_MIN
                    && savedVolume <= AUDIO_VOLUME_MAX) {
                setVolume(savedVolume);
                volumeSlider.setValue(savedVolume);
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO remove.

            // Number format exception if volume isn't valid, or out
            // of bounds exception if cookie read failed. Either way,
            // don't care, do nothing.
            return;
        }
    }

    /**
     * Updates the volume from the slider.
     * 
     * @param vol volume
     */
    private void setVolume(final int vol) {
        if (emulator == null) {
            return;
        }
        if (vol == 0) {
            emulator.disableAudio();
        } else {
            emulator.enableAudio();
        }
        emulator.setVolume(vol);
    }

    /** {@inheritDoc} */
    public final void stateChanged(final ChangeEvent e) {
        if (ignoreUpdate) {
            ignoreUpdate = false;
            return;
        }
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            int volume = source.getValue();
            setVolume(volume);
            putCookie();
        }
    }
}
