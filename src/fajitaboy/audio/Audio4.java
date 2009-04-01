package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import java.util.Random;
import fajitaboy.memory.AddressBus;

public class Audio4 {
    float sampleRate;
    int oldFreq;
    int freq;
    int finalFreq;
    AddressBus ab;
    Random random;
    int amp;
    int step;
    int stepLength;

    public Audio4(AddressBus ab, float sampleRate) {
        this.ab = ab;
        this.sampleRate = sampleRate;
        random = new Random();
        amp = 32;
        oldFreq = 0;
    }

    public byte[] generateTone(byte[] destBuff, boolean left, boolean right, int samples) {
        calcFreq();
        int finalAmp = amp;
        int k = 0;
        for(int i = 0; i < samples; i++) {
          if (stepLength != 0) {
              //Envelope
              if ((i % (stepLength * samples)) == 0) {
                  if ((amp > 0) && (amp < 32)) {
                      amp += step;
                  }
              }
          }

          if(left) {
              destBuff[k] += (byte) finalAmp;
          }
          k++;
          if(right) {
              destBuff[k] += (byte) finalAmp;
          }
          k++;

          if(i % finalFreq  == 0) {
             boolean j = random.nextBoolean();
             if(j)
                 finalAmp = amp;
             else {
                 finalAmp = -amp;
             }
          }
      }
        return destBuff;
    }

    private void calcFreq() {
        int nr43 = ab.read(NR43_REGISTER);
        int s = (nr43 & 0xF0) >> 4 ;
        double r = nr43 & 0x7;
        if(r == 0) {
            r = 0.5;
        }
        freq = (int)((524288 / r) / (Math.pow(2, (s+1))));
        if (freq == oldFreq) {
            return;
        }
        else {
            finalFreq = (int) (sampleRate/freq);
            if (finalFreq == 0) {
                finalFreq = 1;
            }
            calcEnvelope();
            oldFreq = freq;
        }
    }

    /**
     *
     */
    private void calcEnvelope() {
        int nr12 = ab.read(NR12_REGISTER);
        amp = ((nr12 & 0xF0 )>> 4) * 2;
        stepLength = nr12 & 0x7;
        int direction = nr12 & 0x8;
        if (direction == 0) {
            step = -2;
        } else {
            step = 2;
        }
    }
}
