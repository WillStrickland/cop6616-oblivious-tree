/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oblivious.trees;
import java.util.Random;
import java.util.Arrays;
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
   private Vector treeNodes;
   
  public ObliviousTree(byte[] file)
  {    
    //1). Instantiate root node
    root = new OTree_Node();
    fileChunks = new Vector<OTree_Elem>();
    
    //2). Generate leaf nodes from the byte array           
    //3). Create Oblivious Tree
    generateLeaves(file);
    create();
  }
  
  /**
   * @param byte[] file
   * @return void
   * Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
   * and, after taking a number between two and three, generate a number of non-leaf, which
   * is randomly chosen between 2 and 3.
   */
  private void create()
  {
    Random rand = new Random();
    
    int degree = 0;
    int size = file.length;
    var treeNode;
    
    //do until there is only 1 node left
      nodesOnLevel = treeNodes.size;
      treeNode = new OTree_Node();
      for(int list = 0; list < nodesOnLevel)
      {
        degree = 2 + rand.nextInt(3);               
        
       
      }
  }
  
  private void generateLeaves(byte[] file)
  {
      long byteLen = file.length;
      long bytesRead = 0
    
      while(bytesRead < byteLen)
      {
        treeNodes.addElement(new OTree_Leaf(copyOfRange(file, bytesRead, bytesRead + 10));
        bytesRead += 10;
      } 
  }
  
  
  public void add(OTree_Leaf newLeaf)
  {
  }
  
  public OTree_Leaf delete()
  {
     OTree_Leaf = deletedNode;
     
     return deletedNode;
  }
  
  
}