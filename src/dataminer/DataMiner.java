/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataminer;
import java.io.File;
import java.util.ArrayList;
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
        //String filename = "C:\\Users\\Elijah\\Desktop\\T10I4D100K.txt";
        String filename = "C:\\Users\\Elijah\\Desktop\\testDataset.txt";
        Database database = new Database(filename);        
        if (database.getList() == null) {
            return;
        }
        
        int numItems = database.getNumItems();
        final int SUPPORT_COUNT = (int) 0.05 * database.size();
        
        int[] buckets = new int[numItems];
        database.genInitialCounts(buckets);
        for (int i = 0; i < buckets.length; i++) {
            System.out.print(buckets[i] + " ");
        }
        System.out.println("");
        
        
        HashTree groups = new HashTree();
        HashTree frequent = genInitialGroups(database, groups, numItems, SUPPORT_COUNT);
    }
    
    public HashTree genInitialGroups(Database db, HashTree groups, int numItems, int minSupCount) {
        int[] buckets = new int[numItems];
        db.genInitialCounts(buckets);
        ArrayList<int[]> itemVectors = new ArrayList<>();
        for (int i = 0; i < buckets.length; i++) {
            if(buckets[i] >= minSupCount) {
                int[] temp = new int[2];
                temp[0] = i;
                temp[1] = buckets[i];
                itemVectors.add(temp);
            }
        }
        
        
    }
    
}