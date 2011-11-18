package oblivious.sequential;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import oblivious.ObliviousTree;

/** Oblivious Tree - COP 6616
 * @author William Strickland and Chris Fontaine
 * @version Sequential Implementation
 */
public class SequentialObliviousTree extends oblivious.ObliviousTree{

	/* Class Properties */
	private static final Random rndSrc = initPRNG();				// Random source for creating obliviousness
	
	/* Instance Properties */
	private OTree_Node root;	// root node of tree
	private ArrayList<OTree_Elem> treeNodes;	// list of nodes and leaves for rapid access
	
	/** Constructor generates empty initial tree.
	 */
	public SequentialObliviousTree(){
		root = new OTree_Node();
	}
	/** Constructor generates initial leaf node using using a given input file.
	 */
	public SequentialObliviousTree(FileInputStream file, Signature signer){
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new ArrayList<OTree_Elem>();
		//2). Generate leaf nodes from the byte array
		generateLeaves(file, signer);
		//3). Create Oblivious Tree
		create(signer);
	}
	/** Constructor generates initial leaf node using using a given input byte array.
	 */
	public SequentialObliviousTree(byte[] file, Signature signer){
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new ArrayList<OTree_Elem>();
		//2). Generate leaf nodes from the byte array
		generateLeaves(file, signer);
		//3). Create Oblivious Tree
		create(signer);
	}
	
	
	
	/** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 *  and, after taking a number between two and three, generate a number of non-leaf, which
	 *  @param byte[] file
	 *  @return void
	 */
	private synchronized void generateLeaves(FileInputStream file, Signature signer){	
		int this_size;
		byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
		treeNodes.clear();
		try {
			// loop until reaches end of file
			while(true){
				this_size = file.read(chunk);
				OTree_Leaf newLeaf = new OTree_Leaf();
				signer.update(chunk, 0, this_size);
				newLeaf.setSig(signer.sign());
				treeNodes.add(newLeaf);
				
				// if less than whole chunk, reached end of file
				if (this_size < ObliviousTree.CHUNK_SIZE){
					// break out of loop
					break;
				}
			}
		} catch (Exception e){
			return;
		}
	} //*/
	/** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 *  and, after taking a number between two and three, generate a number of non-leaf, which
	 *  @param byte[] file
	 *  @return void
	 */
	private synchronized void generateLeaves(byte[] file, Signature signer){	
		int this_size=0;
		// clear current leaves
		treeNodes.clear();
		try {
			// loop until reaches end of file
			for(int i=0; i<file.length; i+=this_size){
				OTree_Leaf newLeaf = new OTree_Leaf();
				this_size = (file.length-i>ObliviousTree.CHUNK_SIZE) ? ObliviousTree.CHUNK_SIZE : file.length-i;
				signer.update(file, 0, this_size);
				newLeaf.setSig(signer.sign());
				treeNodes.add(newLeaf);
			}
		} catch (Exception e){
			return;
		}
	} //*/
	
        /** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
         *  and, after taking a number between two and three, generate a number of non-leaf, which
         */
        private synchronized final void create(Signature signer)
        {
                int randomDegree;
                int numOfNodesAtLevel;
                int nodesAdded = 0;
                int nodeIndex = 0;
                int addCount;
                int traversingLevel;
                OTree_Node tempNode;
                
                /*
                 * Holds the nodes contained at the previous level. It is 
                 * instantiated with the nodes at the leaf level
                 */
                ArrayList<OTree_Elem> previousLevel = treeNodes;
                /*
                 * Holds the nodes being added to the current level.
                 */
                ArrayList<OTree_Elem> currentLevel;
                
                while(previousLevel.size() > 1)
                {
                    currentLevel = new ArrayList<OTree_Elem>();
                    numOfNodesAtLevel = previousLevel.size();
                    
                    for(addCount = 0; addCount < numOfNodesAtLevel; addCount += randomDegree)
                    {
                        randomDegree = (rndSrc.nextBoolean()) ? 2 : 3;
                        randomDegree = (((addCount + randomDegree) + 1) > numOfNodesAtLevel) ? ((addCount + randomDegree) + 1) - numOfNodesAtLevel : randomDegree;
                        tempNode = new OTree_Node();
                        
                        switch(randomDegree)
                        {
                            case 1:
                                previousLevel.get(addCount).setParent(tempNode);
                                tempNode.addChild(previousLevel.get(addCount));
                                break;
                            case 2:                                
                                previousLevel.get(addCount).setParent(tempNode);
                                tempNode.addChild(previousLevel.get(addCount));
                                previousLevel.get(addCount + 1).setParent(tempNode);
                                tempNode.addChild(previousLevel.get(addCount + 1));
                                break;
                            case 3:
                                previousLevel.get(addCount).setParent(tempNode);
                                tempNode.addChild(previousLevel.get(addCount));
                                previousLevel.get(addCount + 1).setParent(tempNode);
                                tempNode.addChild(previousLevel.get(addCount + 1));
                                previousLevel.get(addCount + 2).setParent(tempNode);
                                tempNode.addChild(previousLevel.get(addCount + 2));
                                break;
                        }
                        
                        try
                        {
                            currentLevel.get(currentLevel.size()-1).setNeighbor(tempNode);
                        }
                        catch(NoSuchElementException e)
                        {
                        }
                        
                        currentLevel.add(tempNode);
                    }
                    
                    previousLevel = currentLevel;
                }
                
                try
                {
                    root = (OTree_Node)previousLevel.get(0);
                }
                catch(NoSuchElementException e)
                {                    
                }
        }
        
