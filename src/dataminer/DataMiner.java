/*
 * Elijah Meyer
 * Add header
 *
 *
 * This file contains the driver program and several methods used to implement
 * the MaxMiner data mining algorithm on a specified transaction database.
 */

package dataminer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;


public class DataMiner {

    /**
     * This method handles the main control loop, the data structures it needs,
     * and reading in the input file. Because this program takes a very long time to
     * run, this program prints statements to the console keeping the user 
     * informed about what step of the algorithm it is currently on.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Initialize variables.
        //String filename = "C:\\Users\\Elijah\\Desktop\\T10I4D100K.txt";
        String filename = "C:\\Users\\Elijah\\Desktop\\testDataset.txt";
        
        System.out.println("Reading in the database...");
        Database database = new Database(filename);        
        
        // If the database is empty, return.
        if (database.getList() == null) {
            return;
        }
        System.out.println("Database read finished.");
        
        int numItems = database.getNumItems();
        //final int SUPPORT_COUNT = (int) 0.3 * database.size();
        final int SUPPORT_COUNT = 2;
        
        // Count frequency of each item.
        System.out.println("Generating initial counts...");
        int[] buckets = new int[numItems];
        database.genInitialCounts(buckets);
        System.out.println("Count successful.");
        
        // Convert frequent items into candidate groups.
        System.out.println("Generating initial groups...");
        HashTree groups = new HashTree();
        HashTree frequent = genInitialGroups(database, groups, numItems, SUPPORT_COUNT);
        System.out.println("Generation successful.");
        
        while (!groups.isEmpty()) {
            System.out.println("Scanning database...");
            HashTree newGroups = new HashTree();
            
            // Check the support of each candidate group.
            database.supportScan(groups);
            System.out.println("Scan complete.");
        
            // Check support of each candidate groups's head united with each item in its tail.
            unpackCandidates(groups, newGroups, frequent, SUPPORT_COUNT);
            
            // Remove any frequent itemsets that are subsets of other frequent itemsets.
            frequent = purgeFrequent(frequent);
            
            // Remove any candidate groups from the new candidate hash tree
            // whose union of head and tail is a subset of a frequent itemset
            // and change focus to the new candidate hash tree.
            groups = purgeCandidates(newGroups, frequent);
        }
        
        // Print maximal frequent itemsets to the console.
        SearchByClass traverser = new SearchByClass(Candidate.class);
        frequent.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        System.out.println("\n\n\n\nFinal output:");
        
        while (iterator.hasNext()) {
            ArrayList<Integer> temp = iterator.next().getHead();
            for (int i = 0; i < temp.size(); i++) {
                System.out.print(temp.get(i) + " ");
            }
            System.out.println("");          
        }
    }
    
    /*
       Determines frequent items, sorts them from least to most frequent, and creates the intital
       candidate groups based on this order. Candidate groups are created by making each frequent
       item the head of a candidate group and making each more frequently ocurring item the tail of 
       that group.
       @param db - the database to be scanned
       @param groups - the hash tree to store the candidate groups
       @param numItems - the number of distinct items in the database
       @param minSupCount - the minimum support count used to determine which items are frequent
       @return a hash tree containing the candidate group whose head is the most frequently occurring 
       item
    */
    public static HashTree genInitialGroups(Database db, HashTree groups, int numItems, int minSupCount) {
        // Return if there are no items to use to make candidate groups.
        if (numItems == 0) {
            System.out.println("Error: No items detected.");
            return null;
        }
        
        // Initialize the hash tree that will contain the maximal frequent itemsets.
        HashTree frequent = new HashTree();
        
        // Determine which items are frequent.
        ArrayList<FrequentItem> itemVectors = findFrequentItems(db, numItems, minSupCount);
        
        // Return if no items are frequent.
        if (itemVectors.size() == 0) {
            System.out.println("Error: No frequent items detected.");
            return null;
        }
        
        // Sort the frequent items from least to most frequent.
        Collections.sort(itemVectors);
        
        // Make each frequent item the head of a candidate group and make every
        // item that occurs more frequently the tail of that group.
        for (int i = 0; i < itemVectors.size() - 1; i++) {
            ArrayList<Integer> tail = new ArrayList<>();
            for (int j = (i + 1); j < itemVectors.size(); j++) {
                tail.add(itemVectors.get(j).getItem());
            }
            ArrayList<Integer> head = new ArrayList<>();
            head.add(itemVectors.get(i).getItem());
            Candidate cand = new Candidate(head, tail);
            
            // Use the head of each candidate group to hash that group into the candidate
            // hash tree.
            groups.add(cand.getHead(), cand);
        }
        
        // Make the most frequent item its own candidate group and place it in
        // the frequent hash tree.
        ArrayList<Integer> head = new ArrayList<>();
        head.add(itemVectors.get(itemVectors.size() - 1).getItem());
        Candidate c = new Candidate(head, new ArrayList<>());
        frequent.add(c.getHead(), c);
        
        // Return the frequent hash tree.
        return frequent;
    }
    
    /*
       Determines which items in the given database are frequent with respect to
       the given support count.
       @param db - the database to be scanned
       @param numItems - the number of distinct items in db
       @param minSupCount - the minimum support count used to determine which items are frequent
       @return an ArrayList containing each frequent item and its count
    */
    public static ArrayList<FrequentItem> findFrequentItems(Database db, int numItems, int minSupCount) {
        // Count occurrences of each item.
        int[] buckets = new int[numItems];
        db.genInitialCounts(buckets);
        
        // Place the frequent items and their counts in an ArrayList and return.
        ArrayList<FrequentItem> itemVectors = new ArrayList<>();
        for (int i = 0; i < buckets.length; i++) {
            if(buckets[i] >= minSupCount) {
                FrequentItem temp = new FrequentItem(i, buckets[i]);
                itemVectors.add(temp);
            }
        }
        return itemVectors;
    }
    
