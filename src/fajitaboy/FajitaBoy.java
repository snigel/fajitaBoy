package fajitaboy;

import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

/**
 * @author Marcus Johansson, Peter Olsson
 * 
 */
public class FajitaBoy extends JApplet implements ActionListener, KeyListener {

    /** Prefered applet size. */
    private Dimension frameSize = new Dimension(400, 400);

    /** -pling- FaHIta */
    private AudioClip bootSound;

    private String romPath;

    private JFileChooser fileChooser;
    private JTextField fileStringField;

    /** Srs bsns. */
    private static final long serialVersionUID = 615741205610901754L;
    private GameState gameState;
    JPanel startScreen;
    JPanel singleplayerLoadscreen;

    private enum GameState {
        STARTSCREEN, SINGLEPLAYER_LOADSCREEN
    }

    public void init() {
        singleplayerLoadscreen = new JPanel();

        romPath = "/.";
        fileChooser = new JFileChooser(System.getProperty("user.home"));
        // fileChooser.

        initStartScreen();
        initSingleplayerScreen();

        resize(frameSize);
        bootSound = getAudioClip(getCodeBase(), "bootsound_mockup.wav");

        gameState = GameState.STARTSCREEN;
        setContentPane(startScreen);
        getContentPane().validate();
    }

    public void start() {
        bootSound.play();

    }

    public void stop() {

    }

    public void destroy() {

    }

    private void initStartScreen() {
        startScreen = new JPanel();
        JButton singleplayerButton = new JButton("Singleplayer");
        JButton multiplayerButton = new JButton("Multiplayer");

        singleplayerButton.addActionListener(this);
        multiplayerButton.addActionListener(this);

        singleplayerButton.setActionCommand("startToSingleButton");
        multiplayerButton.setActionCommand("startToMultiButton");

        startScreen.add(singleplayerButton);
        startScreen.add(multiplayerButton);
    }

    private void initSingleplayerScreen() {
        singleplayerLoadscreen = new JPanel();
        JButton loadFileButton = new JButton("Load ROM");
        JButton startGameButton = new JButton("Start Game");
        fileStringField = new JTextField(20);

        loadFileButton.addActionListener(this);
        startGameButton.addActionListener(this);
        fileStringField.addActionListener(this);

        loadFileButton.setActionCommand("loadROMSingle");
        startGameButton.setActionCommand("startSingleGame");
        fileStringField.setActionCommand("fileStringEvent");

        fileStringField.setText(romPath);

        singleplayerLoadscreen.add(fileStringField);
        singleplayerLoadscreen.add(loadFileButton);
        singleplayerLoadscreen.add(startGameButton);
    }

    private void changeGameState(GameState state) {
        if (state == gameState)
            return;

        switch (state) {
        case STARTSCREEN:
            setContentPane(startScreen);
            break;

        case SINGLEPLAYER_LOADSCREEN:
            setContentPane(singleplayerLoadscreen);
            break;
        }
        gameState = state;
        validate();

    }

    // ----------------------------------------------------------------------//
    // Listeners

    public void actionPerformed(ActionEvent e) {
        showStatus("A-A-ACTION! :D");

        bootSound.play();
        if (e.getActionCommand().equals("startToSingleButton")) {
            changeGameState(GameState.SINGLEPLAYER_LOADSCREEN);
        } else if (e.getActionCommand().equals("startToMultiButton")) {

        } else if (e.getActionCommand().equals("loadROMSingle")) {
            File file;
            int retVal = fileChooser.showOpenDialog(this);

            if (retVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try {
                    romPath = file.getCanonicalPath();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block

                    e1.printStackTrace();
                }
                fileStringField.setText(romPath);
            }

        } else if (e.getActionCommand().equals("startSingleGame")) {
            // startGame();
        } else if (e.getActionCommand().equals("fileStringEvent")) {

        }
    }

    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        showStatus("KEYPRESS! :D");

    }

    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        showStatus("KEYRELEASE! :D");

    }

    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        showStatus("KEYTYPE! :D");

    }

}
