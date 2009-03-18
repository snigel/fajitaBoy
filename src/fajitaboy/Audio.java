package fajitaboy;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Audio{
	float samplerate;
	AudioFormat af;
	byte[] buffer;
	SourceDataLine sdl;
	
	
	public Audio()
	throws LineUnavailableException {
		samplerate = 44100;
		af = new AudioFormat(samplerate,8,1,true,false);
		sdl = AudioSystem.getSourceDataLine(af);
		buffer = new byte[706];
		sdl.open(af);
		sdl.start();
		
	}
	
	public void generateTone(int freq,int duration, int volume){
		for(int i=0; i<(float)(duration)/1000*samplerate; i++){
			double angle = i/(samplerate/freq)*2.0*Math.PI;
			
			buffer[i%706]=(byte)(Math.sin(angle)*volume);
			if(i%706==0)
				noise(buffer);
		}
	}
	private void noise(byte[] buffer){
		sdl.write(buffer,0,706);
	}
}