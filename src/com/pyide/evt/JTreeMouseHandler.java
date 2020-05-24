package com.pyide.evt;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
/**
 * This class is for listening to mouse events in the package explorer
 * @author Kevin Zhang
 */
public class JTreeMouseHandler implements MouseListener {

    /**
     * The action listener to call
     */
    private ActionListener action;

    /**
     * Constructor for this mouse handler
     * @param a An action listener to call
     */
    public JTreeMouseHandler(ActionListener a) {
        this.action=a;
    }
    /**
     * This is what happens when mouse is clicked
     * @param e The mouse event, this will give useful information
     */
    public void mouseClicked(MouseEvent e) {

        int clicks=e.getClickCount();

        //perform the action if it is a double click
        if(clicks==2) {
            action.actionPerformed(null);
        }

    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }
}