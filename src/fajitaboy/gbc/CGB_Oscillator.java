package fajitaboy.gbc;

import static fajitaboy.constants.AudioConstants.AUDIO_SAMPLERATE;
import static fajitaboy.constants.AudioConstants.AUDIO_SAMPLES;

import javax.sound.sampled.LineUnavailableException;

import fajitaboy.VideoReciever;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.Timer;
import fajitaboy.gb.audio.SoundHandler;
import fajitaboy.gb.lcd.LCD;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gbc.lcd.CGB_LCD;
import fajitaboy.gbc.memory.CGB_AddressBus;

/**
 * Oscillator in CGB mode.
 */
public class CGB_Oscillator extends Oscillator {
    
    public CGB_Oscillator(Cpu cpu, CGB_AddressBus ram) {
    	this(cpu, ram, null);
    }
    
    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     */
    public CGB_Oscillator(Cpu cpu, CGB_AddressBus ram, VideoReciever videoReciever) {
    	this.cpu = cpu;
        this.ram = ram;
        this.videoReciever = videoReciever;
        
        lcd = new CGB_LCD(ram);
        timer = new Timer();
        try {
			soundHandler = new SoundHandler(ram, AUDIO_SAMPLERATE, AUDIO_SAMPLES);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			soundHandler = null;
			audioEnabled = false;
		}
        
        reset();
    }

    /**
     * {@inheritDoc}
     */
    public int step() {
        // Step CPU
        int cycleInc = cpu.step();
        if ( ((CGB_Cpu)cpu).speedSwitch.getSpeed() == 2) {
        	cycles += cycleInc >> 1;
        	timer.update(cycleInc, ram);
        	lcd.updateLCD(cycleInc >> 1);
        } else {
        	cycles += cycleInc;
        	timer.update(cycleInc, ram);
        	lcd.updateLCD(cycleInc);
        }

        // Update LCD
        if ( videoReciever != null && lcd.newScreenAvailable() ) {
            videoReciever.transmitVideo(lcd.getPixels());
        }

        return cycleInc;
    }
}
