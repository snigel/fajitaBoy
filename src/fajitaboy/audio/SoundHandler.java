package fajitaboy.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

import fajitaboy.memory.AddressBus;

import fajitaboy.memory.AddressBus;

/**
 * This class handles the sound
 *
 * @author Adam Hulin, Johan Gustafsson
 *
 */
public class SoundHandler {
    private AudioFormat audioFormat;
    private SourceDataLine sourceDataLine;
    private float sampleRate;
    private int bufferLength;
    private SoundChannel1 channel1;
    private int playLength;
    private AddressBus addressbus;

    public SoundHandler(AddressBus addresbus) {
        this.addressbus = addressbus;
        // Prövar med den här sampleraten.
        sampleRate = 44100;
        bufferLength = 100; //100 ms
        channel1 = new SoundChannel1(addressbus, sampleRate);
        //Används i playSound().
        playLength = (int) (sampleRate / 60);

        //Använder 8 bits signed pcm. Tror inte det spelar så stor roll
        //om det är signed eller inte. Nu när den är signed så är det 0
        //som är medelpunkten = tyst.
        audioFormat =
            new AudioFormat(Encoding.PCM_SIGNED,
                            sampleRate,
                            8, //Antal bits per sample
                            2, //Antal kanaler, kör med stereo
                            2, // Framesize in bytes = samplesize*antal kanaler.
                            sampleRate,  //Förstår inte riktigt skillnaden, för den
                                         //blir ju samma som sample rate, kanske finns
                                         //special fall.

                            true); //Big endian

        //Fann att vi måste ta en lite om väg via dataline för att få att vi använder pcm.
        //http://www.javafaq.nu/java-example-code-454.html
        DataLine.Info info =
            new DataLine.Info (SourceDataLine.class, audioFormat);
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine (info);
        } catch (LineUnavailableException e) {
            // Kanske ha någon sound enable variabel som sätts av här?
            e.printStackTrace();
        }

        //Nu ska en buffer storlek bestämmas. Varför inte en så stor som möjligt?
        //Vi är ju rädda för att hamna i block state, när vi försöker skriva till
        //en fullbuffer. Men kollar man i datalines api så står det att det finns
        //en max storlek. I https://www.cs.auckland.ac.nz/references/java/java1.5/tutorial/sound/capturing.html
        //står det att man bara ska fylla en liten del av buffer vid varje skrivning.
        //De använder en femtedel. Så om vi ska fylla med 16.6 ms vi varje vblank. Så
        //kan buffern vara 16.6 * 5. Så jag sätter den till 100 ms. Vi får nog förmodligen
        //kanske utöka den. Vi har ju snackat om 200 ms.
        try {
            //Vet inte riktigt om vi måste ta hänsyn till stereo när vi beräknar buffer
            //size. I så fall får vi lägga till en faktor 2.
            sourceDataLine.open(audioFormat, ((int) (sampleRate / 1000 * bufferLength)));
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sourceDataLine.start();
    }

    //Den här metoden kallas vid vblank och skall mixa ihop de fyra kanalerna.
    public void playSound() {
        //Det är den här delen jag är lite osäker på. Behöver lite try and error.
        //Men om vi kallar på vblank 60 / sek. Så behöver vi producera 16.6 ms ljud.
        //Som sagt exakt hur det blir slut ändan, vet jag inte än.
        byte buffer[] = new byte[playLength];
        //Här skickar vi med alla samma buffer till samtliga kanaler för att
        //addera ihop vågorna.
        //http://deku.gbadev.org/program/sound1.html
        channel1.generateSound(buffer);
        sourceDataLine.write(buffer, 0, playLength);
    }
}
