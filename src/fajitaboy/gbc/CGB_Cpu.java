package fajitaboy.gbc;

import static fajitaboy.constants.AddressConstants.*;

import fajitaboy.SpeedSwitch;
import fajitaboy.gb.Cpu;
import fajitaboy.gb.memory.MemoryInterface;

/**
 * Cpu in CGB mode·.
 */
public class CGB_Cpu extends Cpu {
    
    /**
     * SpeedSwitch object. Used to change the speed.
     */
    private SpeedSwitch speedSwitch;
    
    /**
     * 
     * @param addressbus
     * @param ss SpeedSwitch object that to use if the speed should be changed.
     */
    public CGB_Cpu(MemoryInterface addressbus, SpeedSwitch ss) {
        super(addressbus);
        speedSwitch = ss;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        super.reset();
        // in CGB hardware a starts with the value 0x11 instead of 0x01.
        a = 0x11;
        if (speedSwitch != null) {
            speedSwitch.reset();
        }
    }
    /**
     * Called each time the STOP instruction is executed.
     * If the Prepare Speed Switch is activated the speed is changed
     */
    protected void stopActions() {
        int data = ram.read(ADDRESS_SPEED_SWITCH);
        
        if ((data & 0x01) == 1 ) {
            speedSwitch.toggleSpeed();
            
            if (speedSwitch.getSpeed() == 2) {
                ram.write(0x80, ADDRESS_SPEED_SWITCH);
            } else {
                ram.write(0x00, ADDRESS_SPEED_SWITCH);
            }
        }
    }

}
