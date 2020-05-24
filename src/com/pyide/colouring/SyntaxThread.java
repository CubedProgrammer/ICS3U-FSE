package com.pyide.colouring;
import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * This class is for syntax colouring
 * @author Kevin Zhang
 */
public class SyntaxThread implements Runnable {

    /**
     * The array of key words
     */
    public static final String[]keyWords={"False","None","True","and","as","assert","break","class","continue","def","del","do","elif","else","except","finally","for","from","global","if","import","in","is","lambda","nonlocal","not","or","pass","raise","return","self","try","while","with","yield"};
    /**
     * The set of methods
     */
    public HashSet<String> methods;
    /**
     * The set of methods
     */
    public HashSet<String> classes;
    /**
     * The default style
     */
    public Style norm;
    /**
     * Style for key words
     */
    public Style keyWord;
    /**
     * The style for numbers
     */
    public Style num;
    /**
     * The style for strings
     */
    public Style str;
    /**
     * The style for methods
     */
    public Style mthd;
    /**
     * The style for classes
     */
    public Style classs;
    /**
     * The style for comments
     */
    public Style comments;
    /**
     * The document
     */
    private volatile StyledDocument doc;
    /**
     * The text pane containing the editors
     */
    private volatile JTabbedPane pane;

    /**
     * Constructor for syntax thread
     * @param pane The tabbed pane to syntax colour
     */
    public SyntaxThread(JTabbedPane pane) {

        this.pane=pane;
        this.methods=new HashSet<String>();
        this.classes=new HashSet<String>();

    }

    /**
     * Sets the tabbed pane
     * @param pane The tabbed pane to set to
     */
    public synchronized void setTabbedPane(JTabbedPane pane) {
        this.pane=pane;
    }