    /*
       Processes each candidate group in the search for maximal frequent itemsets. 
       If the group's head united with its tail is a frequent itemset, this itemset is added
       to the frequent hash tree. If not, several candidate groups, consisting of the 
       group's head united with each item in its tail, are added to the new candidate hash tree.
       However, the candidate group consisting of the head united with the tail's most
       frequent item is instead added to the frequent hash tree.
       @param cand - the hash tree containing the candidate groups
       @param newCand - the hash tree to contain the new candidate groups
       @param freq - the tree to contain the frequent itemsets
       @param minSupCount - the minimum support count used to determine which itemsets are frequent
    */
    public static void unpackCandidates(HashTree cand, HashTree newCand, HashTree freq, int minSupCount) {
        // Traverse the candidate hash tree.
        SearchByClass traverser = new SearchByClass(Candidate.class);
        cand.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) {
            
            // If the candidate group's head united with its tail is a frequent
            // itemset, add it to the frequent hash tree.
            Candidate temp = iterator.next();
            if (temp.getUnionCount() >= minSupCount) {
                ArrayList<Integer> frequent = new ArrayList<>();
              
                // The itemset is copied instead of being directly passed to the
                // hash tree to avoid changes made to the candidate tree affecting
                // the frequent tree.
                frequent.addAll(temp.union());
                Candidate c = new Candidate(frequent, new ArrayList<>());
                freq.add(c.getHead(), c);
            }
            else {
                
                // If the head united with the tail is infrequent, create new candidate
                // groups that consist of the head united with each item in the tail.
                // Place all except the one containing the most frequent tail item in 
                // the candidate hash tree. Place that one in the frequent hash tree.
                Candidate largestFrequent = temp.genSubNodes(newCand, minSupCount);
                freq.add(largestFrequent.getHead(), largestFrequent);
            }
        }
    }
    
    /*
      Determines whether an ArrayList of integers is a subset of another.
      @param subset - the ArrayList to be tested for being a subset of the other
      @param superset - the ArrayList to be tested for being a superset of the other
      @return a boolean value reflecting whether the possible subset is a subset of
      the possible superset
    */
    public static boolean subsetOf(ArrayList<Integer> subset, ArrayList<Integer> superset) {
        // Count how many items in subset are in superset.
        int supportCount = 0;
        for (int i = 0; i < subset.size(); i++) {
            if (superset.contains(subset.get(i))) {
                supportCount++;
            }
        }
        
        // If every item in subset is in superset, it must really be a subset.
        return supportCount == subset.size();
    }
    
    public static HashTree purgeFrequent(HashTree freq) {
        HashTree newFrequent = new HashTree();
        ArrayList<Candidate> freqContents = new ArrayList<>();
        
        SearchByClass traverser = new SearchByClass(Candidate.class);
        freq.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) {
            freqContents.add(iterator.next());
        }
        
        ArrayList<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < freqContents.size(); i++) {
            for (int j = i + 1; j < freqContents.size(); j++) {
                if (subsetOf(freqContents.get(i).getHead(), freqContents.get(j).getHead()) && !toRemove.contains(i)) {
                    toRemove.add(i);
                    break;
                }
                else if (subsetOf(freqContents.get(j).getHead(), freqContents.get(i).getHead()) && !toRemove.contains(j)) {
                    toRemove.add(j);
                }
            }
        }
        
        // Remove the items with the specified indices from the tail.
        for (int i = 0; i < toRemove.size(); i++) {
                   
            // Removing any items from the tail will change the indices of the other 
            // entries. Keep track of the number of items removed to compensate.
            freqContents.remove(toRemove.get(i) - i);
        }
        
        for (int i = 0; i < freqContents.size(); i++) {
            newFrequent.add(freqContents.get(i).getHead(), freqContents.get(i));
        }
        
        return newFrequent;
    }
    
    public static HashTree purgeCandidates(HashTree cand, HashTree freq) {
        HashTree newCand = new HashTree();
        
        ArrayList<Candidate> freqContents = new ArrayList<>();
        
        SearchByClass traverser = new SearchByClass(Candidate.class);
        freq.traverse(traverser);
        Iterator<Candidate> iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) {
            freqContents.add(iterator.next());
        }
        
        ArrayList<Candidate> candContents = new ArrayList<>();
        
        traverser = new SearchByClass(Candidate.class);
        cand.traverse(traverser);
        iterator = traverser.getSearchResults().iterator();
        
        while (iterator.hasNext()) {
            candContents.add(iterator.next());
        }
        
        ArrayList<Integer> toRemove = new ArrayList<>();
        
        for (int i = 0; i < candContents.size(); i++) {
            for (int j = 0; j < freqContents.size(); j++) {
                if (subsetOf(candContents.get(i).union(), freqContents.get(j).getHead()) && !toRemove.contains(i)) {
                    toRemove.add(i);
                    break;
                }
            }
        }
        
        // Remove the items with the specified indices from the tail.
        for (int i = 0; i < toRemove.size(); i++) {
                   
            // Removing any items from the tail will change the indices of the other 
            // entries. Keep track of the number of items removed to compensate.
            candContents.remove(toRemove.get(i) - i);
        }
        
        for (int i = 0; i < candContents.size(); i++) {
            newCand.add(candContents.get(i).getHead(), candContents.get(i));
        }
        
        return newCand;
    }
}