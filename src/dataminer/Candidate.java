/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;

import java.util.ArrayList;
import org.apache.jorphan.collections.HashTree;

/**
 *
 * @author Elijah
 */
public class Candidate {
    private ArrayList<Integer> head = new ArrayList<>();
    private ArrayList<Integer> tail = new ArrayList<>();
    private ArrayList<Integer> tailBuckets = new ArrayList<>();
    private int count = 0;
    private int unionCount = 0;
    
    
    public Candidate(ArrayList<Integer> h, ArrayList<Integer> t) {
        head = h;
        tail = t;
        tailBuckets = genTailBuckets();
    }
    
    /*
       This method returns the union of the Candidate's head and tail lists.
       This helps find maximal frequent itemsets.
    */
    public ArrayList<Integer> union() {
        ArrayList<Integer> union = new ArrayList<>();
        union.addAll(head);
        union.addAll(tail);
        return union;
    }
    
    private ArrayList<Integer> genTailBuckets() {
        ArrayList<Integer> buckets = new ArrayList<>();
        for (int i = 0; i < tail.size(); i++) {
            buckets.add(0);
        }
        return buckets;
    }
    
    public void countSupport(ArrayList<Integer> transaction) {
        ArrayList<Integer> union = union();
        int itemCount = 0;
        for (int i = 0; i < union.size(); i++) {
            if (transaction.contains(union.get(i))) {
                itemCount++;
            }
        }
        if (itemCount == union.size()) {
            unionCount++;
        }
        
        for (int j = 0; j < tail.size(); j++) {
            ArrayList<Integer> temp = new ArrayList<>();
            temp.addAll(head);
            temp.add(tail.get(j));
            int subsetCount = 0;
            for (int k = 0; k < temp.size(); k++) {
                if (transaction.contains(temp.get(k))) {
                    subsetCount++;
                }
            }
            if (subsetCount == temp.size()) {
                tailBuckets.set(j, tailBuckets.get(j) + 1);
            }
        }
    }
    
    public Candidate genSubNodes(HashTree cand, HashTree frequent, int minSupCount) {
        if (!tail.isEmpty()) {
            pruneTail(minSupCount);
            // TODO: order the items in tail, add new Candidates to cand, and return the greatest
        }
        return new Candidate(head, new ArrayList<>());
    }
    
    public void pruneTail(int minSupCount) {
        ArrayList<Integer> removeTargets = new ArrayList<>();
        for (int i = 0; i < tailBuckets.size(); i++) {
            if (tailBuckets.get(i) < minSupCount) {
                removeTargets.add(i);
            }
        }
            
        for (int i = 0; i < removeTargets.size(); i++) {
                   
            // Removing any items from the ArrayList will change the indices of the other 
            // entries. Keep track of the number of items removed to compensate.
            tail.remove(removeTargets.get(i) - i);
        }
    }
    
    public void increment() {
        count++;
    }
    
    public ArrayList<Integer> getHead() {
        return head;
    }
    
    public ArrayList<Integer> getTail() {
        return tail;
    }
    
    public int getUnionCount() {
        return unionCount;
    }
            
    @Override
    public String toString() {
        String message = "Head: ";
        for (int i = 0; i < head.size(); i++) {
            message += head.get(i) + " ";
        }
        message += "\nTail: ";
        for (int j = 0; j < tail.size(); j++) {
            message += tail.get(j) + " ";
        }
        
        message += "\nTail Buckets: ";
        for (int k = 0; k < tailBuckets.size(); k++) {
            message += tailBuckets.get(k) + " ";
        }
        return message;
    }
}
