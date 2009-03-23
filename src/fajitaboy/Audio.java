package fajitaboy;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Audio {

    AudioFormat af;

    byte[] buffer1;
    byte[] buffer2;
    SourceDataLine sdl;

    float samplerate = 44100/2;

    int samples = 735;

    public Audio() throws LineUnavailableException {
        af = new AudioFormat(samplerate*2, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        sdl = (SourceDataLine) AudioSystem.getLine(info);
     //   sdl = AudioSystem.getSourceDataLine(af);
        buffer1 = new byte[samples];
        buffer2 = new byte[samples];
        sdl.open(af);
        sdl.start();

    }

    public void generateTone(int freq1, int duration, int volume) {
        buffer1 = new byte[samples];
        for (int i=0; i < samples; i++) {
            double angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[0] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[1] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[2] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[3] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[4] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[5] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[6] = (byte) (100 * Math.signum((Math.sin(angle1))));
            i++;
            angle1 = i / (samplerate/freq1) * 2.0 * Math.PI;
            buffer1[7] = (byte) (100 * Math.signum((Math.sin(angle1))));
            sdl.write(buffer1, 0, 8);
        
        }
    }
}
