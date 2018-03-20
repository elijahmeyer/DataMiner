/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;

/**
 *
 * @author Elijah
 */
public class Database {
    private ArrayList<ArrayList<Integer>> database = new ArrayList<>();
    private int numItems = 0;
    
    public Database(String filename) {
        this.read(filename);
    }
    
    /*
      This method scans the appropriate file, if one exists, for lists of items.
      These lists are converted to integers and stored in a nested ArrayList.
    */
    private void read(String filename) {
        // Open file.
        File f = new File(filename);
        try {
            Scanner input = new Scanner(f);
            
            // Read in every line, convert from String to int, and add the list to 
            // the nested list.
            while (input.hasNextLine()) {
                String transactionString = input.nextLine();
                ArrayList<Integer> transaction = new ArrayList<>();
                String[] itemStrings = transactionString.split(" ");
                for (int i = 0; i < itemStrings.length; i++) {
                    int item = Integer.parseInt(itemStrings[i]);
                    transaction.add(item);
                    
                    // Determine the largest item number in the database.
                    if (item > numItems) {
                        numItems = item;
                    }
                }
                database.add(transaction);
            }
            // Close the Scanner to the input file when finished.
            input.close();
            
            // Increment the count of items to accomodate the fact that one of the
            // items is designated with the number 0.
            numItems++;
        } catch (Exception ex) {
            System.out.println(ex);
            database = null;
        }
    }
    public ArrayList<ArrayList<Integer>> getList() {
        return database;
    }
    
    public int getNumItems() {
        return numItems;
    }
    
    public int size() {
        return database.size();
    }
    
    public ArrayList<Integer> get(int index) {
        return database.get(index);
    }
    
    public void supportScan(HashTree candidates) {
        for (int i = 0; i < database.size(); i++) {
            ArrayList<Integer> transaction = database.get(i);
            SearchByClass traverser = new SearchByClass(Candidate.class);
            candidates.traverse(traverser);
            Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
            
            while (iterator.hasNext()) {
                iterator.next().countSupport(transaction);
            }
        }
        /*
        SearchByClass traverser = new SearchByClass(Candidate.class);
        candidates.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().toString());
        }
        */
    }
    /*
      This method determines the support count of a candidate itemset.
    */
    public int supportOf(ArrayList<Integer> itemset) {
        // Initialize variables.
        int supportCount = 0;
        int itemCount;
        
        // Read in every list in the database.
        for (int i = 0; i < database.size(); i++) {
            ArrayList<Integer> temp = database.get(i);
            itemCount = 0;
            
            // Compare every item in the transaction with every item in the
            // submitted itemset.
            for (int j = 0; j < temp.size(); j++) {
                for (int k = 0; k < itemset.size(); k++) {
                    if (itemset.get(k) == temp.get(j)) {
                        itemCount++;
                    }
                }
            }
            
            // If every item in the itemset was found in the transaction, increment
            // support count.
            if (itemCount == itemset.size()) {
                supportCount++;
            }
        }
        return supportCount;
    }
    
    /*
       This method counts the frequency of every item in the database.
       @param: An empty array with the same number of entries as items in the database.
       Precondition: the array must have the same number of entries as items in the database.
       Postcondition: Every entry in the array will contain the frequency of its index
       in the database.
    */
    public void genInitialCounts(int[] items) {
        // Read in every transaction from the database.
        for (int i = 0; i < database.size(); i++) {
            ArrayList<Integer> transaction = database.get(i);
            
            // For each item in the transaction, increment the item's corresponding
            // bucket in the submitted array.
            for (int j = 0; j < transaction.size(); j++) {
                int item = transaction.get(j);
                items[item]++;
            }
        }
    }
}