        /** Inserts a new leaf into the ith position of the leaf level, then
         *  re-randomized the tree based on the optimized insert algorithm
         *  presented in the paper. In order to insert a new node, you must 
         *  provide data (in the form of a byte array) and a position.
         *  @param byte[] value data value to be inserted
         *  @param int i index of chunk/leaf to insert into
         *  @param Signature signer signature for signing new/randomized tree nodes.
         *  @return void
         */
        public synchronized void insert(byte[] value, int i, Signature signer)
        {
            i = i - 1;
            OTree_Leaf ithLeaf = (OTree_Leaf)treeNodes.get(i);
            OTree_Leaf newLeaf = new OTree_Leaf();
            OTree_Node ithParent = (OTree_Node)ithLeaf.getParent();
            OTree_Node currentNode, newNode, newRoot, neighbor;
            int w, randomDegree, oldDegree;
            ArrayList<OTree_Elem> toUpdate = new ArrayList<OTree_Elem>();
            
            newLeaf.setSig(value);
            ithParent.addChild(newLeaf);
            newLeaf.setParent(ithParent);
            treeNodes.add(i, newLeaf);
            
            randomDegree = (rndSrc.nextBoolean()) ? 2 : 3;
            
            /*
             * The algorithm keeps going up level by level until we pass the
             * root, at which point we stop
             */
            while(ithParent.getParent() != null)
            {
                ithParent = (OTree_Node)ithParent.getParent();
                currentNode = ithParent;
                toUpdate.add(currentNode);
                
                if(ithParent.getNeighbor() == null)
                {
                    /*
                     * If the node has no neighbor, then it is a node on the right
                     * spine, which means we treat it as a 'special case'.
                     */
                    if(ithParent.getDegree() == 2 || (ithParent.getDegree() == 3 && randomDegree == 3))
                    {
                        newNode = new OTree_Node();
                        newRoot = new OTree_Node();
                        
                        root.setNeighbor(newNode);
                        newNode.addChild(root.getChild(root.getDegree() - 1));
                        root.getChild(root.getDegree() - 1).setParent(newNode);
                        root.removeChild(root.getDegree() - 1);
                        
                        newRoot.addChild(root);
                        newRoot.addChild(newNode);
                        root.setParent(newRoot);
                        newNode.setParent(newRoot);
                        root = newRoot;
                        toUpdate.add(root);
                        
                        /*
                         * According to the Structural Agreement lemma, we're 
                         * finished because all nodes are accounted for. We just
                         * need to update the size information along the path
                         * from the leaf to the root.
                         */
                    }
                    
                }
                else
                {
                    w = 1;
                    
                    while(w > 0)
                    {
                        randomDegree = (rndSrc.nextBoolean()) ? 2 : 3;
                        
                        if(randomDegree == w || currentNode.getNeighbor() == null)
                        {
                            /*
                             * If you've hit the end of the level and you still
                             * have nodes accounted for, then you need to create
                             * a new node and attach any outstanding nodes to 
                             * it as its children. This effectively increases 
                             * the 'span' of the level by 1.
                             */
                            newNode = new OTree_Node();
                            currentNode.setNeighbor(newNode);
                            toUpdate.add(newNode);
                            for(int migrate = 0; migrate < w; migrate++)
                            {
                                newNode.addChild(currentNode.getChild(currentNode.getDegree() - 1));
                                currentNode.getChild(currentNode.getDegree() - 1).setParent(newNode);
                                currentNode.removeChild(currentNode.getDegree() - 1);
                            }
                            
                            w = 0;
                        }
                        else
                        {
                            neighbor = (OTree_Node)currentNode.getNeighbor();
                            toUpdate.add(neighbor);
                            /*
                             * randomDegree, in this case, is equivalent to a 
                             * newDegree for the node.
                             */
                            //randomDegree = (rndSrc.nextBoolean()) ? 2 : 3;
                            oldDegree = neighbor.getDegree();
                            
                            for(int migrate = 0; migrate < w; migrate++)
                            {
                                neighbor.addChild(currentNode.getChild(currentNode.getDegree() - 1));
                                currentNode.getChild(currentNode.getDegree() - 1).setParent(neighbor);
                                currentNode.removeChild(currentNode.getDegree() - 1);
                            }
                            
                            /*
                             * Take the original degree of the node, add however
                             * many nodes you added to it, and subtract it by 
                             * its new degree. If this is 0, then all nodes are 
                             * accounted for (AT THAT LEVEL).
                             * 
                             * Ex: If the node originally had degree 3, and you
                             * roll a new degree of 3, then you'll wind up with
                             * an extra node floating around. So you have to 
                             * keep going.
                             */
                            w = java.lang.Math.max(0, ((oldDegree + w) - randomDegree));
                            currentNode = neighbor;
                        }
                    }
                }
            }
            // update signatures of all nodes touched in this operation
            updateSig(toUpdate, signer);
        }
        
