package fajitaboy.gbc;

import fajitaboy.DrawsGameboyScreen;
import fajitaboy.SpeedSwitch;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.Oscillator;
import fajitaboy.gb.Timer;
import fajitaboy.gb.lcd.LCD;
import fajitaboy.gb.memory.AddressBus;
import fajitaboy.gbc.lcd.CGB_LCD;
import fajitaboy.gbc.memory.CGB_AddressBus;

/**
 * Oscillator in CGB mode.
 */
public class CGB_Oscillator extends Oscillator {
    
    /**
     * Creates a new OscillatorCgb object.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     * @param ss
     *            Pointer to SpeedSwitch instance.
     */
    public CGB_Oscillator(Cpu cpu, CGB_AddressBus ram, SpeedSwitch ss) {
        super(cpu, ram, new CGB_LCD(ram), new Timer());   
        speedSwitch = ss;
    }

    /**
     * Creates a new Oscillator with a screen rendering instance and possibility to enable audio.
     * @param cpu
     *            Pointer to CPU instance.
     * @param ram
     *            Pointer to MemoryInterface instance.
     * @param ss
     *            Pointer to SpeedSwitch instance.
     * @param dgs
     *            Pointer to DrawsGameboyScreen instance.
     */
    public CGB_Oscillator(Cpu cpu, CGB_AddressBus ram, SpeedSwitch ss, DrawsGameboyScreen dgs,
            boolean enableAudio) {
        super(cpu, ram, new CGB_LCD(ram), new Timer());
        this.dgs = dgs;
        if ( enableAudio )
            enableAudio();
        speedSwitch = ss;
    }
}
