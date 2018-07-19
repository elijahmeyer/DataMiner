/*
 * Elijah Meyer
 * CS 4710-01
 * Dr. Soon Chung
 * June 20, 2018
 *
 * This file contains the driver program and several methods used to implement
 * the MaxMiner data mining algorithm on a specified transaction database. 
 * This particular version of the program uses the m-step lookahead method, an
 * experimental method that scans the first m items in a candidate group's tail
 * on each pass instead of the entire tail.
 */

package dataminer;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;


public class DataMiner 
{

    /**
     * This method handles the main control loop, the data structures it needs,
     * and reading in the input file. Because this program longer than normal 
     * programs to run, this program prints statements to the console keeping 
     * the user informed about what step of the algorithm it is currently on.
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        // Initialize variables.
        String filename = "C:\\Users\\Elijah\\Desktop\\MaxMiner Stuff\\T10I4D100K.txt";
        //String filename = "C:\\Users\\Elijah\\Desktop\\MaxMiner Stuff\\testDataset.txt";
        //String filename = args[0];
        
        System.out.println("Reading in the database...");
        Database database = new Database(filename);        
        
        // If the database is empty, return.
        if (database.getList() == null) 
        {
            return;
        }
        System.out.println("Database read finished.");
        
        int numItems = database.getNumItems();
        //double supPercent = Double.parseDouble(args[1]) / 100.0;
        double supPercent = 1 / 100.0;
        
        final int SUPPORT_COUNT = (int) Math.ceil(supPercent * database.size());
        //final int SUPPORT_COUNT = 2;
        System.out.println("Support Count: " + SUPPORT_COUNT);
        
        // Convert frequent items into candidate groups.
        System.out.println("Generating initial groups...");
        HashTree groups = new HashTree();
        HashTree frequent = genInitialGroups(database, groups, numItems, SUPPORT_COUNT);
        
        if (frequent == null) 
        {
            return;
        }
        
        System.out.println("Generation successful.");
        
        while (!groups.isEmpty()) 
        {
            System.out.println("Scanning database...");
            HashTree newGroups = new HashTree();
            
            // Check the support of each candidate group.
            database.supportScan(groups);
            System.out.println("Scan complete.");
        
            // Check support of each candidate groups's head united with each 
            // item in its tail.
            unpackCandidates(groups, newGroups, frequent, SUPPORT_COUNT);
            
            // Remove any frequent itemsets that are subsets of other frequent 
            // itemsets.
            frequent = pruneFrequents(frequent);
            
            // Remove any candidate groups from the new candidate hash tree
            // whose union of head and tail is a subset of a frequent itemset
            // and change focus to the new candidate hash tree.
            groups = pruneCandidates(newGroups, frequent);
        }
        
        // Print maximal frequent itemsets to the console and write them to 
        // a file.
        SearchByClass traverser = new SearchByClass(Candidate.class);
        frequent.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
                
        try 
        {
            int extensionIndex = filename.lastIndexOf(".");
            String resultsFileName = filename.substring(0, extensionIndex) + "LookaheadResults.txt";
            File results = new File(resultsFileName);
            PrintWriter out = new PrintWriter(results);
        
            int setCount = 0;
            while (iterator.hasNext()) {
                ArrayList<Integer> temp = iterator.next().getHead();
                Collections.sort(temp);
                for (int i = 0; i < temp.size(); i++) {
                    out.print(temp.get(i) + " ");
                    System.out.print(temp.get(i) + " ");
                }
                out.println("");          
                System.out.println("");
                setCount++;
            }
            out.close();
        
            /*
            groups = null;
            database = null;
            ArrayList<ArrayList<Integer>> frequents = new ArrayList<>();
            while (iterator.hasNext()) {
                ArrayList<Integer> temp = new ArrayList<>();
                frequents.add(temp);
            }
            Collections.sort(frequents, new ItemsetComparator());
            for (int i = 0; i < frequents.size(); i++) {
                ArrayList<Integer> temp = frequents.get(i);
                Collections.sort(temp);
                for (int j = 0; j < temp.size(); j++) {
                    out.print(temp.get(j) + " ");
                    System.out.print(temp.get(j) + " ");
                }
                out.println("");
                System.out.println("");
                setCount++;
            }
            out.close();
            */
            
