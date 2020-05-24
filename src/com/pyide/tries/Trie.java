package com.pyide.tries;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
/**
 * This class is for the trie used for suggestions
 * @author Kevin Zhang
 */
public class Trie {

    /**
     * This is the node for the trie
     * @author Kevin Zhang
     */
    public static class Node {

        /**
         * The children of this node
         */
        private HashMap<Character,Node>children;
        /**
         * The character that is this node
         */
        private char c;
        /**
         * Whether this is where a word ends
         */
        private boolean isWord;
        /**
         * The constructor for the node
         */
        public Node(char c) {

            this.children=new HashMap<>();
            this.c=c;

        }

        /**
         * Gets all the words that match the prefix
         * @param prefix The prefix to find
         * @return All words that match the prefix
         */
        public String[]getAllWords(String prefix) {

            if(this.isWord)
                return new String[]{prefix};//returns the prefix in an array if this is a word

            //linked list of words and iterator through the children
            LinkedList<String>words=new LinkedList<String>();
            Iterator<Character> iter=children.keySet().iterator();

            //iterate using iterator
            while(iter.hasNext()) {

            	//get all words from child
                char ch=iter.next();
                String[]r=children.get(ch).getAllWords(prefix+String.valueOf(ch));

                //and add it to the list
                for(int i=0;i<r.length;i++)
                    words.add(r[i]);

            }

            //return the list as an array
            return words.toArray(new String[words.size()]);

        }

    }

    /**
     * The root node
     */
    private Node root;

    /**
     * This is the constructor for the trie
     */
    public Trie() {
        this.root=new Node((char)0);
    }

    /**
     * Adds a word to the trie
     * @param word The word to add
     */
    public void addWord(String word) {

    	//node and index
        Node n=root;
        int i;

        //iterate through word
        for(i=0;i<word.length();i++) {

            if(n.children.containsKey(word.charAt(i)))
                n=n.children.get(word.charAt(i));//move along if the child node exists
            else {

            	//otherwise, add a child node, and then move along
                n.children.put(word.charAt(i),new Node(word.charAt(i)));
                n=n.children.get(word.charAt(i));

            }

        }

        //set the ending node to be a word
        n.isWord=true;

    }

    /**
     * Matches up the prefix
     * @param prefix The prefix to find
     * @return All words that match the prefix
     */
    public String[]match(String prefix) {

    	//node used to traverse the trie
        Node n=root;

        //iterate through prefix
        for(int i=0;i<prefix.length();i++) {

            if(n.children.containsKey(prefix.charAt(i)))
                n=n.children.get(prefix.charAt(i));//if there is a child, move along
            else
                return new String[0];//otherwise, return empty array

        }

        //gets all the words from the ending node
        return n.getAllWords(prefix);

    }

}