    /**
     * The run method for this thread
     */
    public void run() {

        try {

            if(pane.getComponentCount()>0) {

            	//creates the document and the styles
                this.doc=((JTextPane)((JScrollPane)pane.getSelectedComponent()).getViewport().getView()).getStyledDocument();
                this.keyWord=doc.addStyle("key words",null);
                this.norm=doc.addStyle("default",null);
                this.num=doc.addStyle("numbers",null);
                this.str=doc.addStyle("strings",null);
                this.mthd=doc.addStyle("methods",null);
                this.classs=doc.addStyle("classes",null);
                this.comments=doc.addStyle("comments",null);
                //gets the text from document and sets the default style
                String s=doc.getText(0,doc.getLength());

                //initializes all styles
                StyleConstants.setForeground(keyWord,new Color(140,70,30));
                StyleConstants.setFontFamily(keyWord,"Consolas");
                StyleConstants.setFontSize(keyWord,12);
                StyleConstants.setForeground(norm,new Color(18,18,18));
                StyleConstants.setFontFamily(norm,"Consolas");
                StyleConstants.setFontSize(norm,12);
                StyleConstants.setForeground(num,new Color(30,180,50));
                StyleConstants.setFontFamily(num,"Consolas");
                StyleConstants.setFontSize(num,12);
                StyleConstants.setForeground(str,new Color(120,140,80));
                StyleConstants.setFontFamily(str,"Consolas");
                StyleConstants.setFontSize(str,12);
                StyleConstants.setForeground(mthd,new Color(40,50,180));
                StyleConstants.setItalic(mthd,true);
                StyleConstants.setFontFamily(mthd,"Consolas");
                StyleConstants.setFontSize(mthd,12);
                StyleConstants.setForeground(classs,new Color(15,90,20));
                StyleConstants.setItalic(classs,true);
                StyleConstants.setFontFamily(classs,"Consolas");
                StyleConstants.setFontSize(classs,12);
                StyleConstants.setForeground(comments,new Color(120,112,112));
                StyleConstants.setFontFamily(comments,"Consolas");
                StyleConstants.setFontSize(comments,12);

                //loops through the string
                for(int i=0;i<s.length();i++) {

                	//if the char is a digit
                    if(s.charAt(i)>=48&&s.charAt(i)<=57) {

                    	//if there is no letter before it
                        if(i==0||s.charAt(i-1)>90&&s.charAt(i-1)<97||s.charAt(i-1)>122||s.charAt(i-1)<65) {

                        	//nor after it
                            if(i==s.length()-1||s.charAt(i+1)>90&&s.charAt(i+1)<97||s.charAt(i+1)>122||s.charAt(i+1)<65) {
                                doc.setCharacterAttributes(i,1,num,false);//color the number
                            }

                        }

                    }

                    //if there is a space before the character
                    if(i==0||s.charAt(i-1)=='\n'||s.charAt(i-1)==' ') {

                    	//loops through keywords
                        for(int j=0;j<keyWords.length;j++) {

                        	//if keyword length does not reach document length
                            if(i+keyWords[j].length()<s.length()) {

                            	//if there is a match
                                if(s.substring(i,i+keyWords[j].length()).equals(keyWords[j])) {

                                	//and there is a space after
                                    if(i+keyWords[j].length()==s.length()||s.charAt(i+keyWords[j].length())==' '||s.charAt(i+keyWords[j].length())=='\n') {

                                    	//colors the keyword
                                        doc.setCharacterAttributes(i,keyWords[j].length(),keyWord,false);
                                        //resets the style
                                        if(i+keyWords[j].length()<s.length())doc.setCharacterAttributes(i+keyWords[j].length(),1,norm,true);

                                        //if keyword is def
                                        if(keyWords[j].equals("def")&&s.length()>4) {

                                        	//add the function it defines to set of methods
                                            int k;
                                            for(k=i+4;k<s.length()&&(!(s.charAt(k)>90&&s.charAt(k)<97||s.charAt(k)>122||s.charAt(k)<65)||s.charAt(k)=='_');k++);
                                            methods.add(s.substring(i+4,k));

                                        } else if(keyWords[j].equals("class")&&s.length()>6) {

                                        	//adds the class it defines to the set of classes, if the keyword is class
                                            int k;
                                            for(k=i+6;k<s.length()&&(!(s.charAt(k)>90&&s.charAt(k)<97||s.charAt(k)>122||s.charAt(k)<65)||s.charAt(k)=='_');k++);
                                            classes.add(s.substring(i+6,k));

                                        }

                                    }

                                }

                            }

                        }

                    }

                }

                //iterate through methods
                Iterator<String>iter=methods.iterator();
                String m;

                //loops using iterator
                while(iter.hasNext()) {

                	//gets the next method
                    m=iter.next();
                    int ind=s.indexOf(m);

                    //loops through string and colors the methods
                    while(ind>0) {

                        doc.setCharacterAttributes(ind,m.length(),mthd,true);
                        if(ind+m.length()<s.length())doc.setCharacterAttributes(ind+m.length(),1,norm,true);//resets the style
                        ind=s.indexOf(m,ind+1);

                    }

                }

                //iterator for classes
                iter=classes.iterator();

                //loop using iterator
                while(iter.hasNext()) {

                	//get the class name
                    m=iter.next();
                    int ind=s.indexOf(m);

                    //iterate through string
                    while(ind>0) {

                    	//color all class names
                        doc.setCharacterAttributes(ind,m.length(),classs,true);
                        if(ind+m.length()<s.length())doc.setCharacterAttributes(ind+m.length(),1,norm,true);//resets the style
                        ind=s.indexOf(m,ind+1);

                    }

                }

                //loops through the text
                for(int i=0;i<s.length();i++) {

                	//if there is a quote
                    if(s.charAt(i)=='"') {

                    	//loop to find matching quote
                        for(int j=i+1;j<s.length();j++) {

                        	//if there is a matching quote
                            if(s.charAt(j)=='"') {

                            	//set the color to string color and break
                                doc.setCharacterAttributes(i,j-i+1,str,false);
                                i=j+1;
                                break;

                            }

                        }

                    }

                }

                //iterate through text
                for(int i=0;i<s.length();i++) {

                	//if there is a hashtag
                    if(s.charAt(i)=='#') {

                    	//color the whole line
                    	int end=s.indexOf('\n',i);
                        doc.setCharacterAttributes(i,end==-1?s.length()-i:end-i,comments,true);
                        i=s.length();

                    }

                }

            }

            //wait three seconds before recursively updating the syntax colour
            Thread.sleep(3000);
            this.run();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}