package fajitaboy.gb.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fajitaboy.AudioReciever;

public class SoundReciever implements AudioReciever {

	/**
	 * The audio format that is used with SourceDataLine.
	 */
	private AudioFormat af;

	/**
	 * The line to the sound card.
	 */
	private SourceDataLine sdl;
	
	double volume;

	/**
	 * Sets up the line to the sound card and creates the fours sound channels.
	 *
	 * @param sampleRate
	 *            The sample rate that the sound should be sampled.
	 * @throws LineUnavailableException
	 */
	public SoundReciever(int sampleRate) throws LineUnavailableException {

		volume = 1;
		af = new AudioFormat(sampleRate, 8, 2, true, false);
		enableAudio();
		
	}

	/**
	 * Generates and outputs a clip of sound.
	 */
	public final void transmitAudio(byte[] data) {
		
		if ( volume <= 0 || sdl == null )
			return;
		
		// Check if the available space in data is less then
        // the number of samples.
		int bytes;
		int available = sdl.available(); 
		if ( available < 0 )
			return;
		
        if ( available*2 < data.length) {
            bytes = available*2;
        } else {
            bytes = data.length;
        }
        
        if ( volume < 1 ) {
        	for ( int i = 0; i < bytes; i++ ) {
        		data[i] = (byte)((double)data[i] * volume);
        	}
        }
		sdl.write(data, 0, bytes);
	}

	/**
	 * Closes the line to the sound card.
	 */
	public final void close() {
		sdl.close();
	}

    public void enableAudio() {
        if ( sdl == null ) {
    		try {
    			DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
    			sdl = (SourceDataLine) AudioSystem.getLine(info);
				sdl.open(af);
	    		sdl.start();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    public void disableAudio() {
        if ( sdl != null ) {
        	sdl.drain();
        	sdl.stop();
        	sdl.close();
        }
    }
    
    public boolean isAudioEnabled() {
    	return (sdl != null);
    }

    /**
     * Sets emulator volume.
     *
     * @param volume
     */
    public final void setVolume(int vol) {
        vol = Math.max(0, vol);
        vol = Math.min(100, vol);
        volume = vol;
    }

    /**
     * Returns emulator volume.
     * @return int
     */
    public final int getVolume() {
        return (int)(volume * 100);
    }
    
}