            System.out.println(setCount + " maximal frequent itemsets.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    /*
       Determines frequent items, sorts them from least to most frequent, and 
       creates the intital candidate groups based on this order. Candidate 
       groups are created by making each frequent item the head of a candidate 
       group and making each more frequently ocurring item the tail of that 
       group.
       @param db - the database to be scanned
       @param groups - the hash tree to store the candidate groups
       @param numItems - the number of distinct items in the database
       @param minSupCount - the minimum support count used to determine which 
       items are frequent
       @return a hash tree containing the candidate group whose head is the 
       most frequently occurring item
    */
    public static HashTree genInitialGroups(Database db, HashTree groups, int numItems, int minSupCount) {
        // Return if there are no items to use to make candidate groups.
        if (numItems == 0) 
        {
            System.out.println("Error: No items detected.");
            return null;
        }
        
        // Initialize the hash tree that will contain the maximal frequent 
        // itemsets.
        HashTree frequent = new HashTree();
        
        // Determine which items are frequent.
        ArrayList<FrequentItem> itemVectors = findFrequentItems(db, numItems, minSupCount);
        
        // Return if no items are frequent.
        if (itemVectors.isEmpty()) 
        {
            System.out.println("Error: No frequent items detected.");
            return null;
        }
        
        // Sort the frequent items from least to most frequent.
        Collections.sort(itemVectors);
        
        // Make each frequent item the head of a candidate group and make every
        // item that occurs more frequently the tail of that group.
        for (int i = 0; i < itemVectors.size() - 1; i++) 
        {
            ArrayList<Integer> tail = new ArrayList<>();
            for (int j = (i + 1); j < itemVectors.size(); j++) 
            {
                tail.add(itemVectors.get(j).getItem());
            }
            ArrayList<Integer> head = new ArrayList<>();
            head.add(itemVectors.get(i).getItem());
            Candidate cand = new Candidate(head, tail);
            //System.out.println(cand.toString());
            
            // Use the head of each candidate group to hash that group into the
            // candidate hash tree.
            groups.add(cand.getHead(), cand);
        }
        
        // Make the most frequent item its own candidate group and place it in
        // the frequent hash tree.
        ArrayList<Integer> head = new ArrayList<>();
        head.add(itemVectors.get(itemVectors.size() - 1).getItem());
        Candidate c = new Candidate(head, new ArrayList<>());
        frequent.add(c.getHead(), c);
        //System.out.println(c.toString());
        
        // Return the frequent hash tree.
        return frequent;
    }
    
    /*
       Determines which items in the given database are frequent with respect to
       the given support count.
       @param db - the database to be scanned
       @param numItems - the number of distinct items in db
       @param minSupCount - the minimum support count used to determine which 
       items are frequent
       @return an ArrayList containing each frequent item and its count
    */
    public static ArrayList<FrequentItem> findFrequentItems(Database db, int numItems, int minSupCount) 
    {
        // Count occurrences of each item.
        int[] buckets = new int[numItems];
        db.genInitialCounts(buckets);
        
        // Place the frequent items and their counts in an ArrayList and return.
        ArrayList<FrequentItem> itemVectors = new ArrayList<>();
        for (int i = 0; i < buckets.length; i++) 
        {
            if(buckets[i] >= minSupCount) 
            {
                FrequentItem temp = new FrequentItem(i, buckets[i]);
                itemVectors.add(temp);
            }
        }
        return itemVectors;
    }
    
    /*
       Processes each candidate group in the search for maximal frequent
       itemsets. If the group's head united with its tail is a frequent itemset,
       this itemset is added to the frequent hash tree. If not, several 
       candidate groups, consisting of the group's head united with each item 
       in its tail, are added to the new candidate hash tree. However, the 
       candidate group consisting of the head united with the tail's most
       frequent item is instead added to the frequent hash tree.
       @param cand - the hash tree containing the candidate groups
       @param newCand - the hash tree to contain the new candidate groups
       @param freq - the tree to contain the frequent itemsets
       @param minSupCount - the minimum support count used to determine which 
       itemsets are frequent
    */
    public static void unpackCandidates(HashTree cand, HashTree newCand, HashTree freq, int minSupCount) 
    {
        // Traverse the candidate hash tree.
        SearchByClass traverser = new SearchByClass(Candidate.class);
        cand.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        System.out.println("Unpacking candidates...");
        while (iterator.hasNext()) {
            
            // If the candidate group's head united with its tail is a frequent
            // itemset, add it to the frequent hash tree.
            Candidate temp = iterator.next();
            //System.out.println("Before:");
            //System.out.println(temp.toString());
            if (temp.getUnionCount() >= minSupCount) 
            {
                // This is wrong! This says that {1,2,3,4,5} and the tail items
                // have a support of x, but really x is the support of {1} and the tail item
                // (I'm fairly sure this has been corrected. However, this section of the code
                // may deserve a little extra scrutiny.)
                //temp.loadTailItems();
                temp.pruneTail(minSupCount);
                if (temp.getTail().isEmpty()) {
                    ArrayList<Integer> newHead = new ArrayList<>();
                    newHead.addAll(temp.union());
                    
                    Candidate newCandidate = new Candidate(newHead, new ArrayList<>());
                    freq.add(newCandidate.getHead(), newCandidate);
                }
                else {
                    ArrayList<Integer> newHead = new ArrayList<>();
                    newHead.addAll(temp.union());
                    ArrayList<Integer> newTail = new ArrayList<>();
                    newTail.addAll(temp.getTail());
                    
                    Candidate newCandidate = new Candidate(newHead, newTail);
                    newCand.add(newCandidate.getHead(), newCandidate);
                    
                    //Candidate largestFrequent = temp.genSubNodes(newCand, minSupCount);
                    //freq.add(largestFrequent.getHead(), largestFrequent);
                }
            }
            else {
                // If the head united with the tail is infrequent, create new 
                // candidate groups that consist of the head united with each 
                // item in the tail. Place all except the one containing the 
                // most frequent tail item in the candidate hash tree. Place 
                // that one in the frequent hash tree.
                Candidate largestFrequent = temp.genSubNodes(newCand, minSupCount);
                freq.add(largestFrequent.getHead(), largestFrequent);
            }
            //System.out.println("After:");
            //System.out.println(temp.toString());
        }
        System.out.println("Unpacking complete.");
    }
    
    /*
      Determines whether an ArrayList of integers is a subset of another.
      @param subset - the ArrayList to be tested for being a subset of the other
      @param superset - the ArrayList to be tested for being a superset of the 
      other
      @return a boolean value reflecting whether the possible subset is a subset
      of the possible superset
    */
    public static boolean subsetOf(ArrayList<Integer> subset, ArrayList<Integer> superset) 
    {
        // Count how many items in subset are in superset.
        int supportCount = 0;
        for (int i = 0; i < subset.size(); i++)
        {
            if (superset.contains(subset.get(i))) 
            {
                supportCount++;
            }
        }
        
        // If every item in subset is in superset, it must really be a subset.
        return supportCount == subset.size();
    }
    
    /*
       Removes all itemsets from a frequent hash tree that are subsets of
       another itemset in that hash tree.
       @param freq - the frequent hash tree to be trimmed
       @return the trimmed frequent hash tree
    */
    public static HashTree pruneFrequents(HashTree freq) 
    {
        // Initialize variables.
        HashTree newFrequent = new HashTree();
        ArrayList<Candidate> freqContents = new ArrayList<>();
        
        // Add every itemset in the frequent hash tree into an array.
        SearchByClass traverser = new SearchByClass(Candidate.class);
        freq.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) 
        {
            freqContents.add(iterator.next());
        }
        
        for (int i = 0; i < freqContents.size(); i++) 
        {
            for (int j = i + 1; j < freqContents.size(); j++) 
            {
                
                // If any itemset in the array is a subset of another itemset, 
                // remove it from the ArrayList.
                if (subsetOf(freqContents.get(i).getHead(), freqContents.get(j).getHead())) 
                {
                    freqContents.remove(i);
                    
                    // Removing an itemset from the ArrayList will decrement the 
                    // indices of all later itemsets. Decrement the loop counter
                    // to compensate.
                    i--;
                    
                    // Once the itemset is removed, exit the inner loop.
                    break;
                }
                
                // Check the itemsets in the other order to make programming 
                // easier.
                else if (subsetOf(freqContents.get(j).getHead(), freqContents.get(i).getHead())) 
                {
                    freqContents.remove(j);
                    
                    // Decrement the loop counter for the reason stated above.
                    j--;
                }
            }
        }
               
        // Add the itemsets that are not subsets to the new frequent hash tree.
        for (int i = 0; i < freqContents.size(); i++) 
        {
            newFrequent.add(freqContents.get(i).getHead(), freqContents.get(i));
        }
        
        // Return the new frequent hash tree.
        return newFrequent;
    }
    
