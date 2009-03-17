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
		buffer = new byte[1];
		sdl.open(af);
		sdl.start();
		
	}
	
	public void generateTone(int freq,int duration, int volume){
		for(int i=0; i<(float)(duration)/1000*samplerate; i++){
			double angle = i/(samplerate/freq)*2.0*Math.PI;
			buffer[0]=(byte)(Math.sin(angle)*volume);
			sdl.write(buffer,0,1);
		}
	}
	//	sdl.drain();
	//	sdl.stop();
	//	sdl.close();
	
}