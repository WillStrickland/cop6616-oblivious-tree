/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oblivious.trees;
import java.util.Random;
/**
 *
 * William Strickland and Chris Fontaine
 */
public class ObliviousTree {

    /*
    public static void main(String[] args) {
        System.out.println("Hello, World!!!");
    }
    */
   
  /**
   * Constructor generates initial leaf node using
   * using a given input file.
   *
   */
   
   private OTree_Node root;
   private Vector fileChunks;
   
  public ObliviousTree(byte[] file)
  {    
    root = new OTree_Node();
    fileChunks = new Vector<OTree_Leaf>();
    
    for(int read = 0; read < file.length - 1; read += 10)
    {
        OTree_Leaf chunk = new OTree_Leaf(copyOfRange(file, read, read + 10));
        fileChunks.addElement(chunk);
    }
    
    create(file);
  }
  
  private void create(byte[] file)
  {
    Random rand = new Random();
    
    int degree = 0;
    int size = file.length;
    
    for(int list = 0; list < size - 1; list += degree)
    {
      degree = 2 + rand.nextInt(3);
      
      
    }
  }
  
  
}