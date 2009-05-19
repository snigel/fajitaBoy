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

    /** cookies. */
    private CookieJar cookieJar;

    /**
     * Enum for controller buttons.
     */
    public enum ControllerButton {
        P1LEFT, P1RIGHT, P1UP, P1DOWN, P1A, P1B, P1START, P1SELECT, P2LEFT, P2RIGHT, P2UP, P2DOWN, P2A, P2B, P2START, P2SELECT
    }

    // -- Button pushing actions
    private Action pushLeftPlayer1, pushRightPlayer1, pushUpPlayer1,
            pushDownPlayer1;
    private Action pushAPlayer1, pushBPlayer1, pushSelectPlayer1,
            pushStartPlayer1;
    private Action pushLeftPlayer2, pushRightPlayer2, pushUpPlayer2,
            pushDownPlayer2;
    private Action pushAPlayer2, pushBPlayer2, pushSelectPlayer2,
            pushStartPlayer2;

    // -- Button releasing actions
    private Action releaseLeftPlayer1, releaseRightPlayer1, releaseUpPlayer1,
            releaseDownPlayer1;
    private Action releaseAPlayer1, releaseBPlayer1, releaseSelectPlayer1,
            releaseStartPlayer1;
    private Action releaseLeftPlayer2, releaseRightPlayer2, releaseUpPlayer2,
            releaseDownPlayer2;
    private Action releaseAPlayer2, releaseBPlayer2, releaseSelectPlayer2,
            releaseStartPlayer2;

    // -- Applet actions
    private Action pause, menu, fullscreen, mute;

    public KeyInputController() {
    }

    /**
     * Creates a new KeyInputController object that handles key input.
     * 
     * @param fb mainframe
     * @param lgp layeredgameframe
     * @param emu emulator
     */
    public KeyInputController(final FajitaBoy fb, final LayeredGamePanel lgp,
            final Emulator emu) {
        fajitaBoy = fb;
        emulator = emu;
        gamePanel = lgp;
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

        pushLeftPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.LEFT, true,
                        Emulator.Player.PLAYER1);
            }
        };
        pushRightPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.RIGHT, true,
                        Emulator.Player.PLAYER1);
            }
        };
        pushUpPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.UP, true, Emulator.Player.PLAYER1);
            }
        };
        pushDownPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.DOWN, true,
                        Emulator.Player.PLAYER1);
            }
        };
        pushAPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.A, true, Emulator.Player.PLAYER1);
            }
        };
        pushBPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.B, true, Emulator.Player.PLAYER1);
            }
        };
        pushStartPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.START, true,
                        Emulator.Player.PLAYER1);
            }
        };
        pushSelectPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.SELECT, true,
                        Emulator.Player.PLAYER1);
            }
        };

        pushLeftPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.LEFT, true,
                        Emulator.Player.PLAYER2);
            }
        };
        pushRightPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.RIGHT, true,
                        Emulator.Player.PLAYER2);
            }
        };
        pushUpPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator
                        .setKey(Emulator.Keys.UP, true, Emulator.Player.PLAYER2);
            }
        };
        pushDownPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.DOWN, true,
                        Emulator.Player.PLAYER2);
            }
        };
        pushAPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.A, true, Emulator.Player.PLAYER2);
            }
        };
        pushBPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.B, true, Emulator.Player.PLAYER2);
            }
        };
        pushStartPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.START, true,
                        Emulator.Player.PLAYER2);
            }
        };
        pushSelectPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.SELECT, true,
                        Emulator.Player.PLAYER2);
            }
        };

        // -- Button releasing actions ----------------------------------------

        releaseLeftPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.LEFT, false,
                        Emulator.Player.PLAYER1);
            }
        };
        releaseRightPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.RIGHT, false,
                        Emulator.Player.PLAYER1);
            }
        };
        releaseUpPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.UP, false,
                        Emulator.Player.PLAYER1);
            }
        };
        releaseDownPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.DOWN, false,
                        Emulator.Player.PLAYER1);
            }
        };
        releaseAPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator
                        .setKey(Emulator.Keys.A, false, Emulator.Player.PLAYER1);
            }
        };
        releaseBPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator
                        .setKey(Emulator.Keys.B, false, Emulator.Player.PLAYER1);
            }
        };
        releaseStartPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.START, false,
                        Emulator.Player.PLAYER1);
            }
        };
        releaseSelectPlayer1 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.SELECT, false,
                        Emulator.Player.PLAYER1);
            }
        };

        releaseLeftPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.LEFT, false,
                        Emulator.Player.PLAYER2);
            }
        };
        releaseRightPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.RIGHT, false,
                        Emulator.Player.PLAYER2);
            }
        };
        releaseUpPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.UP, false,
                        Emulator.Player.PLAYER2);
            }
        };
        releaseDownPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.DOWN, false,
                        Emulator.Player.PLAYER2);
            }
        };
        releaseAPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator
                        .setKey(Emulator.Keys.A, false, Emulator.Player.PLAYER2);
            }
        };
        releaseBPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator
                        .setKey(Emulator.Keys.B, false, Emulator.Player.PLAYER2);
            }
        };
        releaseStartPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.START, false,
                        Emulator.Player.PLAYER2);
            }
        };
        releaseSelectPlayer2 = new AbstractAction() {
            public void actionPerformed(final ActionEvent e) {
                emulator.setKey(Emulator.Keys.SELECT, false,
                        Emulator.Player.PLAYER2);
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

        // Pushers Player 1
        am.put("pushLeftP1", pushLeftPlayer1);
        am.put("pushRightP1", pushRightPlayer1);
        am.put("pushUpP1", pushUpPlayer1);
        am.put("pushDownP1", pushDownPlayer1);
        am.put("pushAP1", pushAPlayer1);
        am.put("pushBP1", pushBPlayer1);
        am.put("pushStartP1", pushStartPlayer1);
        am.put("pushSelectP1", pushSelectPlayer1);

        // Releasers Player 1
        am.put("releaseLeftP1", releaseLeftPlayer1);
        am.put("releaseRightP1", releaseRightPlayer1);
        am.put("releaseUpP1", releaseUpPlayer1);
        am.put("releaseDownP1", releaseDownPlayer1);
        am.put("releaseAP1", releaseAPlayer1);
        am.put("releaseBP1", releaseBPlayer1);
        am.put("releaseStartP1", releaseStartPlayer1);
        am.put("releaseSelectP1", releaseSelectPlayer1);

        // Pushers Player 1
        am.put("pushLeftP2", pushLeftPlayer2);
        am.put("pushRightP2", pushRightPlayer2);
        am.put("pushUpP2", pushUpPlayer2);
        am.put("pushDownP2", pushDownPlayer2);
        am.put("pushAP2", pushAPlayer2);
        am.put("pushBP2", pushBPlayer2);
        am.put("pushStartP2", pushStartPlayer2);
        am.put("pushSelectP2", pushSelectPlayer2);

        // Releasers Player 1
        am.put("releaseLeftP2", releaseLeftPlayer2);
        am.put("releaseRightP2", releaseRightPlayer2);
        am.put("releaseUpP2", releaseUpPlayer2);
        am.put("releaseDownP2", releaseDownPlayer2);
        am.put("releaseAP2", releaseAPlayer2);
        am.put("releaseBP2", releaseBPlayer2);
        am.put("releaseStartP2", releaseStartPlayer2);
        am.put("releaseSelectP2", releaseSelectPlayer2);

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

        setKey(KeyEvent.VK_A, "LeftP1");
        setKey(KeyEvent.VK_D, "RightP1");
        setKey(KeyEvent.VK_W, "UpP1");
        setKey(KeyEvent.VK_S, "DownP1");
        setKey(KeyEvent.VK_E, "AP1");
        setKey(KeyEvent.VK_Q, "BP1");
        setKey(KeyEvent.VK_X, "StartP1");
        setKey(KeyEvent.VK_Z, "SelectP1");

        setKey(KeyEvent.VK_LEFT, "LeftP2");
        setKey(KeyEvent.VK_RIGHT, "RightP2");
        setKey(KeyEvent.VK_UP, "UpP2");
        setKey(KeyEvent.VK_DOWN, "DownP2");
        setKey(KeyEvent.VK_L, "AP2");
        setKey(KeyEvent.VK_K, "BP2");
        setKey(KeyEvent.VK_I, "StartP2");
        setKey(KeyEvent.VK_O, "SelectP2");

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

        if (keys.length != 18) {
            return;
        }

        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "pushMenu");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "pushPause");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false),
                "pushFullscreen");
        setKey(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0, false), "pushMute");

        setKey(keys[0], "UpP1");
        setKey(keys[1], "DownP1");
        setKey(keys[2], "LeftP1");
        setKey(keys[3], "RightP1");
        setKey(keys[4], "AP1");
        setKey(keys[5], "BP1");
        setKey(keys[6], "StartP1");
        setKey(keys[7], "SelectP1");
        setKey(keys[8], "UpP2");
        setKey(keys[9], "DownP2");
        setKey(keys[10], "LeftP2");
        setKey(keys[11], "RightP2");
        setKey(keys[12], "AP2");
        setKey(keys[13], "BP2");
        setKey(keys[14], "StartP2");
        setKey(keys[15], "SelectP2");
        setKey(keys[16], "Pause");
        setKey(KeyStroke.getKeyStroke("pressed " + keys[16]), "pushPause");
        setKey(KeyStroke.getKeyStroke("pressed " + keys[17]), "pushMute");

        fajitaBoy.refreshLabels();
    }

    /**
     * Attempts to save keybindings to browser cookie.
     */
    public void exportKeys() {
        String bindings;

        bindings = getKey("UpP1");
        bindings += ":" + getKey("DownP1");
        bindings += ":" + getKey("LeftP1");
        bindings += ":" + getKey("RightP1");
        bindings += ":" + getKey("AP1");
        bindings += ":" + getKey("BP1");
        bindings += ":" + getKey("StartP1");
        bindings += ":" + getKey("SelectP1");
        bindings += ":" + getKey("UpP2");
        bindings += ":" + getKey("DownP2");
        bindings += ":" + getKey("LeftP2");
        bindings += ":" + getKey("RightP2");
        bindings += ":" + getKey("AP2");
        bindings += ":" + getKey("BP2");
        bindings += ":" + getKey("StartP2");
        bindings += ":" + getKey("SelectP2");
        bindings += ":" + getKey("Pause");
        bindings += ":" + getKey("Mute");

        cookieJar.put(COOKIE_KEYBIND, bindings);

    }

    /**
     * Resets keys to default values.
     */
    public void reset() {
        initInputMap();
    }
}