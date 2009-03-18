package fajitaboy;
import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.*;

import javax.sound.sampled.LineUnavailableException;

public class AudioMain {
    private static final int INSTRUCTIONS = 5000000;
    public static void main(String[] args) throws LineUnavailableException {
		System.out.println("Running " + INSTRUCTIONS + " instructions");
		Audio au = new Audio();
		AddressBus ab = new AddressBus("/tetris_patched.gb");
		Cpu cpu = new Cpu(ab);
		Oscillator oc = new Oscillator(cpu, ab);
		
		int duration = 50;
		int volume = 125;
		//c.reset();
		for (int i = 0; i < INSTRUCTIONS; i++){
			//System.out.println("looping");
		    oc.step();
			int low = ab.read(SOUND1_LOW);
			int high = ab.read(SOUND1_HIGH);
			int freq = (high&AUDIO_FREQ_MASK)<<AUDIO_UPPER_BYTE | low;
				au.generateTone(freq,duration, volume);
			//System.out.println(cpu.getSP());
			//if(ab.read(0xFF24)>1)
			//	System.out.println(ab.read(0xFF24));
	    }
    }
}