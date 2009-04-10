package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;

import javax.sound.sampled.LineUnavailableException;

import fajitaboy.memory.AddressBus;

public class Audio {
    private float sampleRate;
    private int oldFreq;
    private int amp;
    private AddressBus ab;
    private int freq;
    private int pos;
    private int waveLength;
    private int dutyLength;
    private int step;
    private int stepLength;
    private int sweepLength;
    private int sweepSteps;
    private int sweepDirection;
    private int sweepNr;
    private boolean lengthEnabled;
    private int toneLength;

    public Audio(AddressBus ab,float sampleRate) throws LineUnavailableException {
        this.ab = ab;
        this.sampleRate = sampleRate;
        pos = 0;
        oldFreq = 0;
        amp = 32;
        lengthEnabled = false;
    }

    public byte[] generateTone(byte[] destBuff, boolean left, boolean right, int samples) {
        calcFreq();
        if(((ab.read(NR12_REGISTER) & 0xF0 )>> 4) == 0) {
            return destBuff;
        }
                

        if ((toneLength > 0 && lengthEnabled) || !lengthEnabled) {
            if(lengthEnabled) {
                toneLength -= samples;
            }
            
            int finalAmp;
            int k = 0;
            for (int i = 0; i < samples; i++) {

                //Envelope
                if (stepLength != 0) {
                    if ((i % (stepLength * samples)) == 0) {
                        if ((amp > 0) && (amp < 32)) {
                            amp += step;
                        }
                    }
                }
                //Sweep
                if (sweepLength != 0) {
                    if (((i % (sweepLength * samples)) == 0)
                            && (sweepNr < sweepSteps)) {
                        if (sweepDirection == 0) {
                            freq = freq + (int) (freq / (Math.pow(2, sweepNr)));
                        } else {
                            freq = freq - (int) (freq / (Math.pow(2, sweepNr)));
                        }
                    }
                    calcWavePattern();
                }

                if (pos < dutyLength) {
                    finalAmp = -amp;
                }

                else {
                    finalAmp = amp;
                }
                if (left) {
                    destBuff[k] += (byte) finalAmp;
                }
                k++;
                if (right) {
                    destBuff[k] += (byte) finalAmp;
                }
                k++;
                pos = (pos + 1) % waveLength;
            }
        }
        return destBuff;
    }

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


    private void calcFreq() {
        int low1 = ab.read(SOUND1_LOW);
        int high1 = ab.read(SOUND1_HIGH) * 0x100;
        int tmp = (2047 - (high1 + low1) & 0x7ff);
        if(tmp != 0 ) {
            freq = 131072 / tmp;
        }
        else {
            freq = 131072;
        }
        if (freq == oldFreq) {
            return;
        } else {
            calcToneLength();
            calcSweepLength();
            calcEnvelope();
            dutyLength = calcWavePattern();
            oldFreq = freq;
        }
    }

    /**
     * 
     */
    private void calcToneLength() {
        lengthEnabled = ((ab.read(NR14_REGISTER) & 0x40) > 0);
        if(lengthEnabled) {
            toneLength = (int) (((64 -((double)(ab.read(NR11_REGISTER) & 0x3F))) / 256) * sampleRate);
        }
    }

    /**
     * @return
     */
    private void calcSweepLength() {
        sweepNr = 0;
        int nr10 = ab.read(NR10_REGISTER);
        sweepSteps =  nr10 & 0x7;
        sweepDirection = nr10 & 8;
        int sweepTime = ((nr10 & 0x70) >> 4);

        switch(sweepTime) {
        case 0:
            sweepLength = 0;
            break;
        case 1:
            sweepLength = (int)((sampleRate / 1000) * 7.8);
            break;
        case 2:
            sweepLength = (int)((sampleRate / 1000) * 15.6);
            break;
        case 3:
            sweepLength = (int)((sampleRate / 1000) * 23.4);
            break;
        case 4:
            sweepLength = (int)((sampleRate / 1000) * 31.3);
            break;
        case 5:
            sweepLength = (int)((sampleRate / 1000) * 39.1);
            break;
        case 6:
            sweepLength = (int)((sampleRate / 1000) * 46.9);
            break;
        case 7:
            sweepLength = (int)((sampleRate / 1000) * 54.7);
            break;
        default:
            sweepLength = 0;
            break;
        }

    }

    /**
     *
     */
    private int calcWavePattern() {
        waveLength = (int)((sampleRate) / (float)freq);
        if (waveLength == 0) {
            waveLength = 1;
        }
        int nr11 = ((ab.read(NR11_REGISTER) & 0xC0) >> 6);
        switch(nr11) {
        case 0:
            return (int)((float)waveLength * 0.125);
        case 1:
            return (int)((float)waveLength * 0.25);
        case 2:
            return (int)((float)waveLength * 0.50);
        case 3:
            return (int)((float)waveLength * 0.75);
        default:
            return 0;
        }
    }
}
