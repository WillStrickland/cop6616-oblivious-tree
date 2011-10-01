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
    
    private String[] deltas;
    private int datalen;
    
    public TwoThreeTreeNode()
    {
        deltas = new String[2];
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
    
    public String[] getDeltas()
    {                
        return this.deltas;
    }
    
    public int getDegree()
    {
        return this.degree;
    }
    
        
    public boolean setDelta(String delta)
    {
        boolean result;
        
        if(this.datalen >= 1)
        {
            result = false;
        }
        else
        {
            this.datalen++;
            this.deltas[this.datalen] = delta;
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
            this.deltas[this.datalen] = null;
            this.datalen--;
            result = true;
        }
        
        return result;
    }
}
