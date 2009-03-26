/**
 *
 */
package fajitaboy.audio;

import static fajitaboy.constants.AddressConstants.*;
import static fajitaboy.constants.BitmaskConstants.AUDIO_FREQ_MASK;
import static fajitaboy.constants.BitmaskConstants.AUDIO_UPPER_BYTE;
import fajitaboy.memory.AddressBus;

/**
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundChannel1 {

    private AddressBus addressBus;
    private float sampleRate;
    private int sequenceLength;
    private boolean sweepOn;
    private boolean envelopeOn;
    private int freq;
    private int waveLength;
    /**
     * @param addressbus
     * @param sampleRate
     */
    public SoundChannel1(AddressBus addressbus, float sampleRate) {
        this.addressBus = addressbus;
        this.sampleRate = sampleRate;

    }

    /**
     * @param buffer
     */
    public void generateSound(byte[] buffer) {
        //Blir lite av polling här, kan undvikas om vi har en koppling i I/0
        //som meddelar när det skrivs en ny längd. I pandocs så står det
        //att längdbitarna är writeonly så det är kanske så det fungerar.
        //Något vi måste diskutera. I båda lösningarna måste i alla fall
        //calc length anropas.
        /*
        if (sequenceLength == 0) {
            sequenceLength = calcNewWave();
        }
        */
        if (sequenceLength != 0) {
            //Här ska vi dekrementera sequencelength med något lämpligt :)
            //Här tar problemet som jag skrev om i SoundHandler.playSound() an.
            //Ljud kanalerna är klockade i 256 Hz. Vblank 60/sek. 256/60 = 4.2.
            //Om vi behåller gameboy orignal length och räknar av 4. Så är det som
            //vi genererar 1/64 ljud. Vilket också är den minsta delen i, vad jag kan se
            //i envelope och sweep. Men då genereras ljudet i försökta. Vi skulle
            //kanske kunna omvandla och räkna i millisekunder. Som sagt behövs lite
            //tester här.

            //Eftersom sequencelength inte är lika med 0 så är en ljud sekvens aktivt.

            if(sweepOn) {
                //Beräkna  sweep
                calcSweep();
            }

            if(envelopeOn) {
                //Beräkna envelope.
                calcEnvelope();
            }

            //Nu ska arrayen fyllas med vågorna.
            //Behöver lite tanke arbete hur det ska implementeras,
            //vi måste ta hänsyn till våglängden och wave pattern.
            produceWaves(buffer);

        }
    }

    /**
     * @return
     */
    private void calcNewWave() {
        //Beräkna ny längd, antingen med att läsa av registret eller med en
        //setter metod.

        setFrequency();
        setWavePattern();
        setSweep();
        setEnvelope();
    }

    /**
     *
     */
    private void setEnvelope() {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    private void setSweep() {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    private void setWavePattern() {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    private void setFrequency() {
        int low = addressBus.read(SOUND1_LOW);
        int high = addressBus.read(SOUND1_HIGH);
        freq = (high & AUDIO_FREQ_MASK) << AUDIO_UPPER_BYTE | low;
        freq = 131072 / (2048 - freq);
        //Hur lång en vågläng är i samples.
        waveLength = (int) (sampleRate / freq);
    }

    /**
     * @param buffer
     */
    private void produceWaves(byte[] buffer) {


    }

    /**
     *
     */
    private void calcEnvelope() {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    private void calcSweep() {
        // TODO Auto-generated method stub

    }

}
