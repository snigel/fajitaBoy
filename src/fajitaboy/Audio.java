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
	int samples = 735;
	boolean first = true;
	
	
	public Audio()
	throws LineUnavailableException {
		samplerate = 44100;
		af = new AudioFormat(samplerate,8,1,true,false);
		sdl = AudioSystem.getSourceDataLine(af);
		buffer = new byte[samples*2];
		sdl.open(af);
		sdl.start();
		
	}
	
	public void generateTone(int freq,int duration, int volume){
	    /*
	    if (samples > sdl.available()) {
	        buffer = new byte[sdl.available()];	        
	    }
	    else {
	        buffer = new byte[samples];
	    }
	    */
		//for(int i=0; i<(float)(duration)/1000*samplerate; i++){
	    if(first) {
	        //buffer = new byte[735];
	        for (int i = 0; i < 735; i++) {
			    double angle = i/(samplerate/freq)*2.0*Math.PI;
			
			    buffer[i]=(byte) (Math.signum(Math.sin(angle)*volume));
		    }
	        first=false;
	    }
	    else {
	        for (int i = 735; i < 1470; i++) {
	            double angle = i/(samplerate/freq)*2.0*Math.PI;
	            
                buffer[i]=(byte) (Math.signum(Math.sin(angle)*volume));
	        }
	        first=true;
            noise(buffer);
	    }
	}
	private void noise(byte[] buffer){
		sdl.write(buffer,0,buffer.length);
	}
}