package com.pyide.main;
import static java.lang.Math.max;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import com.pyide.colouring.SyntaxThread;
import com.pyide.evt.JTreeMouseHandler;
import com.pyide.evt.KeyHandler;
import com.pyide.evt.WindowHandler;
/**
 * This is the main class
 * This class contains all the components used in this program
 * @author Kevin Zhang
 */
public class AppMain {

    /**
     * The project currently being worked on
     */
    private File project;
    /**
     * The package explorer
     */
    private JScrollPane explorer;
    /**
     * The console output
     */
    private volatile JScrollPane output;
    /**
     * The console input
     */
    private volatile JScrollPane input;
    /**
     * The process currently running
     */
    private volatile Process p;
    /**
     * The tabs with the file editors
     */
    private JTabbedPane editors;
    /**
     * Contains the tab names
     */
    private LinkedList<String>tabs;
    /**
     * Contains data for undoing
     */
    private LinkedList<LinkedList<String>>undos;
    /**
     * The component for the package explorer.
     */
    private JTree packages;
    /**
     * Package names
     */
    private LinkedList<String>names;
    /**
     * Code syntax colouring for the editor
     */
    private volatile SyntaxThread syntax;
    /**
     * The key handler used for listening to key events
     */
    private KeyHandler keys;
    /**
     * The popup menu used for the suggestions
     */
    private JPopupMenu suggest;
    /**
     * Are the suggestions on
     */
    private boolean suggestionsOn;
    /**
     * Constructs my app, the window, and everything
     */
    public AppMain(String path) {

    	//if there is a path
        if(path.length()>0) {

            try {

            	//opens up the project file
                File f=new File(path);
                BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(f))));
                //reads the name and validity check
                String check=reader.readLine();
                String name=reader.readLine();
                //closes the reader
                reader.close();

