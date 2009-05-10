package fajitaboy.applet;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;

import fajitaboy.Emulator;
import fajitaboy.FajitaBoy;
import fajitaboy.FajitaBoy.GameState;

import static fajitaboy.constants.PanelConstants.*;

/**
 * Handles the key input.
 */
public class KeyInputController {

    /** Joypad object. */
    private Emulator emulator;

    /** applet. */
    private FajitaBoy fajitaBoy;
    /** gamepanel. */
    private LayeredGamePanel gamePanel;

    /** keysettings. */
    private IngameMenuPanel keySettingsPanel;

    /** cookies. */
    private CookieJar cookieJar;

    /**
     * Enum for controller buttons.
     */
    public enum ControllerButton {
        LEFT, RIGHT, UP, DOWN, A, B, START, SELECT
    }

    // -- Button pushing actions
    private Action pushLeft, pushRight, pushUp, pushDown;
    private Action pushA, pushB, pushSelect, pushStart;

    // -- Button releasing actions
    private Action releaseLeft, releaseRight, releaseUp, releaseDown;
    private Action releaseA, releaseB, releaseSelect, releaseStart;

    // -- Applet actions
    private Action pause, menu, fullscreen, mute;

    /**
     * Creates a new KeyInputController object that handles key input.
     * 
     * @param fb mainframe
     * @param lgp layeredgameframe
     * @param ksp keysettingspanel
     * @param jp the JoyPad object
     */
    public KeyInputController(final FajitaBoy fb, final LayeredGamePanel lgp,
            final IngameMenuPanel ksp, final Emulator emu) {
        fajitaBoy = fb;
        emulator = emu;
        gamePanel = lgp;
        keySettingsPanel = ksp;
        cookieJar = fb.getCookieJar();

        initActions();
        initActionMap();
        initInputMap();
    }

