/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;

/**
 *
 * @author Elijah
 */
public class FrequentItem implements Comparable<FrequentItem> {
    private int item;
    private int count;
    
    public FrequentItem(int i, int c) {
        item = i;
        count = c;
    }
    
    public int getItem() {
        return item;
    }
    
    public int getCount() {
        return count;
    }
    
    @Override
    public int compareTo(FrequentItem f) {
        if (count < f.getCount()) {
            return -1;
        }
        else if (count == f.getCount()) {
            return 0;
        }
        else {
            return 1;
        }
    }
}