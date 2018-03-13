/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Elijah
 */
public class Database {
    private ArrayList<ArrayList<Integer>> database = new ArrayList<>();
    private int numItems = 0;
    
    public Database(String filename) {
        File f = new File(filename);
        try {
            Scanner input = new Scanner(f);
            while (input.hasNextLine()) {
                String transactionString = input.nextLine();
                ArrayList<Integer> transaction = new ArrayList<>();
                String[] itemStrings = transactionString.split(" ");
                for (int i = 0; i < itemStrings.length; i++) {
                    int item = Integer.parseInt(itemStrings[i]);
                    transaction.add(item);
                    if (item > numItems) {
                        numItems = item;
                    }
                }
                database.add(transaction);
            }
            input.close();
            numItems++;
        } catch (Exception ex) {
            System.out.println(ex);
            database = null;
        }
    }
    
    public ArrayList<ArrayList<Integer>> getList() {
        return database;
    }
    
    public int getNumItems() {
        return numItems;
    }
    
    public int size() {
        return database.size();
    }
    
    public ArrayList<Integer> get(int index) {
        return database.get(index);
    }
    
    public int supportOf(ArrayList<Integer> itemset) {
        int supportCount = 0;
        int itemCount;
        for (int i = 0; i < database.size(); i++) {
            ArrayList<Integer> temp = database.get(i);
            itemCount = 0;
            for (int j = 0; j < temp.size(); j++) {
                if (itemCount == itemset.size()) {
                    break;
                }
                if (temp.get(j) == itemset.get(itemCount)) {
                    itemCount++;
                }
            }
            if (itemCount == itemset.size()) {
                supportCount++;
            }
        }
        return supportCount;
    }
    
    public void genInitialCounts(int[] items) {
        for (int i = 0; i < database.size(); i++) {
            ArrayList<Integer> transaction = database.get(i);
            for (int j = 0; j < transaction.size(); j++) {
                int item = transaction.get(j);
                items[item]++;
            }
        }
    }
}