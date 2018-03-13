/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;

import java.util.ArrayList;

/**
 *
 * @author Elijah
 */
public class Candidate {
    private ArrayList<Integer> head;
    private ArrayList<Integer> tail;
    int count = 0;
    
    public Candidate(ArrayList<Integer> h, ArrayList<Integer> t) {
        head = h;
        tail = t;
    }
    
    public ArrayList<Integer> union() {
        ArrayList<Integer> union = new ArrayList<>();
        union.addAll(head);
        union.addAll(tail);
        return union;
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
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int num) {
        count = num;
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
        return message;
    }
}
