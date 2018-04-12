/*
 * Elijah Meyer
 * CS 4710-01
 * Dr. Soon Chung
 * April 17, 2018
 *
 * This class defines the FrequentItem class and its data fields and methods.
 * This class contains an item and a count of that item's frequency in order to
 * sort items in order of their frequency.
 */ 
package dataminer;

public class FrequentItem implements Comparable<FrequentItem> 
{
    private int item;
    private int count;
    
    public FrequentItem(int i, int c) 
    {
        item = i;
        count = c;
    }

    /*
       Returns the item stored in this FrequentItem.
       @return the item
    */
    public int getItem()
    {
        return item;
    }
    
    /*
       Returns the frequency of the item stored in this FrequentItem.
       @return the item's frequency
    */
    public int getCount() 
    {
        return count;
    }
    
    /*
       Compares two FrequentItems. Used to sort an ArrayList of FrequentItems 
       in ascending order of frequency.
       @param f - the FrequentItem to be compared to the calling FrequentItem
       @return an integer reflecting whether the calling FrequentItem occurs
       more frequently, less frequently, or at the same frequency as the 
       FrequentItem submitted as a parameter
    */
    @Override
    public int compareTo(FrequentItem f) 
    {
        if (count < f.getCount()) 
        {
            return -1;
        }
        else if (count == f.getCount()) 
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
}