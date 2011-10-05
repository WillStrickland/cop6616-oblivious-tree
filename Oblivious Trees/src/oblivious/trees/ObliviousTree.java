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
    //1). Instantiate root node
    root = new OTree_Node();
    fileChunks = new Vector<OTree_Leaf>();
    
    //2). Generate leaf nodes from the byte array
    for(int read = 0; read < file.length; read += 10)
    {
        OTree_Leaf chunk = new OTree_Leaf(copyOfRange(file, read, read + 10));
        fileChunks.addElement(chunk);
    }
    
    //3). Create Oblivious Tree
    create(file);
  }
  
  /**
   * Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
   * and, after taking a number between two and three, generate a number of non-leaf, which 
   * is randomly chosen between 2 and 3.
   */
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
  
  public void add(OTree_Leaf newLeaf)
  {}
  
  public OTree_Leaf delete()
  {
     OTree_Leaf = deletedNode;
     
     return deletedNode;
  }
  
  
}