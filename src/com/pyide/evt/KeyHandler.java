package com.pyide.evt;
import static java.lang.Math.max;
import com.pyide.main.AppMain;
import com.pyide.tries.Trie;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
/**
 * This class is responsible for listening to key presses
 * @author Kevin Zhang
 */
public class KeyHandler implements KeyListener {

    /**
     * The app to use and modify
     */
    private AppMain app;
    /**
     * The trie of words for suggestions
     */
    private Trie words;
    /**
     * How many characters have been typed
     */
    private int typed;

    /**
     * This is the constructor for the key listener
     * @param app The editor to modify.
     */
    public KeyHandler(AppMain app) {

    	//initializes the app and words
        this.app=app;
        this.words=new Trie();

        try {

        	//read from resource
            BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(getClass().getResourceAsStream("/suggestions.txt"))));
            String s;

            //load the words to the trie
            while((s=reader.readLine())!=null)
                words.addWord(s);

            //close the reader
            reader.close();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Detects when a char is typed and performs an action
     */
    public void keyTyped(KeyEvent e) {

        JTextPane editor=app.getSelectedEditor();

        if(editor!=null) {

            try {

                if(e.getKeyChar()==10) {

                	//don't show suggestions
                    app.showSuggestions(false);

                    //gets caret position, the line, and then count spaces
                    int pos=editor.getCaretPosition();
                    int lnstart=editor.getText(0,pos-1).lastIndexOf('\n')+1;
                    int spaces=editor.getText(lnstart,pos-lnstart-2).length()-editor.getText(lnstart,pos-lnstart-2).trim().length();
                    //adds four extra spaces if there is a colon
                    String s=editor.getText(editor.getText(0,pos).lastIndexOf('\n')-1,1).equals(":")?"    ":"";

                    //adds this many spaces
                    for(int i=0;i<spaces;i++)
                        s+=" ";

                    //insert to document
                    editor.getDocument().insertString(pos,s,editor.getLogicalStyle());

                } else if(e.getKeyChar()==32)
                    app.showSuggestions(false);//don't show suggestions if space is typed
                else if(((e.getKeyChar()>=65&&e.getKeyChar()<=90)||(e.getKeyChar()>=97&&e.getKeyChar()<=122)||e.getKeyChar()=='_')&&editor.getText().length()-2>editor.getCaretPosition()) {

                	//turn off suggestions
                    app.showSuggestions(false);
                    //get the text and adds the key that was typed
                    String s=editor.getText();
                    s+=String.valueOf(e.getKeyChar());

                    //gets the last whitespace index
                    int sp=s.substring(0,editor.getCaretPosition()+1).lastIndexOf(32);
                    sp=max(sp,s.substring(0,editor.getCaretPosition()+1).lastIndexOf(10));
                    //gets the prefix and the words that match it
                    String pr=s.substring(sp+1,editor.getCaretPosition()+2);
                    String[]sugs=words.match(pr);

                    //add the suggestions
                    for(String sug:sugs) {
                        app.addSuggestion(sug);
                    }

                    //and show the suggestions
                    app.showSuggestions(true);

                } else if(e.getKeyChar()==9) {

                	//if tab is typed
                	//gets the highlighted text
                    String txt=editor.getSelectedText();

                    if(txt==null)
                        editor.getDocument().insertString(editor.getCaretPosition(),"    ",editor.getLogicalStyle());//adds four spaces if there is no selected text
                    else {

                        try {

                        	//gets the document and selection start
                            StyledDocument doc=editor.getStyledDocument();
                            int start=editor.getSelectionStart();
                            //adds four spaces at the start and shift start right by four
                            doc.insertString(start,"    ",doc.getLogicalStyle(start));
                            start+=4;

                            //loops through selected text
                            for(int i=txt.length()-1;i>=0;i--) {

                            	//add four spaces after line separator
                                if(txt.charAt(i)=='\n')
                                    doc.insertString(start+i+1,"    ",doc.getLogicalStyle(start+i+1));

                            }

                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                }

                //adds one to the amount of characters typed
                typed++;

                //if more than 50 characters have been typed
                if(typed>50) {

                    //add new undo text data
                    app.addUndoText(editor.getText());
                    //and reset typed
                    typed-=50;

                }


            } catch(BadLocationException ex) {
                ex.printStackTrace();
            }

        }

    }

    public void keyPressed(KeyEvent e) {}

    /**
     * Detects when a key is released and performs that action
     */
    public void keyReleased(KeyEvent e) {

        int key=e.getKeyCode();

        if(key==KeyEvent.VK_F11)
            app.runProject(null);//hotkey for running project
        else if(key==KeyEvent.VK_F11&&e.isControlDown())
            app.compileProject(null);//hotkey for compiling project
        else if(key==KeyEvent.VK_S&&e.isControlDown())
            app.saveProject(null);//hotkey for saving project
        else if(key==KeyEvent.VK_F&&e.isControlDown()&&e.isShiftDown())
            app.fori(null);//hotkey for for loop
        else if(key==KeyEvent.VK_W&&e.isControlDown()&&e.isShiftDown())
            app.winding(null);//hotkey for winding function
        else if(key==KeyEvent.VK_R&&e.isControlDown()&&e.isShiftDown())
            app.rotating(null);//hotkey for rotating an array
        else if(key==KeyEvent.VK_N&&e.isControlDown()&&e.isShiftDown())
            app.nextWord(null);//hotkey for reading next word
        else if(key==KeyEvent.VK_L&&e.isControlDown()&&e.isShiftDown())
            app.loop(null);//hotkey for game loop
        else if(key==KeyEvent.VK_A&&e.isControlDown()&&e.isShiftDown())
            app.runArguments(null);//hotkey for arguments
        else if(key==KeyEvent.VK_K&&e.isControlDown()&&e.isShiftDown())
            app.killProcess(null);//hotkey for killing process
        else if(key==88&&e.isAltDown()) {

            if(e.isShiftDown())
                app.closeAllTabs(null);//hotkey for closing all tabs
            else
                app.closeCurrentTab(null);//hotkey for closing one tab

        } else if(key==85&&e.isControlDown()&&e.isShiftDown())
            app.comment(null);//hotkey for commenting a block
        else if(key==86&&e.isControlDown()&&e.isShiftDown())
            app.uncomment(null);//hotkey for uncommenting a block
        else if(key==90&&e.isControlDown())
            app.undo(null);//hotkey for undo
        else if(key==89&&e.isControlDown())
            app.redo(null);//hotkey for deleting your code
        else if(key==70&&e.isControlDown())
            app.findtxt(null);//hotkey for find

    }

}