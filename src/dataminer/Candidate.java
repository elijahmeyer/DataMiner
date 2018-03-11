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
    
    public Candidate(ArrayList<Integer> h, int itemCount) {
        head = h;
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = (h.get(h.size() - 1) + 1); i < itemCount; i++) {
            temp.add(i);
        }
        tail = temp;
    }
    
    public ArrayList<Integer> union() {
        ArrayList<Integer> union = new ArrayList<>();
        union.addAll(head);
        union.addAll(tail);
        return union;
    }
    
    public ArrayList<Integer> getHead() {
        return head;
    }
    
    public ArrayList<Integer> getTail() {
        return tail;
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
