package fajitaboy;

import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.*;

import fajitaboy.lcd.LCD;

/**
 * An applet a day keeps the doctor away.
 * 
 * @author Marcus Johansson, Peter Olsson
 */
@SuppressWarnings("serial")
public class FajitaBoy extends JApplet implements KeyListener{

    // ------------------------------------------------------------------------
    // - Global variables
    // ------------------------------------------------------------------------

    // - Emulator stuff
    private Emulator emulator;
    private Thread emulatorThread;

    /** Emulator addressbus. */
    // private AddressBus addressBus;
    //
    // /** Emulator cpu. */
    // private Cpu cpu;
    //
    // /** Emulator oscillator. */
    // private Oscillator oscillator;
    /** -pling- Fajita! */
    private AudioClip bootSound;

    // - Applet stuff

    /** Appletviewer size. */
    private Dimension frameSize = new Dimension(400, 400);

    /** File opener for roms & savestates. */
    private JFileChooser fileChooser;

    private JTextField fileStringField;

    private String romPath;

    /**
     * Enum describing which pane the applet is showing, ie what is shown on the
     * screen.
     */
    public enum GameState {
        /** StartScreenPanel. */
        STARTSCREEN,

        /** SingleplayerLoadscreenPanel. */
        SINGLEPLAYER_LOADSCREEN,

        /** Multiplayer stuff NOT IMPLEMENTED. */
        MULTIPLAYER_DUNNOLOL,

        /** GamePanel. */
        PLAYGAME
    }

    // - Panels
    /** The applet state, ie in menu or emulating. */
    private GameState gameState;

    /** Start screen menu. Select single/multiplayer. */
    private StartScreenPanel startScreen;

    /** Singleplayer menu. Select ROM and play. */
    private SingleplayerLoadPanel singleplayerLoadscreen;

    /** Emulator panel, where the actual emulator screen is shown. */
    private GamePanel gamePanel;

    // ------------------------------------------------------------------------
    // - Applet overrides
    // ------------------------------------------------------------------------

    /**
     * @inheritDoc
     */
    public final void init() {

        // Init paths to user home catalog
        romPath = System.getProperty("user.home");
        fileChooser = new JFileChooser(romPath);

        // Init panels
        startScreen = new StartScreenPanel(this);
        singleplayerLoadscreen = new SingleplayerLoadPanel(this, fileChooser);

      //  emulatorThread = new Thread(emulator);

        // Appletviewer resize
        resize(frameSize);

        // Highly necessary
        bootSound = getAudioClip(getCodeBase(), "bootsound_mockup.wav");

        // Set state to startup screen
        gameState = GameState.STARTSCREEN;
        setContentPane(startScreen);
        getContentPane().validate();
        
        addKeyListener(this);
    }

    /**
     * @inheritDoc
     */
    public final void start() {
        bootSound.play();

    }

    /**
     * @inheritDoc
     */
    public final void stop() {

    }

    /**
     * @inheritDoc
     */
    public final void destroy() {

    }

    // ------------------------------------------------------------------------
    // - Controllers
    // ------------------------------------------------------------------------

    /**
     * Changes what panel to show.
     * 
     * @param state
     *            the state/panel
     */
    public final void changeGameState(final GameState state) {

        // Do something depending on current state
        switch (gameState) {
        case PLAYGAME:
            emulator.stop();
            break;
        default:
            break;
        }

        // Switch to new state
        switch (state) {
        case STARTSCREEN:
            setContentPane(startScreen);
            startScreen.requestFocusInWindow();
            showStatus("Start Screen");
            break;

        case SINGLEPLAYER_LOADSCREEN:
            setContentPane(singleplayerLoadscreen);
            singleplayerLoadscreen.requestFocusInWindow();
            showStatus("Singleplayer Screen");
            
            break;
        case PLAYGAME:
            setContentPane(gamePanel);
            showStatus("Emulator Screen");
//            emulatorThread = new Thread(emulator);
//            emulatorThread.start();
            gamePanel.requestFocusInWindow();
            break;
        default:
            return; // Non-implemented state or something
        }
        gameState = state;
        validate();
        


        System.out.println("Applet has focus: " +this.hasFocus());

        
        System.out.println("Start has focus: " +startScreen.hasFocus());

        

        System.out.println("Single has focus: " +singleplayerLoadscreen.hasFocus());

        

        //System.out.println("Game has focus: " +gamePanel.hasFocus());
        

    }

    /**
     * Changes to game view and starts emulation of the rom with given path.
     * 
     * @param path
     *            filepath to rom
     */
    public final void startGame(final String path) {

        showStatus("Loading...");

        emulator = new Emulator(path);
        // GamePanel should not need LCD.
        gamePanel = new GamePanel(2);
        
        // TODO Grafikgruppen ta en titt på detta! 
        //emulator.getLCD().setGamePanel(gamePanel);
        
        changeGameState(GameState.PLAYGAME);
        
        //gamePanel.addKeyListener(new KeyInputController(emulator.addressBus));
        gamePanel.addKeyListener(this);
        emulatorThread = new Thread(emulator);
        emulatorThread.start();
        

    }

    // ------------------------------------------------------------------------
    /**
     * Encapsulates the emulator.
     * 
     * @author Marcus, Peter
     * 
     */
    private final class Emulator implements Runnable {

        /** Emulator addressbus. */
        private AddressBus addressBus;

        /** Emulator cpu. */
        private Cpu cpu;

        /** Emulator oscillator. */
        private Oscillator oscillator;
        
        private boolean running = false;

        /**
         * Standard constructor.
         * 
         * @param path
         *            Rom path
         */
        Emulator(final String path) {

            addressBus = new AddressBus(path);
            cpu = new Cpu(addressBus);
            oscillator = new Oscillator(cpu, addressBus);
        }

        /**
         * @inheritDoc
         */
        public void run() {
            showStatus("Game on!");
            running = true;
            int i = 0;
            /*
             * This loop should probably be in the oscillator instead.
             * This loop runs at full speed but it goes too slow anyway. 
             */
            while (running) {
                oscillator.step();
                
                /*
                 * This is only a temporary solution!
                 * Either the oscillator or the LCD should call the draw method
                 * whenever the screen is to be redrawn. 
                 */
                if(i++ > 70000) {
                    gamePanel.draw(oscillator.getLCD().getScreen());
                    i = 0;
                }
            }
            //oscillator.run();
        }
        /**
         * Pauses the emulator.
         */
        public void stop() {
            //oscillator.stop();
            running = false;
        }

        /**
         * Returns the emulator screen.
         * 
         * @return LCD the screen.
         */
        LCD getLCD() {
            return oscillator.getLCD();
        }

    }

    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        System.out.println("KEY :D");
    }

    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        System.out.println("KEY :D");
    }

    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        System.out.println("KEY :D");
    }
}
