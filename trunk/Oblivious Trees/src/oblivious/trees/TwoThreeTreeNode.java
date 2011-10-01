/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oblivious.trees;

/**
 *
 * @author ch647274
 */
public class TwoThreeTreeNode 
{    
    private TwoThreeTreeNode parent;
    private TwoThreeTreeNode leftChild;
    private TwoThreeTreeNode middleChild;
    private TwoThreeTreeNode rightChild;
    private int degree;
    
    //implement delta as Comparable
    //compare() returns based on position
    //equals() returns based on position and value
    private FileChunk[] pieces;
    private int datalen; 
    
    public TwoThreeTreeNode()
    {
        pieces = new FileChunk[2];
        datalen = -1;
    }
    
    public TwoThreeTreeNode getParent()
    {
        return this.parent;
    }
    
    public TwoThreeTreeNode[] getChildren()
    {
        TwoThreeTreeNode[] children = new TwoThreeTreeNode[3];
        
        children[0] = this.leftChild;
        children[1] = this.middleChild;
        children[2] = this.rightChild;
                
        return children;
    }
    
    public FileChunk[] getPieces()
    {                
        return this.pieces;
    }
    
    public int getDegree()
    {
        return this.degree;
    }
    
        
    public boolean setPiece(FileChunk piece)
    {
        boolean result;
        
        if(this.datalen >= 1)
        {
            result = false;
        }
        else
        {
            this.datalen++;
            this.pieces[this.datalen] = piece;
            result = true;
        }
        
        return result;
    }
    
    public boolean removeDelta()
    {
        boolean result;
        
        if(this.datalen < 0)
        {
            result = false;
        }
        else
        {
            this.pieces[this.datalen] = null;
            this.datalen--;
            result = true;
        }
        
        return result;
    }
    
    public String toString()
    {
        String description = "";
        
        return description;
    }
}