                //the project folder and the package explorer
                this.project=f.getParentFile();
                this.explorer=new JScrollPane(packages);
                //if this is not a valid project
                if(!project.getName().equals(name)||!check.equals("ICS3U FSE May 12th, 2019 by Kevin Zhang"))
                    this.project=null;//cancel opening project
                else
                    loadProject();//otherwise, load project

            } catch(IOException e) {
                e.printStackTrace();
            }

        } else {
        	
            this.packages=new JTree(new DefaultMutableTreeNode("Nothing Here"));
            this.names=new LinkedList<String>();
            
        }

        //initializes fields
        this.explorer=new JScrollPane(packages);
        this.output=new JScrollPane();
        this.editors=new JTabbedPane();
        this.tabs=new LinkedList<String>();
        this.undos=new LinkedList<>();
        this.syntax=new SyntaxThread(editors);
        this.keys=new KeyHandler(this);
        this.suggest=new JPopupMenu();
        this.suggestionsOn=true;

        //thread for syntax colouring
        Thread thread=new Thread(syntax);
        thread.start();

        //all the components needed
        JPanel panel=new JPanel();
        JMenuBar tools=new JMenuBar();
        JMenu files=new JMenu("File");
        JMenu edit=new JMenu("Edit");
        JMenu compile=new JMenu("Compile or Run");
        JMenu help=new JMenu("Help");
        JMenu templates=new JMenu("Templates");
        JMenu create=new JMenu("New");
        JMenuItem run=new JMenuItem("Run F11");
        JMenuItem comp=new JMenuItem("Compile Ctrl+F11");
        JMenuItem args=new JMenuItem("Arguments Ctrl+Shift+A");
        JMenuItem kill=new JMenuItem("Kill Process Ctrl+Shift+K");
        JMenuItem proj=new JMenuItem("Project");
        JMenuItem pack=new JMenuItem("Package");
        JMenuItem module=new JMenuItem("Module");
        JMenuItem open=new JMenuItem("Open");
        JMenuItem save=new JMenuItem("Save Ctrl+S");
        JMenuItem fori=new JMenuItem("For Range Ctrl+Shift+F");
        JMenuItem winding=new JMenuItem("Winding Ctrl+Shift+W");
        JMenuItem rotate=new JMenuItem("Rotate Ctrl+Shift+R");
        JMenuItem next=new JMenuItem("Next Word Ctrl+Shift+N");
        JMenuItem game=new JMenuItem("Game Loop Ctrl+Shift+L");
        JMenuItem closec=new JMenuItem("Close Current Tab Alt+X");
        JMenuItem closea=new JMenuItem("Close All Tabs Alt+Shift+X");
        JMenuItem findln=new JMenuItem("Go To Line");
        JMenuItem comment=new JMenuItem("Comment Ctrl+Shift+U");
        JMenuItem uncomment=new JMenuItem("Uncomment Ctrl+Shift+V");
        JMenuItem undo=new JMenuItem("Undo Ctrl+Z");
        JMenuItem redo=new JMenuItem("Ctrl+Y Deletes your code LOL!");
        JMenuItem find=new JMenuItem("Find Ctrl+F");
        JMenuItem toggle=new JMenuItem("Suggestions: On");
        JMenuItem documentation=new JMenuItem("Read Documentation");

        //changes the size of the components
        explorer.setSize(180,540);
        editors.setSize(960,540);
        editors.setMinimumSize(editors.getSize());

        //add the action listeners to the menu items
        proj.addActionListener(this::newProject);
        open.addActionListener(this::openProject);
        save.addActionListener(this::saveProject);
        pack.addActionListener(this::newPackage);
        module.addActionListener(this::newModule);
        run.addActionListener(this::runProject);
        comp.addActionListener(this::compileProject);
        args.addActionListener(this::runArguments);
        kill.addActionListener(this::killProcess);

        //template menu item action listeners
        fori.addActionListener(this::fori);
        winding.addActionListener(this::winding);
        rotate.addActionListener(this::rotating);
        game.addActionListener(this::loop);
        next.addActionListener(this::nextWord);

        //edit menu item action listeners
        undo.addActionListener(this::undo);
        redo.addActionListener(this::redo);
        closec.addActionListener(this::closeCurrentTab);
        closea.addActionListener(this::closeAllTabs);
        findln.addActionListener(this::goToLine);
        comment.addActionListener(this::comment);
        uncomment.addActionListener(this::uncomment);

        //help menu item action listeners
        toggle.addActionListener((e)->toggle.setText("Suggestions: "+((this.suggestionsOn=!this.suggestionsOn)?"On":"Off")));
        find.addActionListener(this::findtxt);
        documentation.addActionListener(this::docs);

        //add menu items to templates
        templates.add(fori);
        templates.add(winding);
        templates.add(rotate);
        templates.add(game);
        templates.add(next);

        //add menu items to edit
        edit.add(templates);
        edit.add(undo);
        edit.add(redo);
        edit.add(closec);
        edit.add(closea);
        edit.add(findln);
        edit.add(comment);
        edit.add(uncomment);

        //add menu items to new
        create.add(proj);
        create.add(pack);
        create.add(module);

        //add menu items to file
        files.add(create);
        files.add(open);
        files.add(save);

        //add menu items to compile or run
        compile.add(comp);
        compile.add(run);
        compile.add(args);
        compile.add(kill);

        //add menu items to help
        help.add(toggle);
        help.add(find);
        help.add(documentation);

        //add menus to menu bar
        tools.add(files);
        tools.add(edit);
        tools.add(compile);
        tools.add(help);

        //sets up the jpanel
        panel.setLayout(new GridBagLayout());
        panel.add(explorer);
        panel.add(editors);
        panel.addComponentListener(new WindowHandler(explorer,editors));

        //creates window
        new WindowMaker("ICS3U FSE May 12th, 2019 by Kevin Zhang",panel,tools);

    }

    /**
     * Gets the selected editor
     * @return The text pane which is selected
     */
    public JTextPane getSelectedEditor() {
        return(JTextPane)((JScrollPane)this.editors.getSelectedComponent()).getViewport().getView();
    }

    /**
     * Creates a new project
     * @param evt The action event passed through, which serves no purpose whatsoever
     */
    public void newProject(ActionEvent evt) {

    	//components to get input from user
        JFrame frame=new JFrame("New Project");
        JPanel panel=new JPanel();
        JTextField name=new JTextField(30);
        JTextField loc=new JTextField("C:\\",30);
        JButton browse=new JButton("Browse");
        JButton create=new JButton("Create");
        JLabel label=new JLabel("Name: ");

        //sets the fonts
        label.setFont(new Font("Times new roman",0,15));
        label.setForeground(new Color(18,18,18));
        name.setFont(new Font("Times new roman",0,15));
        name.setForeground(new Color(18,18,18));
        loc.setFont(new Font("Times new roman",0,15));
        loc.setForeground(new Color(18,18,18));
        browse.setFont(new Font("Times new roman",0,15));
        create.setFont(new Font("Times new roman",0,15));

        //browses the hard drive for folders
        browse.addActionListener((e)->{

        	//create variables
            JFileChooser chooser=new JFileChooser(loc.getText());
            JFrame choose=new JFrame();
            File f=chooser.getCurrentDirectory();

            //opens up the file chooser and gets the selected file
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.showOpenDialog(choose);
            f=chooser.getSelectedFile();
            loc.setText(f.getPath());

        });

        //actually creates the project
        create.addActionListener((e)->{

        	//changes the package explorer and initializes project
            this.packages=new JTree(new DefaultMutableTreeNode(name.getText()));
            this.project=new File(loc.getText()+"\\"+name.getText());
            explorer.setViewportView(packages);
            File p=new File(project.getPath()+"\\proj.txt");

            //make the folder if it doesn't exist
            if(!project.exists())
                project.mkdirs();

            try {

            	//make the file if it doesn't exist
                if(!p.exists())
                    p.createNewFile();

                //write out validity checker and project name
                PrintWriter out=new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(p)),"ISO-8859-1"),true);
                out.println("ICS3U FSE May 12th, 2019 by Kevin Zhang\r\n"+project.getName());
                out.close();

            } catch(IOException ex) {
                ex.printStackTrace();
            }

            //closes the window
            frame.dispose();

        });

        //add components to jpanel
        panel.add(browse);
        panel.add(loc);
        panel.add(label);
        panel.add(name);
        panel.add(create);

        //sets up the jframe
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    /**
     * Creates a new package
     * @param evt The action event, which serves no purpose
     */
    public void newPackage(ActionEvent evt) {

    	//components for dialog of creating a new package
        JFrame frame=new JFrame("Create New Package");
        JPanel panel=new JPanel();
        JTextField field=new JTextField(30);
        JLabel label=new JLabel("Name: ");
        JButton button=new JButton("Create");

        //sets the fonts
        field.setFont(new Font("Arial",0,15));
        label.setFont(new Font("Arial",0,15));
        button.setFont(new Font("Arial",0,15));

        //creates the package
        button.addActionListener((e)->{

            try {

            	//the package folder and the __init__.py file
                File f=new File(project.getPath()+"\\"+field.getText());
                File g=new File(f.getPath()+"\\__init__.py");
                //makes the directory and creates the file
                f.mkdirs();
                g.createNewFile();
                //load the project
                loadProject();

            } catch(IOException ex) {
                ex.printStackTrace();
            }

            //closes the window
            frame.dispose();

        });

        //adds components to jpanel
        panel.add(label);
        panel.add(field);
        panel.add(button);

        //makes window visible
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    /**
     * Opens an existing project
     * @param evt The action event, which serves no purpose whatsoever
     */
    public void openProject(ActionEvent evt) {

    	//the file chooser and the jframe
        JFileChooser chooser=new JFileChooser("C:\\");
        JFrame frame=new JFrame();

        //open the file chooser
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.showOpenDialog(frame);
        //and get the chosen one
        File f=chooser.getSelectedFile();
        String[]s=f==null?new String[0]:f.list();
        //linked list of the child files and folders
        LinkedList<String>children=new LinkedList<String>();
        for(int i=0;i<s.length;i++)
            children.add(s[i]);

        //checks validity
        if(children.contains("proj.txt")) {

            try {

            	//the file and the reader
                File g=new File(f.getPath()+"\\proj.txt");
                BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(g))));
                //the validity checker and the name
                String check=reader.readLine();
                String name=reader.readLine();
                //closes the reader
                reader.close();

                //initializes project
                this.project=f;
                if(!project.getName().equals(name)||!check.equals("ICS3U FSE May 12th, 2019 by Kevin Zhang"))
                    this.project=null;//set project to null if project is not valid
                else
                    loadProject();//load the project otherwise

            } catch(IOException ex) {
                ex.printStackTrace();
            }

        } else {

        	//shows a warning about the invalid project
            JFrame warn=new JFrame("Warning");
            JLabel label=new JLabel("Invalid project!");

            //set the font to be big
            label.setFont(new Font("Arial",0,45));
            label.setForeground(new Color(18,18,18));

            //add the label to jframe and set it to be visible
            warn.add(label);
            warn.pack();
            warn.setVisible(true);

        }

        //closes the window
        frame.dispose();

    }

    /**
     * Creates a new python module
     * @param evt The action even passed through, it serves no purpose
     */
    public void newModule(ActionEvent evt) {

    	//components to query the user about the name and package of the module
        JFrame frame=new JFrame("Create New Module");
        JPanel panel=new JPanel();
        JComboBox<String>pack=new JComboBox<String>();
        JTextField name=new JTextField(30);
        JButton button=new JButton("Create");
        //iterate through the package names
        Iterator<String>iter=names.iterator();

        //sets the font and adds the default package option
        pack.setFont(new Font("Arial",0,15));
        name.setFont(new Font("Arial",0,15));
        button.setFont(new Font("Arial",0,15));
        pack.addItem("default package");

        //add all other package names to the drop down menu
        while(iter.hasNext())
            pack.addItem(iter.next());

        //creates the module
        button.addActionListener((e)->{

            try {

                String s=name.getText();//gets the text from text field
                if(!s.substring(s.length()-3).equals(".py"))
                    s+=".py";//add .py extension as necessary
                //creates file object and creates file
                File f=pack.getSelectedItem().toString().equals("default package")?new File(project.getPath()+"\\"+s):new File(project.getPath()+"\\"+pack.getSelectedItem().toString()+"\\"+s);
                f.createNewFile();
                //load the project
                loadProject();

            } catch(Exception ex) {
                ex.printStackTrace();
            }

            //closes the window
            frame.dispose();

        });

        //add components to the jpanel
        panel.add(pack);
        panel.add(name);
        panel.add(button);

        //gets the jframe ready
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    /**
     * Loads the project into the package explorer
     */
    public void loadProject() {

    	//the tree nodes
        DefaultMutableTreeNode m;
        DefaultMutableTreeNode n=new DefaultMutableTreeNode(project.getName());
        //the list of tree nodes to be loaded
        LinkedList<DefaultMutableTreeNode>p=loadProject(n,project);
        Iterator<DefaultMutableTreeNode>iter=p.iterator();
        //re-initializes package names
        this.names=new LinkedList<String>();

        //add all the packages to root node and list of packages
        while(iter.hasNext()) {

            n.add(m=iter.next());
            names.add((String)m.getUserObject());

        }

        //redirect package explorer
        this.packages=new JTree(n);
        packages.addMouseListener(new JTreeMouseHandler(this::fileClicked));
        explorer.setViewportView(packages);

    }

    /**
     * Loads the project into the package explorer
     * @param node The node to load onto
     * @param f The directory to load from
     * @return A linked list of packages and files.
     */
    public LinkedList<DefaultMutableTreeNode>loadProject(DefaultMutableTreeNode node,File f) {

    	//temporary node, file arrya and list of nodes
        DefaultMutableTreeNode m;
        File[]fs=f.listFiles();
        LinkedList<DefaultMutableTreeNode>s=new LinkedList<DefaultMutableTreeNode>();

        //for each file g of fs
        for(File g:fs) {

            if(g.isDirectory()) {

                //if g is a directory, potentially having more files
                //recursively load the files of g
                s.add(m=new DefaultMutableTreeNode(g.getPath().substring(this.project.getPath().length()+1)));
                s.addAll(loadProject(m,g));

            } else {
                //otherwise, add it to the tree node.
                node.add(m=new DefaultMutableTreeNode(g.getName()));
            }

        }

        return s;

    }

    /**
     * Kills the currently running program
     * @param evt This doesn't really do anything
     */
    public void killProcess(ActionEvent evt) {

        if(p.isAlive())
            p.destroy();//kills the process if it is still alive

    }

    /**
     * Saves the whole project
     * @param evt The action event passed through, it serves no purpose
     */
    public void saveProject(ActionEvent evt) {

    	//printstream for printing, current pane and iterator
        PrintStream out=null;
        JTextPane pane;
        Iterator<String>iter=tabs.iterator();
        String s;

        for(int i=0;i<tabs.size()&iter.hasNext();i++) {

        	//gets the file path and initializes the text pane
            s=iter.next();
            pane=(JTextPane)((JScrollPane)editors.getComponentAt(i)).getViewport().getView();

            try {

                out=new PrintStream(new BufferedOutputStream(new FileOutputStream(s)));//initializes print stream
                out.print(pane.getText());//print the text from the text pane
                out.flush();//flush the stream
                out.close();//closes the stream

            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Goes to a line with a line number
     * @param evt This doesn't really do anything
     */
    public void goToLine(ActionEvent evt) {

    	//components to query the user about which line to go to
        JFrame frame=new JFrame("Go To Line Number");
        JPanel panel=new JPanel();
        JTextField field=new JTextField(5);
        JButton button=new JButton("Find");

        //sets the font of button and jtextfield
        field.setFont(new Font("Arial",0,30));
        button.setFont(new Font("Arial",0,30));

        //jumps to that line
        button.addActionListener((e)->{

        	//gets the text
            String s=getSelectedEditor().getText();
            int lines=0;//lines
            int dest=Integer.parseInt(field.getText())-1;//destination line
            int pos=0;//position to set caret to

            //loops through all characters
            for(int i=0;i<s.length();i++) {

            	//if a line is found
                if(s.charAt(i)=='\n')
                    lines++;//add to lines

                //if destination is reached
                if(lines==dest) {

                	//set position to i and break for loop
                    pos=i;
                    break;

                }

            }

            //sets the caret position and closes window
            getSelectedEditor().setCaretPosition(pos);
            frame.dispose();

        });

        //adds components to jpanel
        panel.add(field);
        panel.add(button);

        //gets the window ready
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    /**
     * Listens when a tree node is double clicked.
     * @param evt The action event
     */
    public void fileClicked(ActionEvent evt) {

    	//the selected node
        DefaultMutableTreeNode n=(DefaultMutableTreeNode)packages.getLastSelectedPathComponent();
        //if it is null then terminate
        if(n==null)return;
        //the name and the path
        String name=(String)n.getUserObject();
        String path=(String)((DefaultMutableTreeNode)n.getParent()).getUserObject();
        path=path.equals(project.getName())?"":path+"\\";

        //if that file is a file
        if(new File(project.getPath()+"\\"+path+name).isFile()) {

            try {
                open(project.getPath()+"\\"+path+name);//opens the file
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Opens a file
     * @param f The path to the file
     * @throws FileNotFoundException
     */
    public void open(String f)throws Exception {

    	//terminate if the file is already open
        if(tabs.contains(f))
            return;

        String name=f.substring(f.lastIndexOf('\\')+1);//the last index of the backslash
        JTextPane editor=new JTextPane();//creates a new textpane
        JScrollPane pane=new JScrollPane(editor);//creates a new scrollpane
        BufferedReader re=new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new File(f)))));//reader that reads from file
        String s="";//load the text into this
        String l;//one line

        //reads all lines
        while((l=re.readLine())!=null) {
            s+=l+System.getProperty("line.separator");
        }

        //sets the colour, font and text of the editor
        editor.setForeground(new Color(18,18,18));
        editor.setFont(new Font("Consolas",0,12));
        editor.setText(s);

        re.close();//closes reader
        editor.setFont(new Font("Consolas",0,12));
        editor.addKeyListener(keys);
        //kills tab key
        editor.getInputMap().put(KeyStroke.getKeyStroke(9,0),"none");
        //add the text pane to tabs
        editors.addTab(name,pane);
        tabs.add(f);//adds to list of tab names
        undos.add(new LinkedList<>());//creates undo list for this file
        undos.getLast().add(editor.getText());//adds the original text to undo text

    }

    /**
     * Compiles the project
     * @param evt Does absolutely nothing
     */
    public void compileProject(ActionEvent evt) {

    	//saves the project before compiling it
        saveProject(evt);

        try {
            Runtime.getRuntime().exec("python -m compileall \""+project.getPath()+"\"");//compiles the project
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Allows user to set runtime arguments
     * @param evt Serves no purpose
     */
    public void runArguments(ActionEvent evt) {

    	//components to query input from user
        JFrame frame=new JFrame("Run Arguments");
        JPanel panel=new JPanel();
        JLabel label=new JLabel("Arguments:");
        JTextField field=new JTextField(15);
        JButton button=new JButton("Close");

        //sets the fonts
        label.setFont(new Font("Arial",0,30));
        field.setFont(new Font("Arial",0,30));
        button.setFont(new Font("Arial",0,30));

        //adds the command line arguments
        button.addActionListener((e)->{

            try {

            	//project file verifier
                File f=new File(project.getPath()+"\\proj.txt");
                PrintWriter writer=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f,true)))));

                //stores arguments in there
                writer.println(field.getText());
                writer.flush();
                writer.close();

            } catch(FileNotFoundException ex) {
                ex.printStackTrace();
            }

            //closes the window
            frame.dispose();

        });

        //add the components to jpanel
        panel.add(label);
        panel.add(field);
        panel.add(button);

        //gets the jframe ready
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    /**
     * Runs the current project with python interpreter
     * @param evt Action event passed through, serves no purpose
     */
    public void runProject(ActionEvent evt) {

        try {

        	//saves the project before running it
            saveProject(evt);
            Process p=Runtime.getRuntime().exec("python -m compileall \""+project.getPath()+"\"");//compiles the project
            BufferedReader errreader=new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream())));//error reader
            String msg="";//compilation message
            String r;//stores a single line

            //reads from compilation message
            while((r=errreader.readLine())!=null) {
                msg+=r+System.getProperty("line.separator");
            }

            //checks if there is an error
            if(!msg.contains("*** Sorry")) {

            	//if there is not, then run the program
            	//this reader reads command line arguments
                BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new File(project.getPath()+"\\proj.txt")))));
                Thread in=new Thread(this::runInput);//input stream thread
                Thread out=new Thread(this::runOutput);//output stream thread
                Thread err=new Thread(this::runErr);//error stream thread
                String com="python \""+tabs.get(editors.getSelectedIndex())+"\"";//command for running project
                reader.readLine();//reads unimportant line
                reader.readLine();//reads unimportant line
                this.p=Runtime.getRuntime().exec(com+" "+reader.readLine());//run the program with the arguments
                reader.close();//closes the reader

                //starts the threads
                in.start();
                out.start();
                err.start();

            } else {

            	//window and other jcomponents for showing error
                JFrame frame=new JFrame("Compilation Error");
                JPanel panel=new JPanel();
                JTextPane errmsg=new JTextPane();
                JScrollPane pane=new JScrollPane(errmsg);

                //sets the font, colour and text
                errmsg.setForeground(new Color(240,0,0));
                errmsg.setFont(new Font("Consolas",0,30));
                errmsg.setPreferredSize(new Dimension(960,540));
                errmsg.setText(msg);
                panel.add(pane);//adds the pane

                //sets up the jframe
                frame.add(panel);
                frame.pack();
                frame.setResizable(false);
                frame.setVisible(true);

            }

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method will allow the user to write input into the program
     */
    public void runInput() {

        try {

        	//writer to write into the program
            PrintWriter writer=new PrintWriter(new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(p.getOutputStream()))));
            //components for input window
            JFrame inframe=new JFrame("sys.stdin");
            JPanel inpanel=new JPanel(new BorderLayout());
            JTextPane inpane=new JTextPane();
            StyledDocument write=inpane.getStyledDocument();
            this.input=new JScrollPane(inpane);

            //sets size, font and colour
            inpane.setPreferredSize(new Dimension(960,300));
            inpane.setForeground(new Color(30,85,170));
            inpane.setFont(new Font("Consolas",0,15));

            //sets preferred size and add to panel
            input.setPreferredSize(new Dimension(960,300));
            inpanel.add(input);

            //sets up jframe
            inframe.add(inpanel);
            inframe.pack();
            inframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            inframe.setResizable(false);
            inframe.setVisible(true);

            //position and enter position
            int pos=0,ent;

            //loops as long as the process is alive
            while(p.isAlive()) {

            	//gets the text
                String str=write.getText(0,write.getLength());

                //if position is less than length set it to length
                if(pos>write.getLength()) {
                    pos=write.getLength();
                }

                //if there is an enter after the current position
                if((ent=str.lastIndexOf("\n"))>pos) {

                	//write into the process
                    writer.print(str.substring(pos,++ent));
                    writer.flush();
                    pos=ent;//and sets position to the last enter

                }

            }

            //sets the window to be closable after process has finished
            inframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method will actually display the output, it runs in a different thread
     */
    public void runOutput() {

        try {

        	//will read the program's output
            BufferedInputStream in=new BufferedInputStream(p.getInputStream());

            //components for displaying the output of the program
            JFrame outframe=new JFrame("sys.stdout");
            JPanel outpanel=new JPanel(new BorderLayout());
            JTextPane outpane=new JTextPane();
            StyledDocument doc=outpane.getStyledDocument();
            Style s=doc.addStyle("output text",null);
            this.output=new JScrollPane(outpane);

            //sets the preferred size, font and colour
            //and adds to jpanel
            outpane.setPreferredSize(new Dimension(960,300));
            outpane.setFont(new Font("Consolas",0,15));
            outpane.setEditable(false);
            output.setPreferredSize(new Dimension(960,300));
            outpanel.add(output);

            //sets up the jframe
            outframe.add(outpanel);
            outframe.pack();
            outframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            outframe.setResizable(false);
            outframe.setVisible(true);

            //sets up styles
            StyleConstants.setFontFamily(s,"Consolas");
            StyleConstants.setFontSize(s,15);
            StyleConstants.setForeground(s,new Color(18,18,18));
            int b=0;//a byte

            //loops as long as process is alive
            while(p.isAlive()) {

            	//loops until there is nothing to be read
                while((b=in.read())>=0) {
                    doc.insertString(doc.getLength(),Character.toString((char)b),s);//add the character
                }

            }

            //insert finish message after program has finished and enables closing of the window
            doc.insertString(doc.getLength(),"Program finished with exit code "+Integer.toString(p.exitValue())+".",s);
            outframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Thread for error output
     */
    public void runErr() {

        try {

        	//error stream, and strings
            BufferedReader err=new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getErrorStream())));
            String s="";
            String l="";
            boolean error=false;
            //components for displaying errors
            JFrame frame=new JFrame("Error!");
            JPanel panel=new JPanel();
            JTextPane errstrm=new JTextPane();
            JScrollPane pane=new JScrollPane(errstrm);

            //sets the font, size, colour, and then adds to jpanel
            errstrm.setForeground(new Color(240,0,0));
            errstrm.setFont(new Font("Consolas",0,30));
            pane.setPreferredSize(new Dimension(960,540));
            panel.add(pane);

            //sets up the frame
            frame.add(panel);
            frame.pack();
            frame.setResizable(false);

            //
            while(p.isAlive()) {

            	//reads a line and adds it if not null
                l=err.readLine();
                if(l!=null)s+=l;

                //if there are no errors but the length of s is greter than zero
                if(!error&&s.length()>0) {

                	//then there is something wrong
                    frame.setVisible(true);//set the frame to be visible
                    error=true;//sets error to true

                    //appends all lines to text pane
                    while((l=err.readLine())!=null)
                        errstrm.setText(s+=l);

                }

                //sets the text to s if there is an error
                if(error)
                    errstrm.setText(s);

            }

            //closes the stream
            err.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Finds text
     * @param evt This is pretty useless
     */
    public void findtxt(ActionEvent evt) {

    	//components
        JFrame frame=new JFrame("Find Text");
        JPanel panel=new JPanel();
        JTextField find=new JTextField(15);
        JButton pfind=new JButton("Find");

        //sets the font
        find.setFont(new Font("Arial",0,15));
        pfind.setFont(new Font("Arial",0,15));

        //finds the string
        pfind.addActionListener((e)->{

        	//gets the text
            String s=getSelectedEditor().getText();
            String target=find.getText();
            int pos=s.replace("\n","").indexOf(target,getSelectedEditor().getCaretPosition());//gets the index

            //select the word and requests focus
            getSelectedEditor().select(pos,pos+target.length());
            getSelectedEditor().requestFocusInWindow();

        });

        //adds the components
        panel.add(find);
        panel.add(pfind);

        //sets up the jframe
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    /**
     * This is for the for loop template
     * @param evt This doesn't really do anything
     */
    public void fori(ActionEvent evt) {

        try {

        	//reads from resource that contains the code
            BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/fori.txt"))));
            String s;
            String temp="";
            //the textpane and the style
            JTextPane pane=(JTextPane)((JScrollPane)editors.getSelectedComponent()).getViewport().getView();
            Style st=pane.getLogicalStyle();

            //reads the file and appends to a string
            while((s=reader.readLine())!=null)
                temp+=s+System.getProperty("line.separator");

            //insert into document
            pane.getStyledDocument().insertString(pane.getCaretPosition(),temp,st);
            reader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This is for the list rotation template
     * @param evt The action event, doesn't really do anything
     */
    public void rotating(ActionEvent evt) {

        try {

        	//reads from resource that contains the code
            BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/rotate.txt"))));
            String s;
            String temp="";
            //the textpane and the style
            JTextPane pane=(JTextPane)((JScrollPane)editors.getSelectedComponent()).getViewport().getView();
            Style st=pane.getLogicalStyle();

            //reads the file and appends to a string
            while((s=reader.readLine())!=null)
                temp+=s+System.getProperty("line.separator");

            //insert into document
            pane.getStyledDocument().insertString(pane.getCaretPosition(),temp,st);
            reader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This is for the winding function
     * @param evt The action event, doesn't really do anything
     */
    public void winding(ActionEvent evt) {

        try {

        	//reads from resource that contains the code
            BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/winding.txt"))));
            String s;
            String temp="";
            //the textpane and the style
            JTextPane pane=(JTextPane)((JScrollPane)editors.getSelectedComponent()).getViewport().getView();
            Style st=pane.getLogicalStyle();

            //reads the file and appends to a string
            while((s=reader.readLine())!=null)
                temp+=s+System.getProperty("line.separator");

            //insert into document
            pane.getStyledDocument().insertString(pane.getCaretPosition(),temp,st);
            reader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This is the template for reading the next word from sys.stdin
     * @param evt This really doesn't do anything
     */
    public void nextWord(ActionEvent evt) {

        try {

        	//reads from resource that contains the code
        	BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/next_word.txt"))));
            String s;
            String temp="";
        	//the textpane and the style
            JTextPane pane=(JTextPane)((JScrollPane)editors.getSelectedComponent()).getViewport().getView();
            Style st=pane.getLogicalStyle();
            
        	//reads the file and appends to a string
            while((s=reader.readLine())!=null)
                temp+=s+System.getProperty("line.separator");
            
        	//insert into document
            pane.getStyledDocument().insertString(pane.getCaretPosition(),temp,st);
            reader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This is for the game loop
     * @param evt The action event, doesn't really do anything
     */
    public void loop(ActionEvent evt) {

        try {

        	//reads from resource that contains the code
            BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("/templates/game_loop.txt"))));
            String s;
            String temp="";
        	//the textpane and the style
            JTextPane pane=(JTextPane)((JScrollPane)editors.getSelectedComponent()).getViewport().getView();
            Style st=pane.getLogicalStyle();
            
        	//reads the file and appends to a string
            while((s=reader.readLine())!=null)
                temp+=s+System.getProperty("line.separator");

        	//insert into document
            pane.getStyledDocument().insertString(pane.getCaretPosition(),temp,st);
            reader.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Closes the current tab
     * @param evt The action event that doesn't do anything really
     */
    public void closeCurrentTab(ActionEvent evt) {

    	//removes tab from the tabbed pane
    	//but also removes them from list of tab names and removes their undo list
        tabs.remove(editors.getSelectedIndex());
        undos.remove(editors.getSelectedIndex());
        editors.removeTabAt(editors.getSelectedIndex());

    }

    /**
     * Closes all tabs
     * @param evt The action event which really serves no purpose
     */
    public void closeAllTabs(ActionEvent evt) {

    	//clears the tabbed pane
    	//but also clears the list of tab names and the undo lists
        tabs.clear();
        undos.clear();
        editors.removeAll();

    }

    /**
     * Comments the selected text
     * @param evt The action event that doesn't really do anything
     */
    public void comment(ActionEvent evt) {

    	//gets the selected text and the document
    	//and the start of the selection
        String s=getSelectedEditor().getSelectedText();
        StyledDocument doc=getSelectedEditor().getStyledDocument();
        int start=getSelectedEditor().getSelectionStart();

        try {

        	//insert two hashtags and shifts the start by two
            doc.insertString(start,"##",doc.getLogicalStyle(start));
            start+=2;

            //loops through selected text
            for(int i=s.length()-1;i>=0;i--) {

            	//add hash tags after every line separator
                if(s.charAt(i)=='\n')
                    doc.insertString(start+i+1,"##",doc.getLogicalStyle(start+i));

            }

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Uncomments the selected text
     * @param evt The action event that doesn't really do anything
     */
    public void uncomment(ActionEvent evt) {

        String s=getSelectedEditor().getSelectedText();//gets selected text
        s=s.replace("##","");//replace hashtags with empty string
        //gets the document, selection start and end
        StyledDocument doc=getSelectedEditor().getStyledDocument();
        int start=getSelectedEditor().getSelectionStart();
        int end=getSelectedEditor().getSelectionEnd();

        try {

        	//removes the selection and inserts new string
            doc.remove(start,end-start);
            doc.insertString(start,s,doc.getLogicalStyle(start));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds a suggestion
     * @param suggestion The suggestion to be added
     */
    public void addSuggestion(String suggestion) {

    	//create suggestion from item and add it to popup menu
        JMenuItem item=new JMenuItem(suggestion);
        suggest.add(item);

        //inserts the suggestion when clicked on
        item.addActionListener((e)->{

        	//gets the document, and finds the whitespace before the word
            StyledDocument doc=getSelectedEditor().getStyledDocument();
            int space=getSelectedEditor().getText().substring(0,getSelectedEditor().getCaretPosition()).lastIndexOf(' ');
            space=max(space,getSelectedEditor().getText().substring(0,getSelectedEditor().getCaretPosition()).lastIndexOf('\n'));

            try {

            	//removes the old word and insert the suggestion
                doc.remove(space,getSelectedEditor().getCaretPosition()-space);
                doc.insertString(space,item.getText(),doc.getLogicalStyle(space));

            } catch(BadLocationException ex) {
                ex.printStackTrace();
            }

        });

    }

    /**
     * Toggles showing suggestions
     */
    public void showSuggestions(boolean show) {

        if(show&&suggestionsOn) {

        	//editor and font metrics
            JTextPane editor=getSelectedEditor();
            FontMetrics fm=editor.getFontMetrics(new Font("Consolas",0,12));
            //get lines and line count
            String[]lines=editor.getText().substring(0,editor.getCaretPosition()).split("\n");
            int lncount=lines.length;

            //shows the popup menu and places it using font metrics
            suggest.show(editor,fm.stringWidth(lines[lncount-1]),lncount*(fm.getAscent()+fm.getDescent()));
            editor.requestFocusInWindow();

        } else {
            this.suggest=new JPopupMenu();//re-initializes popup menu
        }

    }

    /**
     * Adds an text to the undo list of selected tab
     * @param s The text to add
     */
    public void addUndoText(String s) {
        undos.get(editors.getSelectedIndex()).add(s);
    }

    /**
     * Performs an undo action
     * @param evt Doesn't really do anything
     */
    public void undo(ActionEvent evt) {

    	//undo if and only if there's more than one item in the undo list
        if(undos.get(editors.getSelectedIndex()).size()>1) {

        	//gets the undo text and set the editor text to it
            String txt=undos.get(editors.getSelectedIndex()).pollLast();
            getSelectedEditor().setText(txt==null?"":txt);

        }

    }

    /**
     * Deletes your code LOL
     * @param evt The required action event that doesn't really do anything
     */
    public void redo(ActionEvent evt) {

    	//finds caret position, and start and end of the line
        int pos=getSelectedEditor().getCaretPosition();
        int start=getSelectedEditor().getText().substring(0,pos).lastIndexOf('\n')+1;
        int end=getSelectedEditor().getText().indexOf('\n',pos+1);

        try {
            getSelectedEditor().getStyledDocument().remove(start,end-start);//removes the line
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Opens the documentation
     * @param evt Is not useful
     */
    public void docs(ActionEvent evt) {

        try {

        	//reader to read from documentation resource
            BufferedReader reader=new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("/docs.txt"))));
            //window and jcomponents to show the documentation
            JFrame frame=new JFrame("Documentation");
            JPanel panel=new JPanel();
            JTextArea ar=new JTextArea(20,60);
            JScrollPane pane=new JScrollPane(ar);
            //variables for reading text
            String txt="";
            String ln;

            //reads from documentation resource
            while((ln=reader.readLine())!=null)
                txt+=ln+System.getProperty("line.separator");

            //closes the reader
            reader.close();

            //gets the text area ready
            ar.setFont(new Font("Consolas",0,12));
            ar.setText(txt);
            ar.setLineWrap(true);
            ar.setWrapStyleWord(true);
            //and add scrollpane to jpanel
            panel.add(pane);

            //gets the jframe ready
            frame.add(panel);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);

        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Main method that runs on launch
     * @param args Command line arguments
     */
    public static final void main(String...args) {

    	//combines command line arguments into a single string
        String s="";
        for(String r:args)
            s+=r+" ";
        s=s.trim();

        //creates the app with the s as the path for the project to be opened
        new AppMain(s);

    }

}