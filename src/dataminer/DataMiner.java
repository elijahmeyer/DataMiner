/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import org.apache.jorphan.collections.HashTree;
/**
 *
 * @author Elijah
 */
public class DataMiner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Initialize variables.
        //String filename = "C:\\Users\\Elijah\\Desktop\\T10I4D100K.txt";
        String filename = "C:\\Users\\Elijah\\Desktop\\testDataset.txt";
        
        Database database = new Database(filename);        
        if (database.getList() == null) {
            return;
        }
        
        int numItems = database.getNumItems();
        //final int SUPPORT_COUNT = (int) 0.05 * database.size();
        final int SUPPORT_COUNT = 2;
        
        // Count frequency of each item.
        int[] buckets = new int[numItems];
        database.genInitialCounts(buckets);
        
        // Convert frequent items into candidate groups.
        HashTree groups = new HashTree();
        HashTree frequent = genInitialGroups(database, groups, numItems, SUPPORT_COUNT);
        System.out.println("HashTable:");
        System.out.println(groups.toString());
        System.out.println("Frequent: ");
        System.out.println(frequent.toString());
    }
    
    public static HashTree genInitialGroups(Database db, HashTree groups, int numItems, int minSupCount) {
        if (numItems == 0) {
            System.out.println("Error: No items detected.");
            return null;
        }
        
        HashTree frequent = new HashTree();
        
        // Determine which items are frequent.
        ArrayList<FrequentItem> itemVectors = findFrequentItems(db, numItems, minSupCount);
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
            groups.add(cand.getHead(), cand);
        }
        
        // Make the most frequent item its own candidate group and place it in
        // the frequent hash tree.
        ArrayList<Integer> head = new ArrayList<>();
        head.add(itemVectors.get(itemVectors.size() - 1).getItem());
        Candidate f = new Candidate(head, new ArrayList<>());
        frequent.add(f);
        return frequent;
    }
    
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
}