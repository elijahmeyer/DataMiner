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
        ArrayList<ArrayList<Integer>> database = readInDatabase();
        if (database == null) {
            return;
        }
        
        for (int i = 0; i < database.size(); i++) {
            ArrayList<Integer> temp = database.get(i);
            for (int j = 0; j < temp.size(); j++) {
                System.out.print(temp.get(j) + " ");
            }
            System.out.println(": Line " + i);
        }
        HashTree ht = new HashTree();
    }
    
    public static ArrayList<ArrayList<Integer>> readInDatabase() {
        ArrayList<ArrayList<Integer>> db = new ArrayList<>();
        File f = new File("C:\\Users\\Elijah\\Desktop\\T10I4D100K.txt");
        try {
            Scanner input = new Scanner(f);
            while (input.hasNextLine()) {
                String transactionString = input.nextLine();
                ArrayList<Integer> transaction = new ArrayList<>();
                String[] itemStrings = transactionString.split(" ");
                for (int i = 0; i < itemStrings.length; i++) {
                    int item = Integer.parseInt(itemStrings[i]);
                    transaction.add(item);
                }
                db.add(transaction);
            }
            input.close();
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
        return db;
    }
}