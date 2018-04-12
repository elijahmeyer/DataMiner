/*
 * Elijah Meyer
 * CS 4710-01
 * Dr. Soon Chung
 * April 17, 2018
 *
 * This file defines the Database class and all of its fields and methods.
 * The Database class is used to store the transaction database in a quickly
 * accessible manner and scan it. The class contains a nested ArrayList
 * of integers to keep track of transactions and it keeps track of the number
 * of different items in the database.
 */
package dataminer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;


public class Database 
{
    private ArrayList<ArrayList<Integer>> database = new ArrayList<>();
    private int numItems = 0;
    
    public Database(String filename) 
    {
        this.read(filename);
    }
    
    /*
      Scans the appropriate file, if one exists, for lists of items represented 
      as integers. These lists are converted to integers and stored in a nested 
      ArrayList.
      @param filename - the name of the file to be scanned
    */
    private void read(String filename) 
    {
        // Open file.
        File f = new File(filename);
        try 
        {
            Scanner input = new Scanner(f);
            
            // Read in every line, convert from String to int, and add the list 
            // to the nested list.
            while (input.hasNextLine()) 
            {
                String transactionString = input.nextLine();
                ArrayList<Integer> transaction = new ArrayList<>();
                String[] itemStrings = transactionString.split(" ");
                for (int i = 0; i < itemStrings.length; i++) 
                {
                    int item = Integer.parseInt(itemStrings[i]);
                    transaction.add(item);
                    
                    // Determine the largest item number in the database.
                    if (item > numItems) 
                    {
                        numItems = item;
                    }
                }
                database.add(transaction);
            }
            // Close the Scanner to the input file when finished.
            input.close();
            
            // numItems holds the largest integer in the database, which should
            // be the number of distinct items in the database. However, the
            // items begin count at 0 in the database this program was designed
            // to mine, so numItems must be incremented to account for this.
            numItems++;
            
        } catch (Exception ex) 
        {
            System.out.println(ex);
            database = null;
        }
    }
    
    /*
      Returns the nested ArrayList storing the database's transactions.
      @return the ArrayList of transactions
    */
    public ArrayList<ArrayList<Integer>> getList() 
    {
        return database;
    }
    
    /*
      Returns the count of distinct items in the database.
      @return the number of different items in the database
    */
    public int getNumItems() 
    {
        return numItems;
    }
    
    /*
      Returns the number of transactions in the database.
      @return the number of entries in the ArrayList of transactions
    */
    public int size() 
    {
        return database.size();
    }
    
    /*
       Returns the specified transaction.
       @param index - the index of the desired transaction
       @return an ArrayList containing integers that represent items purchased
       during a transaction
    */
    public ArrayList<Integer> get(int index) 
    {
        return database.get(index);
    }
    
    /*
      Iterates through the database, counting the support of every candidate
      itemset in the given hash tree.
      @param candidates - the hash tree containing the candidate itemsets to be
      evaluated for support
    */
    public void supportScan(HashTree candidates) 
    {
        // For each transaction, iterate through the hash tree.
        for (int i = 0; i < database.size(); i++) 
        {
            ArrayList<Integer> transaction = database.get(i);
            int usedCount = 0;
            SearchByClass traverser = new SearchByClass(Candidate.class);
            candidates.traverse(traverser);
            Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
            while (iterator.hasNext()) 
            {
                
                // For each Candidate in the hash tree, count the support of the
                // union of its head and tail and the union of its head and
                // each item in its tail if its head appears in the transaction.
                Candidate c = iterator.next();
                if (DataMiner.subsetOf(c.getHead(), transaction)) 
                {
                    c.countSupport(transaction);
                    usedCount++;
                }
            }
            
            // Inform the user of the progress made thus far.
            System.out.println("Transaction " + (i + 1) + " of " + database.size() + " scanned.");

            // If no Candidate's head appears in a transaction, prune that 
            // transaction. 
            if (usedCount == 0) 
            {
                database.remove(i);
                System.out.println("Transaction " + (i + 1) + " deleted.");
                i--;
            }
        }
    }
        
    /*
       Counts the frequency of every item in the database.
       @param: An empty array with the same number of entries as items in the 
       database.
       Precondition: the array must have the same number of entries as items in 
       the database.
       Postcondition: Every entry in the array will contain the frequency of
       its index in the database.
    */
    public void genInitialCounts(int[] items) 
    {
        // Read in every transaction from the database.
        for (int i = 0; i < database.size(); i++) 
        {
            ArrayList<Integer> transaction = database.get(i);
            
            // For each item in the transaction, increment the item's
            // corresponding bucket in the submitted array.
            for (int j = 0; j < transaction.size(); j++) 
            {
                int item = transaction.get(j);
                items[item]++;
            }
        }
    }
}