        /** deletes the leaf the ith position of the leaf level, then
         *  re-randomized the tree based on the optimized delete algorithm
         *  presented in the paper. In order to delete a node, you must provide a position.
         *  @param int i index of chunk/leaf to insert into
         *  @param Signature signer signature for signing new/randomized tree nodes.
         *  @return void
         */
        public synchronized void delete(int i,  Signature signer)
        {
            i = i - 1;
            OTree_Elem ithChild = treeNodes.get(i);
            OTree_Elem[] ithParentChildren;
            OTree_Node ithParent = (OTree_Node)ithChild.getParent();
            OTree_Node currentNode, neighbor;
            int w, randomDegree, oldDegree;
            ArrayList<OTree_Elem> toUpdate = new ArrayList<OTree_Elem>();
            
            while(ithParent.getParent() != null)
            {
                currentNode = ithParent;
                ithParentChildren = ithParent.getChildren();
                toUpdate.add(currentNode);
                
                for(int match = 0; match < ithParentChildren.length; i++)
                {
                    if(ithChild == ithParentChildren[i])
                    {
                        ithParent.removeChild(i);
                        break;
                    }
                }
                
                if(ithParent.getNeighbor() == null)
                {
                    if(!(ithParent.getDegree() < 1))
                    {
                        root = (OTree_Node)ithParent.getChild(0);
                    }
                }
                else
                {
                    w = 1;
                    
                    while(w > 0)
                    {
                        randomDegree = (rndSrc.nextBoolean()) ? 2 : 3;
                        neighbor = (OTree_Node)currentNode.getNeighbor();
                        oldDegree = neighbor.getDegree();
                        toUpdate.add(neighbor);
                        
                        if(w >= oldDegree)
                        {
                            OTree_Elem[] neighborChildren = neighbor.getChildren();
                            
                            for(int migrate = 0; migrate < neighborChildren.length; migrate++)
                            {
                                currentNode.addChild(neighborChildren[i]);
                                neighborChildren[i].setParent(currentNode);
                                neighbor.removeChild(0);
                            }
                            
                            w = 0;
                        }
                        else
                        {
                            for(int migrate = 0; migrate < w; migrate++)
                            {
                                currentNode.addChild(neighbor.getChild(0));
                                neighbor.getChild(0).setParent(currentNode);
                                neighbor.removeChild(0);
                            }
                            
                            w = randomDegree - oldDegree + w;
                        }
                    }
                }
                
                ithChild = ithParent;
                ithParent = (OTree_Node)ithParent.getParent();
            }
            // update signatures of all nodes touched in this operation
            updateSig(toUpdate, signer);
        }
        

        
        /**
         * Returns either the node following the given node at its given level
         * (its level neighbor), or null (which means its the last node of that 
         * level)
         * @param OTree_Node node
         * @param int level
         * @return OTree_Node neighbor
         */
        public synchronized OTree_Node getNeighbor(OTree_Node node, int level)
        {
            OTree_Node parent = (OTree_Node)node.getParent();
            OTree_Node previous = node;
            OTree_Node neighbor;
            int levelCounter = level;
            boolean loop = true;
            
            /*
             * There are 2 stop conditions:
             * 
             * If the parent is equal to null, this means you've gone past the
             * root. This indicates that the given node has no neighbor and it
             * is the last node of its level.
             * 
             * If the node you've reached has the same level as the given node,
             * you've found the neighbor.
             */
            
            /*
             * I need to eliminate this while() at some point.
             * 
             */
            while(loop)
            {
                /*
                 * If the parent is null, you've gone the past the root.
                 * Return 'No Neighbor'.
                 */
                if(parent == null)
                {
                    return null;
                }
                else
                {
                    /*
                     * Is the current node the last child of its parent?
                     */
                    if(parent.getChild(parent.getDegree() - 1) == previous)
                    {
                        /*
                         * If it is, you need to go up one more level (on the
                         * path from the root to the leaf node).
                         */
                        previous = parent;
                        parent = (OTree_Node)parent.getParent();
                        /*
                         * Decrement the level counter so you know how far up
                         * the tree you are.
                         */
                        levelCounter--;
                    }
                    else
                    {
                        /*
                         * If the current node is NOT the last child of its 
                         * parent, then you've gone far enough up the tree that
                         * you can stop.
                         */
                        loop = false;
                    }
                }                
            }
            
            /*
             * Set the current neighbor to the next sibling of the current node.
             * Will's getNextSibling() function is quite useful here.
             *
             */
            neighbor = (OTree_Node)previous.getNextSibling();
            /* 
             * Its quite possible the given node is the first or middle child of  
             * a sub-tree of degree 2 or 3. In that case, the levelCounter never
             * decremented in the previous loop, and its next sibling is its
             * next neighbor.
             */            
            while(levelCounter != level)
            {
                /*
                 * Otherwise, keep going left until you reach the same level
                 * as the given node.
                 */
                neighbor = (OTree_Node)neighbor.getChild(0);
                levelCounter++;                
            }
            
            return neighbor;            
        }
        
