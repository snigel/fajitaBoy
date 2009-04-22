package fajitaboy;

import static fajitaboy.constants.HardwareConstants.*;
import static fajitaboy.constants.EmulationConstants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import fajitaboy.audio.SoundHandler;
import fajitaboy.lcd.LCD;
import fajitaboy.memory.AddressBus;
import fajitaboy.memory.MemoryInterface;

/**
 * Oscillator emulates the clock frequency of the Game Boy and tells different
 * devices to do different things at different clock cycles.
 * @author Tobias S
 */
public class Oscillator implements Runnable, StateMachine {

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
     * Pointer to Timer instance.
     */
    private Timer timer;

    /**
     * Pointer to a class with the proper interface to draw the GB pixels to screen.
     */
    private DrawsGameboyScreen dgs;

    /**
     * Audio handler.
     */
    SoundHandler soundHandler;

    /**
     * Audio is disabled by default.
     */
    private boolean audioEnabled;

    /**
     * Sample rate for audio, default = 44100.
     */
    private int sampleRate = 44100;

    /**
     * Sound samples per frame... or something?
     */
    private int samples = 735;

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
        timer = new Timer();
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
        nextHaltCycle = GB_CYCLES_PER_FRAME;
        running = false;
        timer.reset();
        disableAudio();
        resetAudio();
    }

    /**
     * Initializes the Audio module.
     */
    public void resetAudio() {
    	try {
    		soundHandler = new SoundHandler( (AddressBus)ram, sampleRate, samples );
        } catch ( LineUnavailableException e ) {
        	System.out.println("Error when initializing audio: " + e);
        	audioEnabled = false;
        }
    }

    /**
     * Generates one frame of Audio.
     */
    private void generateAudio() {
    	if ( !audioEnabled )
    		return;

		soundHandler.generateTone();
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

        timer.update(cycleInc, ram);

        // Update LCD
        lcd.updateLCD(cycleInc);
        if ( dgs != null && lcd.newScreenAvailable() ) {
        	dgs.drawGameboyScreen(lcd.getScreen());
        }

        return cycleInc;
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
    	if ( audioEnabled ) {
    		soundHandler.close();
    	}
    	audioEnabled = false;
    }

    /**
     * {@inheritDoc}
     */
    public void saveState( FileOutputStream os ) throws IOException {
    	FileIOStreamHelper.writeData(os, cycles, 8);
    	FileIOStreamHelper.writeData(os, nextHaltCycle, 8);
    	FileIOStreamHelper.writeBoolean(os, audioEnabled);
    	FileIOStreamHelper.writeData(os, sampleRate, 4);
    	FileIOStreamHelper.writeData(os, samples, 4);

    	timer.saveState(os);
    	cpu.saveState(os);
    	lcd.saveState(os);
    	soundHandler.saveState(os);
    }

    /**
     * {@inheritDoc}
     */
    public void readState( FileInputStream is ) throws IOException {
    	cycles = FileIOStreamHelper.readData(is, 8);
    	nextHaltCycle = FileIOStreamHelper.readData(is, 8);
    	audioEnabled = FileIOStreamHelper.readBoolean(is);
    	sampleRate = (int) FileIOStreamHelper.readData(is, 4);
    	samples = (int) FileIOStreamHelper.readData(is, 4);

    	timer.readState(is);
    	cpu.readState(is);
    	lcd.readState(is);
    	soundHandler.readState(is);
    }

    /**
     * Getter for audioEnable, ie if the sound is enabled or not.
     *
     * @return the audioEnabled
     */
    public final boolean isAudioEnabled() {
        return audioEnabled;
    }

}