    /*
       Removes all itemsets from a candidate hash tree that are subsets of an
       itemset in a frequent hash tree.
       @param cand - the candidate hash tree to be trimmed
       @param freq - the frequent hash tree to be compared against
       @return the trimmed candidate hash tree
    */
    public static HashTree pruneCandidates(HashTree cand, HashTree freq) 
    {
        // Initialize variables.
        HashTree newCand = new HashTree();
        ArrayList<Candidate> freqContents = new ArrayList<>();
        
        // Iterate through the frequent hash tree and add its contents to an 
        // ArrayList.
        SearchByClass traverser = new SearchByClass(Candidate.class);
        freq.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) 
        {
            freqContents.add(iterator.next());
        }
     
        // Iterate through the candidate hash tree and add its contents to an 
        // ArrayList.
        ArrayList<Candidate> candContents = new ArrayList<>();
     
        traverser = new SearchByClass(Candidate.class);
        cand.traverse(traverser);
        iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) 
        {
            candContents.add(iterator.next());
        }
        
        for (int i = 0; i < candContents.size(); i++) 
        {
            for (int j = 0; j < freqContents.size(); j++) 
            {
                
                // If the union of head and tail of any candidate group in the 
                // candidate hash tree is a subset of an itemset in the frequent
                // hash tree, remove the item.
                if (subsetOf(candContents.get(i).fullUnion(), freqContents.get(j).getHead())) 
                {
                    candContents.remove(i);
                    
                    // Removing a candidate itemset from the ArrayList will 
                    // decrement the indices of all later itemsets. Decrement
                    // the loop counter to compensate.
                    i--;
                    
                    // If a candidate group is removed, exit the inner loop.
                    break;
                }
            }
        }
        
        // Add every candidate group that is not a subset of a frequent itemset 
        // to the new candidate hash tree.
        for (int i = 0; i < candContents.size(); i++) 
        {
            newCand.add(candContents.get(i).getHead(), candContents.get(i));
        }
        
        // Return the new candidate hash tree.
        return newCand;
    }
}