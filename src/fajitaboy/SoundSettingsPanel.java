package fajitaboy;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static fajitaboy.constants.AudioConstants.*;

/**
 * Volume control etc.
 */
@SuppressWarnings("serial")
public class SoundSettingsPanel extends JPanel implements ChangeListener {

    /** Oscillator. */
    private Oscillator oscillator;

    /** Volume. */
    private JSlider volume;

    /**
     * Constructor.
     */
    public SoundSettingsPanel() {

        volume = new JSlider(JSlider.VERTICAL, VOLUME_MIN, VOLUME_MAX,
                VOLUME_INIT);
        add(volume);

        volume.setPreferredSize(new Dimension(100, 150));
        volume.setMajorTickSpacing(50);
        volume.setMinorTickSpacing(10);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);

        Hashtable<Integer, JLabel> labelTable;
        labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(0), new JLabel("Mute"));
        labelTable.put(new Integer(VOLUME_MAX), new JLabel("Max"));
        volume.setLabelTable(labelTable);

        volume.addChangeListener(this);

        validate();
    }

    /**
     * Set oscillator for sound control.
     * 
     * @param osc
     *            oscillator
     */
    public final void setOscillator(Oscillator osc) {
        oscillator = osc;
    }

    /** {@inheritDoc} */
    public final void stateChanged(final ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            int vol = (int) source.getValue();
            if (vol == 0) {
                oscillator.disableAudio();
            } else {
                oscillator.enableAudio();
                // oscillator.setVolume(vol);
            }
        }
    }

    /** Refresh slider position. */
    public final void refreshSlider() {

        if (!oscillator.isAudioEnabled()) {
            volume.setValue(0);
        } else {
            volume.setValue(VOLUME_MAX);
            // int vol = oscillator.getVolume();
            // vol = Math.max(0, vol);
            // vol = Math.min(100, vol);
            // volume.setValue(vol);
        }
    }
}
