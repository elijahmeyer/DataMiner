/*
 * Elijah Meyer
 * CS 4710-01
 * Dr. Soon Chung
 * April 17, 2018
 * 
 * This file defines the Candidate class and its data fields and methods. 
 * The Candidate class represents the candidate groups used in the MaxMiner
 * algorithm. The class contains an ArrayList containing the head of the group,
 * an ArrayList containing the group's tail, an ArrayList containing the support
 * counts of the head united with each item in the tail, and the support count 
 * of the union of the head and the tail.
 */

package dataminer;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.jorphan.collections.HashTree;

public class Candidate 
{
    private ArrayList<Integer> head = new ArrayList<>();
    private ArrayList<Integer> tail = new ArrayList<>();
    private ArrayList<Integer> tailBuckets = new ArrayList<>();
    private int unionCount = 0;
    private final int lookaheadCount = 5;
    
    public Candidate(ArrayList<Integer> h, ArrayList<Integer> t) 
    {
        head = h;
        tail = t;
        
        // tailBuckets keeps track of the support count of the Candidate's head
        // united with each item in the tail.
        tailBuckets = genTailBuckets();
    }
    
    /*
       Returns the union of the Candidate's head and tail lists.
       This helps find maximal frequent itemsets.
       @return the Candidate's head united with its tail
    */
    public ArrayList<Integer> union() 
    {
        ArrayList<Integer> union = new ArrayList<>();
        union.addAll(head);
        
        if (tail.size() <= lookaheadCount) {
            union.addAll(tail);
        }
        else {
            for (int i = 0; i < lookaheadCount; i++) {
                union.add(tail.get(i));
            }
        }
        return union;
    }
    
    /*
       Creates an ArrayList with as many entries as there are items in the
       Candidate's tail. This ArrayList will be used to track the frequency of
       the Candidate's head united with each item in the tail.
       @return an ArrayList of the same size as the Candidate's tail
    */
    private ArrayList<Integer> genTailBuckets() 
    {
        ArrayList<Integer> buckets = new ArrayList<>();
        
        for (int i = 0; i < tail.size(); i++) 
        {
            buckets.add(0);
        }
        return buckets;
    }
    
    /*
       Determines whether the union of the Candidate's head and the first m tail items is in 
       the given transaction and whether the union of the head and each item
       in the tail is in the transaction. Increments the support counts of any
       itemsets that do appear in the transaction.
       @param transaction - the transaction to be scanned
       Precondition: the Candidate's head appears in the transaction
       (this precondition is assumed because this function is only called in
       this program if the head is already determined to appear in the given 
       transaction).
    */
    public void countSupport(ArrayList<Integer> transaction) 
    {
        int itemCount = 0;
        
        // Scan transaction for the Candidate's head and each item in its tail.
        for (int i = 0; i < tail.size(); i++) 
        {
            if (transaction.contains(tail.get(i))) 
            {
                // Determine whether every item in the Candidate's head and tail
                // is in the transaction.
                if (i < lookaheadCount) {
                    itemCount++;
                }
                
                // If the Candidate's head and an item in the tail appear in the 
                // transaction, increment the tail item's corresponding 
                // tailBucket.
                tailBuckets.set(i, tailBuckets.get(i) + 1);
            }
        }
        
        // If the count of items that appear in transaction is the same as the 
        // size of union, increment union count.
        if (itemCount == lookaheadCount || itemCount == tail.size()) 
        {
            unionCount++;
        }
    }
    
    public void loadTailItems() {
        if (tail.size() < lookaheadCount) {
            head.addAll(tail);
            tail.clear();
            tailBuckets.clear();
        }
        else {
            for (int i = 0; i < lookaheadCount; i++) {
                // Removing the first item from the tail and tailBuckets ArrayLists
                // will shift the index of all other items
                head.add(tail.remove(0));
                tailBuckets.remove(0);
            }
        }
    }
    
