package fajitaboy;

import java.applet.AudioClip;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;

import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import fajitaboy.applet.CookieJar;
import fajitaboy.applet.FullScreenFrame;
import fajitaboy.applet.GamePanel;
import fajitaboy.applet.GamePanelMultiplayer;
import fajitaboy.applet.IngameMenuPanel;
import fajitaboy.applet.KeyInputController;
import fajitaboy.applet.LayeredGamePanel;
import fajitaboy.applet.MultiplayerLoadPanel;
import fajitaboy.applet.SingleplayerLoadPanel;
import fajitaboy.applet.StartScreenPanel;
import static fajitaboy.constants.PanelConstants.*;
import static fajitaboy.constants.LCDConstants.*;

/**
 * An applet a day keeps the doctor away.
 * 
 * @author Marcus Johansson, Peter Olsson
 */
@SuppressWarnings("serial")
public class FajitaBoy extends JApplet implements ComponentListener {

    // ------------------------------------------------------------------------
    // -- Global variables.
    // ------------------------------------------------------------------------
	
    // - Emulator stuff
    /** Emulator containing emulation components. */
    private Emulator emulator;

    /** The thread in which runs the emulation. */
    private Thread emulatorThread;

    /** Start sound that is played when the Applet has started. */
    private AudioClip bootSound;

    /** The size of the applet. */
    private Dimension frameSize = new Dimension(320, 288);

    /** Used for loading ROMs. */
    private JFileChooser fileChooser;

    /** Used for loading/saving states. */
    private JFileChooser stateChooser;

    /** Keybindings. */
    private KeyInputController kic;

    /** Path to the rom file. Not sure if this is needed. */
    private String romPath;

    /** Hidden cursor. */
    private Cursor hiddenCursor;

    /** Handles cookies. */
    private CookieJar cookieJar;

    /** Enum describing which pane the applet is showing. */
    public enum GameState {
        STARTSCREEN, SINGLEPLAYER_LOADSCREEN, MULTIPLAYER_LOADSCREEN, PLAYGAME, INGAME_MENU, PAUSE
    }

    // - Panels
    /** The applet state, ie in menu or emulating. */
    private GameState gameState;

    /** Start screen menu. Select single/multiplayer. */
    private StartScreenPanel startScreen;

    /** Singleplayer menu. Select ROM and play. */
    private SingleplayerLoadPanel singleplayerLoadscreen;
    
    /** Multiplayer menu. Select ROM and play. */
    private MultiplayerLoadPanel multiplayerLoadscreen;

    /** Emulator panel, where the actual emulator screen is shown. */
    private GamePanel gamePanel;
    private GamePanelMultiplayer gamePanelMultiplayer;

    /** Contains the gamePanel and an optional menu. */
    private LayeredGamePanel layeredGamePanel;

    /** Menu. */
    private IngameMenuPanel ingameMenuPanel;

    /** Fullscreen frame. Is null if not in fullscreen mode. */
    private FullScreenFrame fullScreen;

    /** Label to show in apple area when in fullscreen mode. */
    private JLabel fullScreenPlaceHolder;

    /** "Pause". */
    private JLabel pauseText;
    
    /** Multiplayer mode enabled */
    boolean multiplayer;

    // ------------------------------------------------------------------------
    // - Applet overrides
    // ------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void init() {
        try {
            UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            System.out.println("Look&Feel fail. Using default.");
        }

        addComponentListener(this);
        cookieJar = new CookieJar(this);
        executeJS("disableLinks()");

        // Appletviewer resize
        resize(frameSize);
        setPreferredSize(frameSize);

        // Init paths to user home catalog
        romPath = System.getProperty("user.home");
        fileChooser = new JFileChooser(romPath);
        stateChooser = new JFileChooser(romPath);

        // Init panels
        startScreen = new StartScreenPanel(this);
        singleplayerLoadscreen = new SingleplayerLoadPanel(this, fileChooser);
        multiplayerLoadscreen = new MultiplayerLoadPanel(this, fileChooser);

        fullScreenPlaceHolder = new JLabel("Click to exit fullscreen mode!");

        pauseText = new JLabel(" PAUSED ");
        pauseText.setOpaque(true);
        pauseText.setFont(FB_INGAMEFONT);
        pauseText.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Highly necessary
        bootSound = getAudioClip(getClass().getResource(
                "resources/bootsound.wav"));

        // Set state to startup screen
        gameState = GameState.STARTSCREEN;
        setContentPane(startScreen);
        getContentPane().validate();
        // addKeyListener(this);

        addMouseListener(new MouseController());

