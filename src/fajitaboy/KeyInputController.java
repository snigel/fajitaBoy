package fajitaboy;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;

import fajitaboy.FajitaBoy.GameState;
import fajitaboy.memory.IO.JoyPad;

/**
 * Handles the key input.
 */
public class KeyInputController {

    /** Joypad object. */
    private JoyPad joypad;

    /** applet. */
    private FajitaBoy fajitaBoy;
    /** gamepanel. */
    private LayeredGamePanel gamePanel;

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
     * @param fb
     *            mainframe
     * @param lgp
     *            layeredgameframe
     * @param jp
     *            the JoyPad object
     * 
     */
    public KeyInputController(final FajitaBoy fb, final LayeredGamePanel lgp,
            final JoyPad jp) {
        fajitaBoy = fb;
        joypad = jp;
        gamePanel = lgp;

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
                joypad.setLeft(true);
            }
        };
        pushRight = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setRight(true);
            }
        };
        pushUp = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setUp(true);
            }
        };
        pushDown = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setDown(true);
            }
        };
        pushA = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setA(true);
            }
        };
        pushB = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setB(true);
            }
        };
        pushStart = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setStart(true);
            }
        };
        pushSelect = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setSelect(true);
            }
        };

        // -- Button releasing actions ----------------------------------------

        releaseLeft = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setLeft(false);
            }
        };
        releaseRight = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setRight(false);
            }
        };
        releaseUp = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setUp(false);
            }
        };
        releaseDown = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setDown(false);
            }
        };
        releaseA = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setA(false);
            }
        };
        releaseB = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setB(false);
            }
        };
        releaseStart = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setStart(false);
            }
        };
        releaseSelect = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                joypad.setSelect(false);
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
                // if (fajitaBoy.getOscillator().isAudioEnable()) {
                fajitaBoy.getOscillator().disableAudio();
                // } else {
                // fajitaBoy.getOscillator().enableAudio();
                // }
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

        setKey(KeyEvent.VK_ESCAPE, "Menu", false);
        setKey(KeyEvent.VK_P, "Pause", false);
        setKey(KeyEvent.VK_F4, "Fullscreen", false);
        setKey(KeyEvent.VK_M, "Mute", false);
    }

    /**
     * Sets a key to a certain action. Ie setKey(KeyEvent.VK_A,"Left") will bind
     * the key a to joypad left
     * 
     * @see setKey(int,String,boolean)
     * @param key
     *            Key pressed, taken from KeyEvent
     * @param actionMapKey
     *            The string for a certain action
     */
    public final void setKey(final int key, final String actionMapKey) {
        setKey(key, actionMapKey, false);
        setKey(key, actionMapKey, true);
    }

    /**
     * Sets a key to a certain action. Ie setKey(KeyEvent.VK_A,"Left", false)
     * will bind the key a to joypad left. The boolean determines if the action
     * triggers on release or on press
     * 
     * @see setKey(int,String)
     * @param key
     *            Key pressed, taken from KeyEvent
     * @param actionMapKey
     *            The string for a certain action
     * @param onRelease
     *            trigger on key released
     */
    public final void setKey(final int key, final String actionMapKey,
            final boolean onRelease) {
        InputMap ip = gamePanel
                .getInputMap(JLayeredPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        KeyStroke keyStroke = KeyStroke.getKeyStroke(key, 0, onRelease);

        String mapKey = (onRelease ? "release" : "push") + actionMapKey;

        // Unbinds the new keys previous binding.
        if (ip.size() != 0) {
            for (KeyStroke keyS : ip.keys()) {
                if (ip.get(keyS).toString().equals(mapKey.toString())) {
                    ip.remove(keyS);
                }
            }
        }
        ip.remove(keyStroke);
        ip.put(keyStroke, mapKey);

    }

    /**
     * Fetches the keyboard key bound to a fajitabutton.
     * 
     * @param mapValue
     *            string value of fajitabutton
     * @return keyboard key as string
     */
    public final String getKey(final String mapValue) {
        InputMap ip = gamePanel
                .getInputMap(JLayeredPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        String val = "push" + mapValue;
        for (KeyStroke k : ip.keys()) {
            if (ip.get(k).toString().equals(val.toString())) {
                return k.toString().substring(8);
            }
        }
        return "Unbound";

    }
}