	/** get number of chunks in OTree
	 * @return int count of leafnodes/chunks in oblivious tree
	 */
	public synchronized int getSize(){
		//return root.getLeafCnt();		// tree version
		return this.treeNodes.size();	// vector version
	}
	
	/** Update the signature for all the OTree_Elem in list.
	  * Method designed to operate only on internal nodes.
	  * make sure that the collection returns elements such that children will be processed before their parent
	  * @param l collection that holds nodes to be updated (children first)
	  * @param signer signature for signing must be initialized for signing
	  * @return true if successful, false if failure
	  */
	private static boolean updateSig(List<OTree_Elem> l, Signature signer){
		// run test to make sure properly assigning update list
		updateSigCheck(l);
		// for node each in collection
		for (OTree_Elem n : l){
			// run update on this node
			// return false if failed
			if (!updateSig(n,signer)){
				return false;
			}
		}
		// return true if all succeed
		return true;
	}
	/** Update the signature for a single OTree_Elem.
	  * Method designed to operate only on internal nodes.
	  * @param n OTree_Elem (that has children)
	  * @param signer signature for signing must be initialized for signing
	  * @return true if successful, false if failure
	  */
	private static boolean updateSig(OTree_Elem n, Signature signer){
		try {
			// get children set
			OTree_Elem[] C = n.getChildren();
			// if has children
			if (C != null && C.length>0){
				// compile signature from children from left to right. 
				for (OTree_Elem c : C){
					signer.update(c.getSig());
				}
				// finish signature computation at set into this node
				n.setSig(signer.sign());
			}
			return true;
		} catch (SignatureException e) {
			return false;
		}
	}
	/** Helper method to view the contents of the list sent to updateSig().
	 *  Prints list values in iterator order to screen with count
	 *  of times they occur in the list.
	 *  @param list list of OTree_Elem to check
	 */
	private static void updateSigCheck(List<OTree_Elem> list){
		System.out.println("UpdateSigCheck");
		for (OTree_Elem a : list){
			int cnt =0;
			for (OTree_Elem b : list){
				if(a==b){
					cnt++;
				}
			}
			System.out.println(a.toString()+" cnt="+cnt);
		}
			
		
	}
	/** Verify the signature for all the OTree_Elem in list.
	  * Method designed to operate only on internal nodes.
	  * no limits imposed on order of nodes
	  * @param l collection that holds nodes to be updated (children first)
	  * @param verifier signature for verification must be initialized for verification
	  * @return true if successful, false if failure
	  */
	@SuppressWarnings("unused")
	private static boolean verifySig(Collection<OTree_Elem> l, Signature verifier){
		// for node each in collection
		for (OTree_Elem n : l){
			// run update on this node
			// return false if verify failed
			if (!verifySig(n,verifier)){
				return false;
			}
		}
		// return true if all succeeded
		return true;
	}
	/** Verify the signature for a single OTree_Elem.
	  * Method designed to operate only on internal nodes.
	  * @param n OTree_Elem to be verified
	  * @param verifier signature for verification must be initialized for verification
	  * @return true if successful, false if failure
	  */
	private static boolean verifySig(OTree_Elem n, Signature verifier){
		try {
			// get children set
			OTree_Elem[] C = n.getChildren();
			// if has children
			if (C != null && C.length>0){
				// compile data from children from left to right. 
				for (OTree_Elem c : C){
					verifier.update(c.getSig());
				}
				// finish verification and return result
				return verifier.verify(n.getSig());
			} else {
				return true;
			}
		} catch (SignatureException e) {
			return false;
		}
	}
	public synchronized boolean verifyTree(Signature verifier){
		return verifyTree(this.root, verifier);
	}
	/** Function for checking 2-3 oblivious tree structure, recursive version
	 *  @param verifier signature to be used to check
	 *  @return true if valid, false if invalid
	 */
	private synchronized boolean verifyTree(OTree_Elem e, Signature verifier){
		// if no children don't continue check - success
		if (e.getDegree()>0){
			return true;
		}
		// else if to many children - failure
		else if(e.getDegree()<3){
			return false;
		}
		// check signature for this node
		boolean result = verifySig(e, verifier);
		if (result){
			//check child signatures recursively
			for (OTree_Elem c : e.getChildren()){
				// if invalid
				if(!verifyTree(c, verifier)){
					// reset result and break loop
					result = false;
					break;
				}
			}
		}
		return result ; 
	}
	
