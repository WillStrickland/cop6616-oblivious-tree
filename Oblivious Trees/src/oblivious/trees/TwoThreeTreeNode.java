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
    
    private String delta1;
    private String delta2;
    
    public TwoThreeTreeNode()
    {
        
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
        String[] data = new String[2];
        
        data[0] = this.delta1;
        data[1] = this.delta2;
                
        return data;
    }
    
    public int getDegree()
    {
        return this.degree;
    }
    
        
    public void setData()
    {
        
    }                                
}
