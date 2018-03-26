/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;

import java.util.ArrayList;
import java.util.Collections;
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
    
    public Candidate genSubNodes(HashTree cand, int minSupCount) {
        System.out.println("Before:\n" + this.toString());
        this.pruneTail(minSupCount);
        System.out.println("After:\n" + this.toString());
        if (!tail.isEmpty()) {
            ArrayList<FrequentItem> tailItems = new ArrayList<>();
            for (int i = 0; i < tail.size(); i++) {
                FrequentItem temp = new FrequentItem(tail.get(i), tailBuckets.get(i));
                tailItems.add(temp);
            }
            Collections.sort(tailItems);
            for (int i = 0; i < tailItems.size() - 1; i++) {
                ArrayList<Integer> nextHead = new ArrayList<>();
                nextHead.addAll(head);
                nextHead.add(tailItems.get(i).getItem());
                
                ArrayList<Integer> nextTail = new ArrayList<>();
                for (int j = i + 1; j < tailItems.size(); j++) {
                    nextTail.add(tailItems.get(j).getItem());
                }
                Candidate nextCandidate = new Candidate(nextHead, nextTail);
                cand.add(nextCandidate.getHead(), nextCandidate);   
                System.out.println(nextCandidate.toString());
            }
            ArrayList<Integer> newHead = new ArrayList<>();
            newHead.addAll(head);
            newHead.add(tailItems.get(tailItems.size() - 1).getItem());
            Candidate next = new Candidate(newHead, new ArrayList<>());
            System.out.println(next.toString());
            return next;            
        }
        ArrayList<Integer> newHead = new ArrayList<>();
        newHead.addAll(head);
        Candidate c = new Candidate(newHead, new ArrayList<>());
        return c;
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
            tailBuckets.remove(removeTargets.get(i) - i);
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