    /**
     * Inits actions.
     */
    @SuppressWarnings("serial")
    private void initActions() {

        // - Button pushing actions -------------------------------------------

        pushLeft = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey( Emulator.Keys.LEFT, true, Emulator.Player.PLAYER1 );
            }
        };
        pushRight = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.RIGHT, true, Emulator.Player.PLAYER1 );
            }
        };
        pushUp = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.UP, true, Emulator.Player.PLAYER1 );
            }
        };
        pushDown = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.DOWN, true, Emulator.Player.PLAYER1 );
            }
        };
        pushA = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.A, true, Emulator.Player.PLAYER1 );
            }
        };
        pushB = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.B, true, Emulator.Player.PLAYER1 );
            }
        };
        pushStart = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.START, true, Emulator.Player.PLAYER1 );
            }
        };
        pushSelect = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.SELECT, true, Emulator.Player.PLAYER1 );
            }
        };

        // -- Button releasing actions ----------------------------------------

        releaseLeft = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.LEFT, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseRight = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.RIGHT, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseUp = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.UP, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseDown = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.DOWN, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseA = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.A, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseB = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.B, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseStart = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.START, false, Emulator.Player.PLAYER1 );
            }
        };
        releaseSelect = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	emulator.setKey( Emulator.Keys.SELECT, false, Emulator.Player.PLAYER1 );
            }
        };

        // -- Applet actions --------------------------------------------------

        pause = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (fajitaBoy.getGameState() == GameState.PLAYGAME) {
                    fajitaBoy.changeGameState(GameState.PAUSE);
                } else {
                    fajitaBoy.changeGameState(GameState.PLAYGAME);
                }
            }
        };
        menu = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                if (fajitaBoy.getGameState() == GameState.INGAME_MENU) {
                    fajitaBoy.changeGameState(GameState.PLAYGAME);
                } else {
                    fajitaBoy.changeGameState(GameState.INGAME_MENU);
                }
            }
        };

        fullscreen = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                fajitaBoy.toggleFullScreen();
            }
        };
        mute = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
            	if (fajitaBoy.getEmulator().isAudioEnabled()) {
                    fajitaBoy.getEmulator().disableAudio();
                } else {
                    fajitaBoy.getEmulator().enableAudio();
                }
            }
        };

    }

    /**
     * Initiates action map. Binds the input key strings to actions
     */
    private void initActionMap() {

        ActionMap am = gamePanel.getActionMap();

        // Pushers
        am.put("pushLeft", pushLeft);
        am.put("pushRight", pushRight);
        am.put("pushUp", pushUp);
        am.put("pushDown", pushDown);
        am.put("pushA", pushA);
        am.put("pushB", pushB);
        am.put("pushStart", pushStart);
        am.put("pushSelect", pushSelect);

        // Releasers
        am.put("releaseLeft", releaseLeft);
        am.put("releaseRight", releaseRight);
        am.put("releaseUp", releaseUp);
        am.put("releaseDown", releaseDown);
        am.put("releaseA", releaseA);
        am.put("releaseB", releaseB);
        am.put("releaseStart", releaseStart);
        am.put("releaseSelect", releaseSelect);

        // Appletators
        am.put("pushPause", pause);
        am.put("pushMenu", menu);
        am.put("pushFullscreen", fullscreen);
        am.put("pushMute", mute);
    }

    /**
     * Initiates the input map. Binds keys to input string thingies.
     */
    private void initInputMap() {

        // -- Pushers ----------------------------------------------------------

        setKey(KeyEvent.VK_LEFT, "Left");
        setKey(KeyEvent.VK_RIGHT, "Right");
        setKey(KeyEvent.VK_UP, "Up");
        setKey(KeyEvent.VK_DOWN, "Down");
        setKey(KeyEvent.VK_X, "A");
        setKey(KeyEvent.VK_Z, "B");
        setKey(KeyEvent.VK_ENTER, "Start");
        setKey(KeyEvent.VK_SHIFT, "Select");

        // -- Appletors --------------------------------------------------------

        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "pushMenu");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "pushPause");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false),
                "pushFullscreen");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, false), "pushMute");
    }

    /**
     * Sets a key to a certain action. Ie setKey(KeyEvent.VK_A,"Left") will bind
     * the key a to joypad left
     * 
     * @see setKey(int,String,boolean)
     * @param key Key pressed, taken from KeyEvent
     * @param actionMapKey The string for a certain action
     */
    public final void setKey(final int key, final String actionMapKey) {
        setKey(KeyStroke.getKeyStroke(key, 0, true), "release" + actionMapKey);
        setKey(KeyStroke.getKeyStroke(key, 0, false), "push" + actionMapKey);
    }

    /**
     * Sets a key to a certain action. Ie setKey(KeyEvent.VK_A,"Left", false)
     * will bind the key a to joypad left. The boolean determines if the action
     * triggers on release or on press
     * 
     * @see setKey(int,String)
     * @param key Key pressed, taken from KeyEvent
     * @param actionMapKey The string for a certain action
     */
    public final void setKey(final KeyStroke key, final String actionMapKey) {
        InputMap ip = gamePanel
                .getInputMap(JLayeredPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Unbinds the new key's previous binding.
        if (ip.size() != 0) {
            for (KeyStroke keyS : ip.keys()) {
                if (ip.get(keyS).toString().equals(actionMapKey)) {
                    ip.remove(keyS);
                }
            }
        }
        ip.remove(key);
        ip.put(key, actionMapKey);
    }

    /**
     * Set a key.
     * 
     * @param key ie A
     * @param actionMapKey ie Left
     */
    public final void setKey(final String key, final String actionMapKey) {
        setKey(KeyStroke.getKeyStroke("pressed " + key), "push" + actionMapKey);
        setKey(KeyStroke.getKeyStroke("released " + key), "release"
                + actionMapKey);
    }

    /**
     * Fetches the keyboard key bound to a fajitabutton.
     * 
     * @param mapValue string value of fajitabutton
     * @return keyboard key as string
     */
    public final String getKey(final String mapValue) {
        InputMap ip = gamePanel
                .getInputMap(JLayeredPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        String val = "push" + mapValue;
        for (KeyStroke k : ip.keys()) {
            if (ip.get(k).toString().equals(val.toString())) {

                return k.toString().substring(8).trim();
            }
        }
        return "None";

    }

    /**
     * Attempts to read keybindings from browser cookie.
     */
    public final void importKeys() {
        String cookie;

        cookie = cookieJar.get(COOKIE_KEYBIND);

        if (cookie == null) {
            return;
        }

        String[] keys = cookie.split(":");

        if (keys.length != 8) {
            return;
        }

        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "pushMenu");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "pushPause");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false),
                "pushFullscreen");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, false), "pushMute");

        setKey(keys[0], "Up");
        setKey(keys[1], "Down");
        setKey(keys[2], "Left");
        setKey(keys[3], "Right");
        setKey(keys[4], "A");
        setKey(keys[5], "B");
        setKey(keys[6], "Start");
        setKey(keys[7], "Select");

        keySettingsPanel.refreshLabels();
    }

    /**
     * Attempts to save keybindings to browser cookie.
     */
    public final void exportKeys() {
        String bindings;

        bindings = getKey("Up");
        bindings += ":" + getKey("Down");
        bindings += ":" + getKey("Left");
        bindings += ":" + getKey("Right");
        bindings += ":" + getKey("A");
        bindings += ":" + getKey("B");
        bindings += ":" + getKey("Start");
        bindings += ":" + getKey("Select");
        
        cookieJar.put(COOKIE_KEYBIND, bindings);

    }

    /**
     * Resets keys to default values.
     */
    public final void reset() {
        initInputMap();
    }
}