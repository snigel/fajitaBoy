package fajitaboy;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import fajitaboy.FajitaBoy.GameState;
import fajitaboy.memory.IO.JoyPad;

/**
 * Handles the key input.
 */
public class KeyInputController implements KeyListener {

    /**
     * joypad object.
     */
    private JoyPad jp;

    private FajitaBoy fajitaBoy;

    /**
     * Creates a new KeyInputController object that handles key input.
     * 
     * @param joypad
     *            the JoyPad object
     */
    public KeyInputController(final FajitaBoy fb, final JoyPad joypad) {
        fajitaBoy = fb;
        jp = joypad;

    }

    /**
     * {@inheritDoc}
     */
    public final void keyPressed(final KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
            jp.setDown(true);
            break;
        case KeyEvent.VK_UP:
            jp.setUp(true);
            break;
        case KeyEvent.VK_LEFT:
            jp.setLeft(true);
            break;
        case KeyEvent.VK_RIGHT:
            jp.setRight(true);
            break;
        case KeyEvent.VK_X:
            jp.setA(true);
            break;
        case KeyEvent.VK_Z:
            jp.setB(true);
            break;
        case KeyEvent.VK_ENTER:
            jp.setStart(true);
            break;
        case KeyEvent.VK_SHIFT:
            jp.setSelect(true);
            break;

        case KeyEvent.VK_P:
            if (fajitaBoy.getGameState() == GameState.PLAYGAME) {
                fajitaBoy.changeGameState(GameState.PAUSE);
            } else {
                fajitaBoy.changeGameState(GameState.PLAYGAME);
            }
            break;

        case KeyEvent.VK_ESCAPE:
            if (fajitaBoy.getGameState() == GameState.INGAME_MENU) {
                fajitaBoy.changeGameState(GameState.PLAYGAME);
            } else {
                fajitaBoy.changeGameState(GameState.INGAME_MENU);
            }
            break;

        default: /* Nothing */
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void keyReleased(final KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
            jp.setDown(false);
            break;
        case KeyEvent.VK_UP:
            jp.setUp(false);
            break;
        case KeyEvent.VK_LEFT:
            jp.setLeft(false);
            break;
        case KeyEvent.VK_RIGHT:
            jp.setRight(false);
            break;
        case KeyEvent.VK_X:
            jp.setA(false);
            break;
        case KeyEvent.VK_Z:
            jp.setB(false);
            break;
        case KeyEvent.VK_ENTER:
            jp.setStart(false);
            break;
        case KeyEvent.VK_SHIFT:
            jp.setSelect(false);
            break;

        default: /* Nothing */
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void keyTyped(final KeyEvent e) {

    }
}
