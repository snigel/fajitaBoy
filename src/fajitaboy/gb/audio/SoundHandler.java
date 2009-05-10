package fajitaboy.gb.audio;

import static fajitaboy.constants.AddressConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fajitaboy.FileIOStreamHelper;
import fajitaboy.gb.StateMachine;
import fajitaboy.gb.memory.AddressBus;

/**
 * This is the main class for sound generation. It opens a sourcedataline that
 * can interact with the sound card.
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundHandler implements StateMachine {

    /**
     * The address bus.
     */
    private AddressBus ab;

    /**
     * Game Boys first sound channel that generates a squarewave.
     */
    private SoundChannel1 au1;

    /**
     * Game Boys second sound channel that generates a squarewave.
     */
    private SoundChannel2 au2;

    /**
     * Game Boys third sound channel that generates a waveoutput.
     */
    private SoundChannel3 au3;

    /**
     * Game Boys fourth sound channnel that generates whitenoise.
     */
    private SoundChannel4 au4;

    /**
     * Flag that indicate if channel 1 left output is on or off.
     */
    private boolean ch1Left;

    /**
     * Flag that indicate if channel 1 right output is on or off.
     */
    private boolean ch1Right;

    /**
     * Flag that indicate if channel 2 left output is on or off.
     */
    private boolean ch2Left;

    /**
     * Flag that indicate if channel 2 right output is on or off.
     */
    private boolean ch2Right;

    /**
     * Flag that indicate if channel 3 left output is on or off.
     */
    private boolean ch3Left;

    /**
     * Flag that indicate if channel 3 right output is on or off.
     */
    private boolean ch3Right;

    /**
     * Flag that indicate if channel 4 left output is on or off.
     */
    private boolean ch4Left;

    /**
     * Flag that indicate if channel 4 right output is on or off.
     */
    private boolean ch4Right;

    /**
     * The number of samples that should be generated per v-blank.
     */
    private int samples;

    /**
     * The final number of samples that will be generated on then availability
     * in the SourceDataLines buffer is taken in account.
     */
    private int finalSamples;

    /**
     * The array that holds the generated sound.
     */
    private byte[] destBuff;

    /**
     * The audio format that is used with SourceDataLine.
     */
    private AudioFormat af;

    /**
     * The line to the sound card.
     */
    private SourceDataLine sdl;

    /**
     * Sets up the line to the sound card and creates the fours sound channels.
     *
     * @param ab
     *            The address bus.
     * @param sampleRate
     *            The sample rate that the sound should be sampled.
     * @param samples
     *            The number of samples per v-blank.
     * @throws LineUnavailableException
     */
    public SoundHandler(final AddressBus ab, final float sampleRate,
            final int samples) throws LineUnavailableException {

        af = new AudioFormat(sampleRate, 8, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
        sdl.open(af);
        sdl.start();

        this.ab = ab;
        this.samples = samples;
        au1 = new SoundChannel1(ab, sampleRate);
        au2 = new SoundChannel2(ab, sampleRate);
        au3 = new SoundChannel3(ab, sampleRate);
        au4 = new SoundChannel4(ab, sampleRate);
    }
    
    public void reset() {
    	ch1Left = true;
        ch2Left = true;
        ch3Left = true;
        ch4Left = true;
        ch1Right = true;
        ch2Right = true;
        ch3Right = true;
        ch4Right = true;
    }

    /**
     * Generates and outputs a clip of sound.
     */
    public final void generateTone() {
        // Check if the available space in the buffer is less then
        // the number of samples.
        if (sdl.available() < samples) {
            destBuff = new byte[sdl.available() * 2];
            finalSamples = sdl.available();
        } else {
            destBuff = new byte[samples * 2];
            finalSamples = samples;
        }
        stereoSelect();
        au1.generateTone(destBuff, ch1Left, ch1Right, finalSamples);
        au2.generateTone(destBuff, ch2Left, ch2Right, finalSamples);
        au3.generateTone(destBuff, ch3Left, ch3Right, finalSamples);
        au4.generateTone(destBuff, ch4Left, ch4Right, finalSamples);

        sdl.write(destBuff, 0, destBuff.length);
    }

    /**
     * Sets up the flag that controls the stereo outputs.
     */
    private void stereoSelect() {
        int nr51 = ab.read(ADDRESS_NR51);
        ch1Left = ((nr51 & 0x1) > 0);
        ch2Left = ((nr51 & 0x2) > 0);
        ch3Left = ((nr51 & 0x4) > 0);
        ch4Left = ((nr51 & 0x8) > 0);
        ch1Right = ((nr51 & 0x10) > 0);
        ch2Right = ((nr51 & 0x20) > 0);
        ch3Right = ((nr51 & 0x40) > 0);
        ch4Right = ((nr51 & 0x80) > 0);
    }

    /**
     * Closes the line to the sound card.
     */
    public final void close() {
        sdl.close();
    }

    public final void setVolume(int volume) {
        double vol = ((double) volume) / 100;
        au1.setVolume(vol);
        au2.setVolume(vol);
        au3.setVolume(vol);
        au4.setVolume(vol);

    }

    /**
	 * {@inheritDoc}
	 */
	public void readState(FileInputStream is) throws IOException {
		samples = (int) FileIOStreamHelper.readData(is, 4);
		ch1Left = FileIOStreamHelper.readBoolean(is);
		ch1Right = FileIOStreamHelper.readBoolean(is);
		ch2Left = FileIOStreamHelper.readBoolean(is);
		ch2Right = FileIOStreamHelper.readBoolean(is);
		ch3Left = FileIOStreamHelper.readBoolean(is);
		ch3Right = FileIOStreamHelper.readBoolean(is);
		ch4Left = FileIOStreamHelper.readBoolean(is);
		ch4Right = FileIOStreamHelper.readBoolean(is);

		au1.readState(is);
		au2.readState(is);
		au3.readState(is);
		au4.readState(is);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveState(FileOutputStream os) throws IOException {
		FileIOStreamHelper.writeData(os, (long) samples, 4);
		FileIOStreamHelper.writeBoolean(os, ch1Left);
		FileIOStreamHelper.writeBoolean(os, ch1Right);
		FileIOStreamHelper.writeBoolean(os, ch2Left);
		FileIOStreamHelper.writeBoolean(os, ch2Right);
		FileIOStreamHelper.writeBoolean(os, ch3Left);
		FileIOStreamHelper.writeBoolean(os, ch3Right);
		FileIOStreamHelper.writeBoolean(os, ch4Left);
		FileIOStreamHelper.writeBoolean(os, ch4Right);

		au1.saveState(os);
		au2.saveState(os);
		au3.saveState(os);
		au4.saveState(os);
	}
}
