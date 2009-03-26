package fajitaboy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;

import fajitaboy.FajitaBoy.GameState;

public class IngameMenuPanel extends JPanel implements ActionListener{
 
    FajitaBoy fajitaBoy;
    
    JButton newRom;
    JButton reset;
    JButton mainmenu;
    JButton resume; 
    
    public IngameMenuPanel(final FajitaBoy fb) {
       
        fajitaBoy = fb;
        setPreferredSize(new Dimension(150, 130));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setLayout(new GridLayout(4, 1));
        
        newRom = new JButton("Load new ROM");
        reset = new JButton("Reset ROM");
        mainmenu = new JButton("Quit to menu");
        resume = new JButton("Resume game");
        
        newRom.addActionListener(this);
        reset.addActionListener(this);
        mainmenu.addActionListener(this);
        resume.addActionListener(this);
        
        add(resume);
        add(newRom);
        add(reset);
        add(mainmenu);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource() == newRom) {
            fajitaBoy.changeGameState(GameState.SINGLEPLAYER_LOADSCREEN);
        } else  if (e.getSource() == reset) {
            fajitaBoy.resetEmulator();
            fajitaBoy.changeGameState(GameState.PLAYGAME);
        } else  if (e.getSource() == mainmenu) {
            fajitaBoy.changeGameState(GameState.STARTSCREEN);
        } else  if (e.getSource() == resume) {
            fajitaBoy.changeGameState(GameState.PLAYGAME);
        } 
    }

}
