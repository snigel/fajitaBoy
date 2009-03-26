package fajitaboy;


import java.applet.AudioClip;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JApplet;
import javax.swing.JFileChooser;

import fajitaboy.IO.JoyPad;
import fajitaboy.lcd.LCD;

import static fajitaboy.constants.HardwareConstants.*;

/**
 * An applet a day keeps the doctor away.
 * @author Marcus Johansson, Peter Olsson
 */
@SuppressWarnings("serial")
public class FajitaBoy extends JApplet {

    // - Emulator stuff
    /**
     * Emulator containing emulation components.
     */
    private Emulator emulator;

    /**
     * The thread in which runs the emulation.
     */
    private Thread emulatorThread;

    /**
     * Start sound that is played when the Applet has started.
     */
    private AudioClip bootSound;

    /**
     * The size of the applet.
     */
    private Dimension frameSize = new Dimension(320, 288);

    /**
     * The fileChooser that is being used whenever the user
     * browse the file system.
     */
    private JFileChooser fileChooser;

    /**
     * Path to the rom file. Not sure if this is needed.
     */
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

        // emulatorThread = new Thread(emulator);

        // Appletviewer resize
        resize(frameSize);

        // Highly necessary
        bootSound = getAudioClip(getCodeBase(), "bootsound_mockup.wav");

        // Set state to startup screen
        gameState = GameState.STARTSCREEN;
        setContentPane(startScreen);
        getContentPane().validate();

        //addKeyListener(this);
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

    /**
     * Changes what panel to show.
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
            // emulatorThread = new Thread(emulator);
            // emulatorThread.start();
            gamePanel.requestFocusInWindow();
            break;
        default:
            return; // Non-implemented state or something
        }
        gameState = state;
        validate();

        System.out.println("Applet has focus: " + this.hasFocus());

        System.out.println("Start has focus: " + startScreen.hasFocus());

        System.out.println("Single has focus: "
                + singleplayerLoadscreen.hasFocus());
    }

    /**
     * Changes to game view and starts emulation of the rom with given path.
     * @param path
     *            filepath to rom
     */
    public final void startGame(final String path) {

        showStatus("Loading...");
        gamePanel = new GamePanel(2);
        emulator = new Emulator(path);

        changeGameState(GameState.PLAYGAME);
        gamePanel.addKeyListener(
                new KeyInputController(
                        emulator.addressBus.getJoyPad(),
                        emulator.oscillator));
        emulatorThread = new Thread(emulator.oscillator);
        emulatorThread.start();
    }

    /**
     * Encapsulates the emulator.
     * @author Marcus, Peter
     */
    private final class Emulator {

        /** Emulator addressbus. */
        private AddressBus addressBus;

        /** Emulator cpu. */
        private Cpu cpu;

        /** Emulator oscillator. */
        private Oscillator oscillator;

        /**
         * To know if the emulation should keep running.
         */
        private boolean running;

        /**
         * Standard constructor.
         * @param path
         *            Rom path
         */
        Emulator(final String path) {

            addressBus = new AddressBus(path);
            cpu = new Cpu(addressBus);
            oscillator = new Oscillator(cpu, addressBus, gamePanel);
            running = false;
        }

        /**
         * Pauses the emulator.
         */
        public void stop() {
            // oscillator.stop();
            running = false;
        }

        /**
         * Returns the emulator screen.
         * @return LCD the screen.
         */
        public LCD getLCD() {
            return oscillator.getLCD();
        }

    }

}
