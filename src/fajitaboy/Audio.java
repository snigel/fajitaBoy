package fajitaboy;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Audio {
    

    AudioFormat af;

    byte[] buffer1;

    byte[] buffer2;

    SourceDataLine sdl;

    float samplerate=11250;
    int sample = (int)(samplerate/60);
    int samples = sample * 100;
    

    boolean first = true;

    int i = 0;

    int j = 0;

    public Audio() throws LineUnavailableException {
        af = new AudioFormat(samplerate, 8, 1, true, false);
        sdl = AudioSystem.getSourceDataLine(af);
        buffer1 = new byte[samples];
        buffer2 = new byte[samples];
        sdl.open(af);
        sdl.start();

    }

    public void generateTone(int freq1, int freq2, int duration, int volume) {
        /*
         * if (samples > sdl.available()) { buffer = new byte[sdl.available()];
         * } else { buffer = new byte[samples]; }
         */
        // for(int i=0; i<(float)(duration)/1000*samplerate; i++){
        // buffer = new byte[sample];
        j = i;
        for (; i < j + sample; i++) {
            double angle1 = i / (samplerate / freq1) * 2.0 * Math.PI;
            double angle2 = i / (samplerate / freq2) * 2.0 * Math.PI;

            buffer1[i] = (byte) (127*(Math.signum(Math.sin(angle1))+Math.signum(Math.sin(angle2)) ));
         //   buffer2[i] = (byte) (Math.signum(Math.sin(angle2))*200);
        }
        i = i + sample;
        if (i == samples) {
            noise(buffer1);
          //  noise(buffer2);
            i = 0;
        }
    }

    private void noise(byte[] buffer) {
        sdl.write(buffer, 0, buffer.length);
    }
}
