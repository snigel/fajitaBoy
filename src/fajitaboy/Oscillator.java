package fajitaboy;

import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.AddressConstants.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import fajitaboy.audio.Audio;
import fajitaboy.audio.Audio2;
import fajitaboy.audio.Audio3;
import fajitaboy.audio.Audio4;
import fajitaboy.lcd.LCD;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;

/**
 * Oscillator emulates the clock frequency of the Game Boy and tells different
 * devices to do different things at different clock cycles.
 * @author Tobias S
 */
public class Oscillator implements Runnable{

    /**
     * Max number of frames to skip in a row.
     * TODO move this constant to a proper constant class.
     */
    private final int MAX_FRAMESKIP = 10;
    
    private boolean running;
    
    /**
     * Next cycle to stop and wait if going too fast
     */
    private long nextHaltCycle;
    
    /**
     * Cycles since Oscillator initialization.
     */
    private long cycles;

    /**
     * Previous cycle at which the Timer was incremented.
     */
    private long prevTimerInc;

    /**
     * Next cycle at which the Divider will be incremented.
     */
    private long nextDividerInc;

    /**
     * Pointer to CPU instance.
     */
    private Cpu cpu;

    /**
     * Pointer to MemoryInterface instance.
     */
    private MemoryInterface ram;

    /**
     * Pointer to LCD instance.
     */
    private LCD lcd;

    /**
     * Pointer to a class with the proper interface to draw the GB pixels to screen.
     */
    private DrawsGameboyScreen dgs; 
    
    /**
     * Audio is disabled by default.
     */
    private boolean audioEnabled;
    
    // Audio variables
    // TODO Move this functionality to a separate class
    private Audio au1;
    private Audio2 au2;
    private Audio3 au3;
    private Audio4 au4;
    private boolean ch1Left;
    private boolean ch1Right;
    private boolean ch2Left;
    private boolean ch2Right;
    private boolean ch3Left;
    private boolean ch3Right;
    private boolean ch4Left;
    private boolean ch4Right;
    private AudioFormat af;
    private SourceDataLine sdl;
    private int sampleRate = 44100;
    private int samples = 735;
    private int finalSamples;
    private byte[] destBuff;
    /**
     * Creates a new Oscillator with default values.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram) {
        this.cpu = cpu;
        this.ram = ram;
        lcd = new LCD(ram);
        reset();
    }

    /**
     * Creates a new Oscillator with a screen rendering instance and possibility to enable audio.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     * @param dgs
     * 			  Pointer to DrawsGameboyScreen instance.
     */
    public Oscillator(Cpu cpu, AddressBus ram, DrawsGameboyScreen dgs, boolean enableAudio ) {
        this(cpu,ram);
        this.dgs = dgs; 
        if ( enableAudio )
        	enableAudio();
    }
    
    /**
     * Resets the Oscillator to default values.
     */
    public void reset() {
        cycles = 0;
        prevTimerInc = 0;
        nextDividerInc = GB_DIV_CLOCK;
        nextHaltCycle = GB_CYCLES_PER_FRAME;
        running = false;
        disableAudio();
        resetAudio();
    }
    
    /**
     * Initializes the Audio module.
     */
    public void resetAudio() {
    	try {
        	af = new AudioFormat(sampleRate, 8, 2, true, false);
        	DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        	sdl = (SourceDataLine) AudioSystem.getLine(info);
        	sdl.open(af);
        	sdl.start();

        	au1 = new Audio((AddressBus)ram, sampleRate);
        	au2 = new Audio2((AddressBus)ram, sampleRate);
        	au3 = new Audio3((AddressBus)ram, sampleRate);
        	au4 = new Audio4((AddressBus)ram, sampleRate);
        } catch ( LineUnavailableException e ) {
        	System.out.println("Error when initializing audio: " + e);
        	audioEnabled = false;
        }
    }
    
    /**
     * Some audio function...
     */
    private void stereoSelect() {
        int nr51 = ram.read(NR51_REGISTER);
        ch1Left = ((nr51 & 0x1) > 0);
        ch2Left = ((nr51 & 0x2) > 0);
        ch3Left = ((nr51 & 0x4) > 0);
        ch4Left = ((nr51 & 0x8) > 0);
        ch1Right = ((nr51 & 0x10) > 0);
        ch2Right = ((nr51 & 0x20)  > 0);
        ch3Right = ((nr51 & 0x40) > 0);
        ch4Right = ((nr51 & 0x80) > 0);
    }