        // create the 'hidden' cursor
        hiddenCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                Toolkit.getDefaultToolkit().createImage(
                        new MemoryImageSource(0, 0, new int[0], 0, 0)),
                new Point(0, 0), "hiddenCursor");
    }

    /** {@inheritDoc} */
    public final void start() {
        bootSound.play();
    }

    /** {@inheritDoc} */
    public final void stop() {
    }

    /** {@inheritDoc} */
    public final void destroy() {
        if (emulator != null) {
            emulator.disableAudio();
        }
    }

    // ------------------------------------------------------------------------
    // -- Applet functionality.
    // ------------------------------------------------------------------------

    /**
     * Changes what panel to show.
     * 
     * @param state the state/panel
     */
    public final void changeGameState(final GameState state) {

        // Do something depending on current state
        switch (gameState) {
        case PLAYGAME:
            emulator.stop();
            if ( multiplayer == false )
            	gamePanel.setIgnoreRepaint(false);
            else
            	gamePanelMultiplayer.setIgnoreRepaint(false);
            layeredGamePanel.setCursor(Cursor.getDefaultCursor());
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
            executeJS("disableLinks()");
            deactivateFullScreen();
            if (emulator != null) {
                emulator.disableAudio();
            }
            setContentPane(startScreen);
            showStatus("Start Screen");
            break;

        case SINGLEPLAYER_LOADSCREEN:
            executeJS("disableLinks()");
            deactivateFullScreen();
            if (emulator != null) {
                emulator.disableAudio();
            }
            singleplayerLoadscreen.loadPath();
            setContentPane(singleplayerLoadscreen);
            showStatus("Singleplayer Screen");
            break;
            
        case MULTIPLAYER_LOADSCREEN:
        	executeJS("disableLinks()");
            deactivateFullScreen();
            if (emulator != null) {
                emulator.disableAudio();
            }
            multiplayerLoadscreen.loadPath();
            setContentPane(multiplayerLoadscreen);
            showStatus("Multiplayer Screen");
            break;

        case PLAYGAME:
            layeredGamePanel.setCursor(hiddenCursor);
            if ( multiplayer == false ) {
            	gamePanel.setIgnoreRepaint(true);
            } else {
            	gamePanelMultiplayer.setIgnoreRepaint(true);
            }
            if (fullScreen != null) {
                fullScreen.setContentPane(layeredGamePanel);
            } else {
                setContentPane(layeredGamePanel);
            }
            showStatus("Emulator Screen");
            emulatorThread = new Thread(emulator);
            emulatorThread.start();
            executeJS("enableLinks()");
            break;

        case INGAME_MENU:
            layeredGamePanel.setOverlapingPane(ingameMenuPanel);
            ingameMenuPanel.refresh();
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

        if (fullScreen != null) {
            fullScreen.getContentPane().requestFocusInWindow();
            fullScreen.validate();
        } else {
            getContentPane().requestFocusInWindow();
            validate();
        }
    }

    /**
     * Changes to game view and starts emulation of the rom with given path.
     * 
     * @param path filepath to rom
     */
    public final void startGame(final String path) {

        multiplayer = false;
    	
    	showStatus("Loading...");
        gamePanel = new GamePanel(2);
        emulator = new Emulator(path, gamePanel);
        ingameMenuPanel = new IngameMenuPanel(this);
        layeredGamePanel = new LayeredGamePanel(gamePanel, LCD_W, LCD_H );
        layeredGamePanel.updateSize(getWidth(), getHeight());

        kic = new KeyInputController(this, layeredGamePanel, ingameMenuPanel,
                emulator);
        kic.importKeys();

        ingameMenuPanel.refreshLabels();
        ingameMenuPanel.setEmulator(emulator);

        changeGameState(GameState.PLAYGAME);
    }
    
    public final void startGameMultiplayer(final String path) {

    	multiplayer = true;
    	
    	setPreferredSize(new Dimension(640, 288));
    	showStatus("Loading...");
        gamePanelMultiplayer = new GamePanelMultiplayer(2);
        emulator = new Emulator(path, gamePanelMultiplayer.vr1, gamePanelMultiplayer.vr2);
        ingameMenuPanel = new IngameMenuPanel(this);
        layeredGamePanel = new LayeredGamePanel(gamePanelMultiplayer, LCD_W*2, LCD_H);
        layeredGamePanel.updateSize(getWidth(), getHeight());

        kic = new KeyInputController(this, layeredGamePanel, ingameMenuPanel,
                emulator);
        kic.importKeys();

        ingameMenuPanel.refreshLabels();
        ingameMenuPanel.setEmulator(emulator);

        changeGameState(GameState.PLAYGAME);
    }

    /**
     * Toggle fullscreen mode.
     */
    public final void toggleFullScreen() {
        if (fullScreen != null) {
            deactivateFullScreen();
        } else {
            activateFullScreen();
        }

    }

    /**
     * Activate fullscreen mode.
     */
    public final void activateFullScreen() {
        if (fullScreen == null) {
            fullScreen = new FullScreenFrame(layeredGamePanel);
            gamePanel.grabFocus();
            setContentPane(fullScreenPlaceHolder);
            validate();
        }
    }

    /**
     * Deactivates fullscreen mode.
     */
    public final void deactivateFullScreen() {
        if (fullScreen != null) {
            layeredGamePanel.updateSize(getWidth(), getHeight());
            setContentPane(layeredGamePanel);
            gamePanel.requestFocus();
            fullScreen.dispose();
            fullScreen = null;
        }
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

    /**
     * Returns the emulators oscillator.
     * 
     * @return oscillator
     */
    public final Emulator getEmulator() {
        return emulator;
    }

    /**
     * Returns the cookieJar.
     * 
     * @return cookieJar
     */
    public final CookieJar getCookieJar() {
        return cookieJar;
    }

    /**
     * Save emulator state.
     */
    public final void saveState() {
        File state;
        int retVal = stateChooser.showSaveDialog(null);

        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        state = stateChooser.getSelectedFile();

        if (!state.exists()) {
            try {
                if (!state.createNewFile()) {
                    errorMsg("File creation error!");
                    return;
                }
            } catch (IOException e) {
                errorMsg("IO FAIL");
                e.printStackTrace();
            }
        } else {
            errorMsg("Uhoh, this file exists. \n Too scared to overwrite!");
            return; // TODO Be brave. Overwrite.
        }
        if (!state.canWrite()) {
            errorMsg("File writing error");
            return;
        }

        // Everything is fine, start writing.
        try {
            FileOutputStream fos = new FileOutputStream(state);
            emulator.saveState(fos);
            fos.close();
        } catch (FileNotFoundException e) {
            errorMsg("File could not be found.");
        } catch (IOException e) {
            errorMsg("Save state IOException");
            e.printStackTrace();
        }
    }

    /**
     * Load emulator state.
     */
    public final void loadState() {
        File state;
        int retVal = stateChooser.showOpenDialog(null);

        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        state = stateChooser.getSelectedFile();

        if (!checkFile(state)) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(state);
            emulator.readState(fis);
        } catch (IOException e) {
            errorMsg("State count not be read. Sorry.");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the file is readable etc.
     * 
     * @param file file to check
     * @return true if everything is cool
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
     * @param msg What to show in the box
     */
    public static void errorMsg(final String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Runs some javascript on the fajita-page.
     * 
     * @param script JS-string to run
     */
    public final void executeJS(final String script) {
        try {
            getAppletContext().showDocument(new URL("javascript:" + script),
                    "_self");

        } catch (MalformedURLException e) {
            System.out.println("Invalid JavaScript");
            return; // Instead of blank to please mistress Checkstyle.
        }
    }

    /**
     * Used by JS when reading from cookie.
     * 
     * @param cookieData data
     */
    public final void _JS_setCookie(final String cookieData) {
        cookieJar._JS_setCookie(cookieData);
    }

    /**
     * Used by JS when saving to cookie.
     * 
     * @return data to save
     */
    public final String _JS_getCookie() {
        return cookieJar._JS_getCookie();
    }

    /** {@inheritDoc} */
    public void componentHidden(final ComponentEvent e) {
    }

    /** {@inheritDoc} */
    public void componentMoved(final ComponentEvent e) {
    }

    /** {@inheritDoc} */
    public final void componentResized(final ComponentEvent e) {
        if (layeredGamePanel != null) {
            layeredGamePanel.updateSize(getWidth(), getHeight());
        }
    }

    /** {@inheritDoc} */
    public void componentShown(final ComponentEvent e) {
    }

    // ------------------------------------------------------------------------
    // -- Mouse stuff.
    // ------------------------------------------------------------------------

    /**
     * Handles the applet mouse events.
     */
    private class MouseController extends MouseAdapter {
        /**
         * Makes sure that the applet can regain the focus by clicking on it.
         * 
         * @param e mouseevent
         */
        public void mousePressed(final MouseEvent e) {
            if (gameState == GameState.PLAYGAME
                    || gameState == GameState.INGAME_MENU
                    || gameState == GameState.PAUSE) {
            	if ( multiplayer == false ) {
            		gamePanel.requestFocus();
            	} else {
            		gamePanelMultiplayer.requestFocus();
            	}
            }

            if (fullScreen != null) {
                deactivateFullScreen();
            }
        }
    }

    public static void main(String args[]) {
        Emulator emu = new Emulator(args[0], null, null);
        emu.run();
    }
}
