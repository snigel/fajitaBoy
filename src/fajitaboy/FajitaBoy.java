package fajitaboy;

import java.applet.AudioClip;

import java.awt.Color;
import java.awt.Dimension;

import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fajitaboy.lcd.LCD;
import fajitaboy.memory.AddressBus;
import static fajitaboy.constants.PanelConstants.*;

/**
 * An applet a day keeps the doctor away.
 * 
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
     * The fileChooser that is being used whenever the user browse the file
     * system.
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
        PLAYGAME,

        /** Ingame Menu. */
        INGAME_MENU,

        /** Pause game. */
        PAUSE
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

    /** Contains the gamePanel and an optional menu. */
    private LayeredGamePanel layeredGamePanel;

    /** Menu. */
    private IngameMenuPanel ingameMenuPanel;

    /** Menu for keybindings. */
    private KeySettingsPanel keySettingsPanel;

    /** "Pause". */
    private JLabel pauseText;

    /** Keybindings. */
    private KeyInputController kic;

    // ------------------------------------------------------------------------
    // - Applet overrides
    // ------------------------------------------------------------------------

    /**
     * @inheritDoc
     */
    public final void init() {

        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager
                    .setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        // Init paths to user home catalog
        romPath = System.getProperty("user.home");
        fileChooser = new JFileChooser(romPath);

        // Init panels
        startScreen = new StartScreenPanel(this);
        singleplayerLoadscreen = new SingleplayerLoadPanel(this, fileChooser);
        ingameMenuPanel = new IngameMenuPanel(this);
        keySettingsPanel = new KeySettingsPanel(this);

        pauseText = new JLabel(" PAUSED ");
        // pauseText.setBackground(Color.white);
        pauseText.setOpaque(true);
        pauseText.setFont(FB_INGAMEFONT);
        pauseText.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Appletviewer resize
        resize(frameSize);

        // Highly necessary
        bootSound = getAudioClip(getCodeBase(), "bootsound_mockup.wav");

        // Set state to startup screen
        gameState = GameState.STARTSCREEN;
        setContentPane(startScreen);
        getContentPane().validate();
        // addKeyListener(this);

        addMouseListener(new MouseController());
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
     * 
     * @param state
     *            the state/panel
     */
    public final void changeGameState(final GameState state) {

        // Do something depending on current state
        switch (gameState) {
        case PLAYGAME:
            emulator.stop();
            gamePanel.setIgnoreRepaint(false);
            break;
        case INGAME_MENU:
            layeredGamePanel.removeOverlapingPane();
            break;
        case PAUSE:
            layeredGamePanel.removeOverlapingPane();
            break;
        default:
            break;
        }

        // Switch to new state
        switch (state) {
        case STARTSCREEN:
            setContentPane(startScreen);
            showStatus("Start Screen");
            break;

        case SINGLEPLAYER_LOADSCREEN:
            setContentPane(singleplayerLoadscreen);
            showStatus("Singleplayer Screen");

            break;
        case PLAYGAME:
            gamePanel.setIgnoreRepaint(true);
            setContentPane(layeredGamePanel);
            showStatus("Emulator Screen");
            emulatorThread = new Thread(emulator.oscillator);
            emulatorThread.start();
            break;

        case INGAME_MENU:
            layeredGamePanel.setOverlapingPane(ingameMenuPanel);
            keySettingsPanel.refreshLabels();
            showStatus("Ingame menu screen");
            break;
        case PAUSE:
            layeredGamePanel.setOverlapingPane(pauseText);
            showStatus("Paused!");
            break;
        default:
            return; // Non-implemented state or something
        }
        gameState = state;
        getContentPane().requestFocusInWindow();
        validate();
    }

    /**
     * Changes to game view and starts emulation of the rom with given path.
     * 
     * @param path
     *            filepath to rom
     */
    public final void startGame(final String path) {

        showStatus("Loading...");
        gamePanel = new GamePanel(2);
        layeredGamePanel = new LayeredGamePanel(gamePanel);

        emulator = new Emulator(path);

        kic = new KeyInputController(this, layeredGamePanel,
                emulator.addressBus.getJoyPad());

        // gamePanel.addKeyListener(kic);
        // layeredGamePanel.addKeyListener(kic);
        // addKeyListener(kic);

        changeGameState(GameState.PLAYGAME);
    }

    /**
     * Resets the emulator.
     */
    public final void resetEmulator() {
        emulator.addressBus.reset();
        emulator.cpu.reset();
        emulator.oscillator.reset();
    }

    /**
     * Returns what state the applet is in.
     * 
     * @return applet game state
     */
    public final GameState getGameState() {
        return gameState;
    }

    /**
     * Returns the input controller.
     * 
     * @return kic
     */
    public final KeyInputController getKIC() {
        return kic;
    }

    /*
     * public void saveState() { File state; int retVal =
     * stateChooser.showSaveDialog(null);
     * 
     * if (retVal != JFileChooser.APPROVE_OPTION) { return; } state =
     * stateChooser.getSelectedFile();
     * 
     * if (!state.exists()) { try { if (!state.createNewFile()) {
     * errorMsg("File creation error!"); return; } } catch (IOException e) {
     * errorMsg("IO FAIL"); e.printStackTrace(); } } else {
     * errorMsg("Uhoh, this file exists. \n Too scared to overwrite!"); return;
     * }
     * 
     * if (!state.canWrite()) { errorMsg("File writing error"); return; }
     * 
     * try { FileOutputStream fos = new FileOutputStream(state);
     * ObjectOutputStream oos = new ObjectOutputStream(fos);
     * oos.writeObject(emulator); } catch (FileNotFoundException e) {
     * errorMsg("File could not be found."); } catch (IOException e) {
     * errorMsg("Unknown IO exception! FORMATING C:\\"); // TODO less //
     * dramatic e.printStackTrace(); }
     * 
     * }
     * 
     * public void loadState() { File state; int retVal =
     * stateChooser.showOpenDialog(null);
     * 
     * if (retVal != JFileChooser.APPROVE_OPTION) { return; } state =
     * stateChooser.getSelectedFile();
     * 
     * if (!checkFile(state)) { return; }
     * 
     * Object obj; try { FileInputStream fis = new FileInputStream(state);
     * ObjectInputStream ois = new ObjectInputStream(fis); obj =
     * ois.readObject();
     * 
     * if (obj instanceof Emulator) { emulator = (Emulator) obj; } } catch
     * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException e)
     * { e.printStackTrace(); } catch (ClassNotFoundException e) {
     * e.printStackTrace(); } }
     */

    /**
     * Checks if the file is readable etc.
     * 
     * @param file
     *            file to check
     * @return true if everythings cool
     */
    public static boolean checkFile(final File file) {
        if (!file.exists()) {
            errorMsg("File doesn't exist, try again.");
            return false;
        }
        if (!file.isFile()) {
            errorMsg("No file selected, try again.");
            return false;
        }
        if (!file.canRead()) {
            errorMsg("File cannot be read, try again.");
            return false;
        }
        return true;
    }

    /**
     * Shows an error message.
     * 
     * @param msg
     *            What to show in the box
     */
    public static void errorMsg(final String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Encapsulates the emulator.
     * 
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
         * Standard constructor.
         * 
         * @param path
         *            Rom path
         */
        Emulator(final String path) {

            addressBus = new AddressBus(path);
            cpu = new Cpu(addressBus);
            oscillator = new Oscillator(cpu, addressBus, gamePanel, true);
        }

        /**
         * Pauses the emulator.
         */
        public void stop() {
            oscillator.stop();
        }

        /**
         * Returns the emulator screen.
         * 
         * @return LCD the screen.
         */
        public LCD getLCD() {
            return oscillator.getLCD();
        }

    }

    /**
     * Handles the applet mouse events.
     */
    private class MouseController extends MouseAdapter {
        /**
         * Makes sure that the applet can regain the focus by clicking on it.
         * @param e mouseevent
         */
        public void mousePressed(final MouseEvent e) {
            gamePanel.requestFocus();
        }
    }

}
