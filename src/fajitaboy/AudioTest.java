package fajitaboy;
/**
 * We will test how audio works in java here
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
import javax.sound.sampled.*;

public class AudioTest {
    public static void main(String[] args) {
        int seconds = 2;
        int sampleRate = 8000;
        double frequency = 1000.0;
        double RAD = 2.0 * Math.PI;
        try {
        	AudioFormat af = new AudioFormat((float)sampleRate,8,1,true,true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,af);
            SourceDataLine source = (SourceDataLine)AudioSystem.getLine(info);
            source.open(af);
            source.start();
            byte[] buf = new byte[sampleRate * seconds];
            
            for (int i=0; i<buf.length; i++) {
                buf[i] = (byte)(Math.sin(RAD*frequency/sampleRate*i)*127.0);
                //  System.out.println(buf[i]);
            }
            
            source.write(buf,0,buf.length);
            source.drain();
            source.stop();
            source.close();
        } 
        catch (Exception e) {
            System.out.println(e);
        }
        
        System.exit(0);
    }
}

