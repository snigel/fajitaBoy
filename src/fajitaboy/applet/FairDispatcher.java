package fajitaboy.applet;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;

/**
 * Dispatcher for use with a KeyboardFocusManager. 
 * Makes sure all the pressed and released actions are executed,
 * even for special keys like shift.
 */
public class FairDispatcher implements KeyEventDispatcher {
	
	public boolean dispatchKeyEvent(KeyEvent e) {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

		if (focusOwner instanceof JComponent) {
			return dispatchKeyEvent(e, (JComponent) focusOwner);
		}
		
		return false;
	}
	
	private boolean dispatchKeyEvent(KeyEvent e, JComponent comp) {
		
		if (comp == null) {
			return false;
		}
		
		boolean pressed;
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			pressed = true;
		} else if (e.getID() == KeyEvent.KEY_RELEASED) {
			pressed = false;
		} else {
			// treat as usual
			return false;
		}
    	
    	ActionMap actionMap = comp.getActionMap();
    	InputMap inputMap = comp.getInputMap(JLayeredPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    	
    	KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyCode(), 0, !pressed);
    	Object binding = inputMap.get(keyStroke);
    	if (binding != null) {
    		Action action =  actionMap.get(binding);
			
	    	if(action != null) {
	    		// Action found
	    		ActionEvent actionEvent = new ActionEvent(e, 0, null);
	    		action.actionPerformed(actionEvent);
	    		e.consume();
	    		return true;
	    	}
    	}
    	
    	// No action found. Look in parent container.
    	Component parent = comp.getParent();
    	if(parent instanceof JComponent ) {
    		return dispatchKeyEvent(e, (JComponent) parent);
    	}
    	
    	// no action found at all
		return false;
	}

}