    /**
     * Step the Oscillator once. The Oscillator causes the CPU to step once, and
     * sends messages to other components at certain cycles.
     * @return Returns step time in cycles.
     */
    public int step() {
        // Step CPU
        int cycleInc = cpu.step();
        cycles += cycleInc;
        
        updateDiv();
        updateTimer();
        
        // Update LCD
        lcd.updateLCD(cycleInc);
        if ( dgs != null && lcd.newScreenAvailable() ) {
        	dgs.drawGameboyScreen(lcd.getScreen());
        }
        
        return cycleInc;
    }

    /**
     * Increment Divider register.
     */
    private void updateDiv() {
    	if ( cycles >= nextDividerInc ) {
    		ram.forceWrite( ADDRESS_DIV, (ram.read(ADDRESS_DIV) + 1) & 0xFF );
    		nextDividerInc += GB_DIV_CLOCK;
    	}
    }
    
    /**
     * Increment Timer.
     */
    private void updateTimer() {
        int tac = ram.read(ADDRESS_TAC);
        int timerFreq = 0;
        switch (tac & 0x03) {
        case 0:
            timerFreq = GB_TIMER_CLOCK_0;
            break;
        case 1:
            timerFreq = GB_TIMER_CLOCK_1;
            break;
        case 2:
            timerFreq = GB_TIMER_CLOCK_2;
            break;
        case 3:
            timerFreq = GB_TIMER_CLOCK_3;
            break;
        }
        
        while (cycles >= prevTimerInc + timerFreq) {
            if ((tac & 0x04) != 0) {
                int tima = ram.read(ADDRESS_TIMA);
                if (tima == 0xFF) {
                    ram.write(ADDRESS_TIMA, ram.read(ADDRESS_TMA));
                    ram.write(ADDRESS_IF, ram.read(ADDRESS_IF) | 0x04);
                } else {
                    ram.write(ADDRESS_TIMA, tima + 1);
                }
            }
            prevTimerInc += timerFreq;
        }

    }
    
    /**
     * Generates one frame of audio.
     */
    private void generateAudio() {
    	if(sdl.available()*2 < samples*2) {
            destBuff = new byte[sdl.available()*2];
            finalSamples = sdl.available();
        }
        else {
            destBuff = new byte[samples*2];
            finalSamples = samples;
        }
        stereoSelect();
        au1.generateTone(destBuff, ch1Left, ch1Right, finalSamples);
        au2.generateTone(destBuff, ch2Left, ch2Right, finalSamples);
        au3.generateTone(destBuff, ch3Left, ch3Right, finalSamples);
        au4.generateTone(destBuff, ch4Left, ch4Right, finalSamples);
        sdl.write(destBuff, 0, destBuff.length);
    }

    public LCD getLCD() {
        return lcd;
    }

    /**
     * Starts the execution. Create a new thread and call start() to
     * make this method run in a separate thread.
     */
    public final void run() {
        running = true;
        long sleepTime;
        long nextUpdate = System.nanoTime();
        int frameSkipCount = 0;
        while (running) {
            step();

            if (cycles > nextHaltCycle) {
            	generateAudio();
                nextUpdate += GB_NANOS_PER_FRAME;
                sleepTime = nextUpdate - System.nanoTime();
                
                // Sleep if running too fast
                if (sleepTime >= 0) {
                    lcd.disableFrameSkip();
                    frameSkipCount = 0;
                    try {
                        Thread.sleep(sleepTime / 1000000);
                    } catch (InterruptedException e) {}
                } else if (frameSkipCount >= MAX_FRAMESKIP) {
                	lcd.disableFrameSkip();
                	frameSkipCount = 0;
                	nextUpdate = System.nanoTime();
                } else {
                	lcd.enableFrameSkip();
                	frameSkipCount++;
                }
                nextHaltCycle += GB_CYCLES_PER_FRAME;
            }
        }
    }

    /**
     * Method to call to stop the run() loop.
     */
    public final void stop() {
        running = false;
    }

    /**
     * To know if run() is running.
     * @return true if the Oscillator is running, otherwise false.
     */
    public final boolean isRunning() {
        return running;
    }
    
    public void enableAudio() {
    	if ( audioEnabled == false ) {
    		audioEnabled = true;
    		resetAudio();
    	}
    }
    
    public void disableAudio() {
    	audioEnabled = false;
    }

}