	/** generate the signature output of algorithm
	 * outputs each node in signature as {sig_size}{sig}{degree} in depth-first preorder
	 *  @return byte[] of current complete signature, null if failure
	 */
 	public synchronized byte[] signatureGenerate(){
		// Initialize output holder; index at 0, initial size of 128 bytes
		ObliviousTree.ByteOutArray sig = new ObliviousTree.ByteOutArray(0, 128);
		SequentialObliviousTree.signatureGenerateRecurse(this.root, sig);
		// return truncated array of just signatures
		return Arrays.copyOf(sig.data, sig.index);
	} //*/
	/** recursive function to traverse tree and compile complete signature
	 *  @param thisNode current node
	 *  @param sig SignatureArray object holding current state
	 */
	private static void signatureGenerateRecurse(OTree_Elem thisNode, ObliviousTree.ByteOutArray sig){
		ByteBuffer buf = ByteBuffer.allocate(4);	// bytebuffer for doing int to byte[] conversions
		byte[] tmp;	// temporary array for holding byte rep of each node
		
		// get byte signature of current node
		tmp = thisNode.getSig();
		// fix size to accept this node
		sig.append(tmp.length+8);
		// write node into sig data
		// prepend with signature size
		buf.putInt(0, tmp.length);
		// copy signature into sig data
		System.arraycopy(buf.array(), 0, sig.data, sig.index, 4);
		buf.clear();
		sig.index+=4;
		// copy signature into sig data
		System.arraycopy(tmp, 0, sig.data, sig.index, tmp.length);
		sig.index += tmp.length;
		// append with degree
		buf.putInt(0, thisNode.getDegree());
		System.arraycopy(buf.array(), 0, sig.data, sig.index, 4);
		//buf.clear();
		sig.index+=4;
		
		// call for each child (left to right)
		if(thisNode.getDegree()>0){
			for (OTree_Elem c : Arrays.asList(thisNode.getChildren())){
				signatureGenerateRecurse(c, sig);
			}	
		}
		
	}
}