    /*
       Creates new Candidates and adds them to the candidate hash tree. Each 
       item in the tail that comprises a frequent itemset with the head is 
       added to the head of a new Candidate, along with the head of the old 
       Candidate. The tail of this new candidate consists of each item in the
       old tail with a higher support count when united with the head. The new
       Candidate with the most frequent old tail item in its head is returned 
       so it can be added to the frequent hash tree.
       @param cand - the hash tree to store the new Candidates
       @param minSupCount - the minimum support count used to determine which 
       itemsets are frequent
       @return the Candidate whose head contains the old head and the tail item
       that appeared in transactions with the old head most frequently, or, if 
       the old tail is empty, the Candidate's head will contain the old head
    */
    public Candidate genSubNodes(HashTree cand, int minSupCount) 
    {
        // Remove all items that did not create frequent itemsets when united
        // with the head from the tail.
        this.pruneTail(minSupCount);
        
        if (!tail.isEmpty()) 
        {
            
            // Create an ArrayList of item/support count pairs.
            ArrayList<FrequentItem> tailItems = new ArrayList<>();
            for (int i = 0; i < tail.size(); i++) 
            {
                FrequentItem temp = new FrequentItem(tail.get(i), tailBuckets.get(i));
                tailItems.add(temp);
            }
            
            // Sort this ArrayList from smallest to largest support count.
            Collections.sort(tailItems);
            
            // Create the heads of the new Candidates, composed of the old head
            // and one tail item.
            for (int i = 0; i < tailItems.size() - 1; i++) 
            {
                ArrayList<Integer> nextHead = new ArrayList<>();
                nextHead.addAll(head);
                nextHead.add(tailItems.get(i).getItem());
                
                // Create the tails of the new Candidates, composed of every
                // tail item with a larger support count than the tail item
                // added to the new head.
                ArrayList<Integer> nextTail = new ArrayList<>();
                for (int j = i + 1; j < tailItems.size(); j++) 
                {
                    nextTail.add(tailItems.get(j).getItem());
                }
                
                // Store the new Candidates in the candidate hash tree.
                Candidate nextCandidate = new Candidate(nextHead, nextTail);
                cand.add(nextCandidate.getHead(), nextCandidate);
                //System.out.println(nextCandidate.toString());
            }
            
            // Return the final Candidate, whose head is composed of the old 
            // head and the tail item that appears with the old head most
            // frequently.
            ArrayList<Integer> newHead = new ArrayList<>();
            newHead.addAll(head);
            newHead.add(tailItems.get(tailItems.size() - 1).getItem());
            Candidate next = new Candidate(newHead, new ArrayList<>());
            //System.out.println(next.toString());
            return next;            
        }
        
        // If the tail is empty, return a Candidate containing the old head.
        ArrayList<Integer> newHead = new ArrayList<>();
        newHead.addAll(head);
        Candidate c = new Candidate(newHead, new ArrayList<>());
        //System.out.println(c.toString());
        return c;
    }
    
    /*
       Removes items from the tail that did not form frequent itemsets when 
       united with the head.
       @param minSupCount - the minimum support count used to determine which 
       itemsets are frequent
    */
    public void pruneTail(int minSupCount) 
    {
        
        for (int i = 0; i < tailBuckets.size(); i++) 
        {
            if (tailBuckets.get(i) < minSupCount) 
            {
                
                // Remove non-frequent items from the tail and tailBuckets.
                tail.remove(i);
                tailBuckets.remove(i);
                
                // Removing an item from the ArrayList will decrement the 
                // indices of all later items. Decrement the loop counter to 
                // compensate.
                i--;
            }
        }
    }
    
    /*
       Returns the Candidate's head.
       @return - the head of the calling Candidate
    */
    public ArrayList<Integer> getHead() 
    {
        return head;
    }
    
    /*
       Returns the Candidate's tail.
       @return - the tail of the calling Candidate
    */
    public ArrayList<Integer> getTail() 
    {
        return tail;
    }
    
    public ArrayList<Integer> fullUnion() 
    {
        ArrayList<Integer> full = new ArrayList<>();
        full.addAll(head);
        full.addAll(tail);
        return full;
    }
    
    /*
       Returns the support count of the Candidate's head united with its tail.
       @return - the count of the calling Candidate's head united with its tail
    */
    public int getUnionCount() 
    {
        return unionCount;
    }
            
    /*
       Returns a String representation of the Candidate.
       @return - the contents of the Candidate's head, tail, and tailBuckets in
       String form.
    */
    @Override
    public String toString() 
    {
        // Include every item in the head.
        String message = "Head: ";
        for (int i = 0; i < head.size(); i++) 
        {
            message += head.get(i) + " ";
        }
        
        // Include every item in the tail.
        message += "\nTail: ";
        for (int j = 0; j < tail.size(); j++) 
        {
            message += tail.get(j) + " ";
        }
        
        // Include the tail buckets.
        message += "\nTail Buckets: ";
        for (int k = 0; k < tailBuckets.size(); k++)
        {
            message += tailBuckets.get(k) + " ";
        }
        return message;
    }
}