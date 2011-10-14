package oblivious.trees;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

/** Oblivious Tree - COP 6616
 * @author William Strickland and Chris Fontaine
 * @version Sequential Implementation
 */
public class ObliviousTree {

	/*
	public static void main(String[] args) {
		System.out.println("Hello, World!!!");
		System.out.println("chunkSize = "+ObliviousTree.CHUNK_SIZE);
		System.out.println("PRNG = "+ObliviousTree.PRNG_Info());
		System.out.println("DIGEST = "+ObliviousTree.Digest_Info());
		//System.out.println("initPRNG = "+ObliviousTree.initPRNG());
		System.out.println("mk oblivioustree");
		ObliviousTree O = new ObliviousTree();
		System.out.println("PRNG = "+ObliviousTree.PRNG_Info());
		System.out.println("DIGEST = "+ObliviousTree.Digest_Info());

	} //*/

	/**
	 * Constructor generates initial leaf node using
	 * using a given input file.
	 *
	 */
	
	/* Class Properties */
	private static SecureRandom rndSrc;			// Random source for creating obliviousness
	private static MessageDigest digest;		// Secure hashing function for leaves
	public final static int CHUNK_SIZE = 1000;	// File chunk size in bytes
	
	/* Instance Properties */
	private OTree_Node root;	// root node of tree
	private Vector<OTree_Elem> treeNodes;	// list of nodes and leaves for rapid access

	
	public ObliviousTree(){
		// Initialize crypto stuff
		initPRNG();
		initDigest();
		root = new OTree_Node();
	}
	
	public ObliviousTree(FileInputStream file)
	{
		// Initialize crypto stuff
		initPRNG();
		initDigest();
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//2). Generate leaf nodes from the byte array
		generateLeaves(file);
		//3). Create Oblivious Tree
		generateTree();

	}
	
	// Setup of cryptographic objects - PRNG for randomness and Message Digest for signatures
	/** Initialize psuedorandom number generator for class if not already initialized.
	 *  @return true if successful, else false
	 */
	private static boolean initPRNG(){	
		if (rndSrc==null){
			try {
				rndSrc = SecureRandom.getInstance("SHA1PRNG");
				byte[] b = new byte[1];
				rndSrc.nextBytes(b);
			} catch (NoSuchAlgorithmException e){
				return false;
			}
		}
		return true;
	}
	/** get information about psuedorandom number generator used.
	 *  @return String describing psuedorandom number generator algorithm
	 */
	public static String PRNG_Info(){
		if (rndSrc != null){
			return rndSrc.getAlgorithm()+ " - " + rndSrc.getProvider().toString();
		} else {
			return "PRNG not initialized!";
		}
	}
	/** Initialize message digest for class if not already initialized.
	 *  @return true if successful, else false
	 */
	private static boolean initDigest(){	
		if (digest==null){
			try {
				digest = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e){
				return false;
			}
		}
		return true;
	}
	/** get information about message digest used.
	 *  @return String describing message digest algorithm
	 */
	public static String Digest_Info(){
		if (digest != null){
			return digest.getAlgorithm()+ " - " + digest.getProvider().toString();
		} else {
			return "Digest not initialized!";
		}
	}
	
	/** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 *  and, after taking a number between two and three, generate a number of non-leaf, which
	 *  @param byte[] file
	 *  @return void
	 */
	private synchronized void generateLeaves(FileInputStream file)
	{	
		int this_size;
		byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
		treeNodes.clear();
		try {
			// loop until reaches end of file
			while(true){
				this_size = file.read(chunk);
				OTree_Leaf newLeaf = new OTree_Leaf();
				newLeaf.setSig(ObliviousTree.digest.digest(Arrays.copyOf(chunk,this_size)));
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
	 */
	private synchronized void generateTree()
	{		
		int randomDegree;
		//The initial level will be the leaves that were added using the
		//generateLeaves function
		int numOfNodesAtLevel = treeNodes.size();
		//This keeps track of how many nodes we add to a particular level
		int nodesAdded = 0;
		//We traverse through the Vector using nodeIndex, which is iterated
		//whenever we add a child to a parent node.
		int nodeIndex = 0;
		//Keeps track of how many children we attach to a node at a higher
		//level
		int addCount;
		//A counter to traverse through a level from left to right.
		int traversingLevel;
		OTree_Node treeNode;
		
		//Generate the initial uniform random degree
		if(rndSrc.nextBoolean())
		{
			randomDegree = 2;
		}
		else
		{
			randomDegree = 3;
		}
		
		//If the number of nodes added to the previous level is 1,
		//then that means we've hit the limit and the loop needs
		//to break. That 1 node added will be the root.
		while(nodesAdded != 1)
		{
			//Reset the counter which keeps track of how many nodes were
			//added to the previous level.
			nodesAdded = 0;
			
			//We traverse the previous level by iterating through the nodes 
			//we added. We increment this by the randomDegree value, which
			//dictates how many children we attach to a parent.
			for(traversingLevel = 0; traversingLevel < numOfNodesAtLevel; traversingLevel += randomDegree)
			{
				treeNode = new OTree_Node();
				
				//We take the current number of nodes we traversed through
				//and increment it by the randomDegree like in the for loop.
				//If this exceeds the number of node at that level, then 
				//we set the degree to however many nodes at left. 
				//traversingLevel is 0-aligned, so we add 1 to compensate			
				if(((traversingLevel + randomDegree) + 1) > numOfNodesAtLevel)
				{
					//We take the difference between the excess number of 
					//nodes and how many nodes are actually left at that
					//level. This becomes the new randomDegree.
					randomDegree = ((traversingLevel + randomDegree) + 1) - numOfNodesAtLevel;
				}
				
				//We set the number of nodes we will be attaching to a 
				//parent by the degree.
				addCount = randomDegree;
				
				while(addCount > 0)
				{
					//Using the nodeIndex to grab the next node and set its
					//parent to the current node. We then add those nodes
					//to the children of the current node.
					treeNodes.get(nodeIndex).setParent(treeNode);
					treeNode.addChild(treeNodes.get(nodeIndex));
					
					//Iterate nodeIndex for every node we 'eat'
					nodeIndex++;		 
					addCount--;
				}
				
				//Add the new node to the Vector
				treeNodes.add(treeNode);
				//Keep track of each node we add this way
				nodesAdded++;
				
				//Generate the next randomDegree
				if(rndSrc.nextBoolean())
				{
					randomDegree = 2;
				}
				else
				{
					randomDegree = 3;
				}
				
			}
			
			//The number of of nodes at the level we just created to the 
			//number of nodes we added to the Vector
			numOfNodesAtLevel = nodesAdded;
		}
		
		//Set the root after the loop breaks to finish off
		//the tree.
		root = (OTree_Node)treeNodes.get(nodeIndex);
	}
	
	/** In order to insert a new node, you must provide data (in the form of
	 *  a byte array) and a position. You want to insert the value into 
	 *  position i.
	 *  @param value value to be inserted
	 *  @param i index of chunk/leaf to insert into
         * @param Signature Object
	 *  @return void
	 */
	public synchronized void insert(byte[] value, int i)
	{
		/*
		 * Create a new leaf node based on the new data
		 */
		int w, randomDegree, level = 0, childRemoveCount;
		int maxLeaves = treeNodes.size();
		OTree_Node tempNode, sibling, ithParent, parent, ithTemp;
		OTree_Leaf newLeaf = new OTree_Leaf();
		newLeaf.setSig(value);
		/*
		 * Fetch the current ith (zero-aligned) leaf node
		 */
		OTree_Leaf iThLeaf = (OTree_Leaf)treeNodes.get((i - 1));
		/*
		 * Get the parent of the current ith node
		 */
		parent = ithParent = (OTree_Node)iThLeaf.getParent();
		/*
		 * Insert the new ith node into the ith (zero-aligned) position
		 */
		treeNodes.add((i-1), newLeaf);

                /*
                 * Figure out what our level is by traversing the tree up to the
                 * root. We need to know the level so we can fetch the level
                 * neighbor. Ugh, this is so inefficient.
                 */
                while(parent != null)
                {
                    parent = (OTree_Node)parent.getParent();
                    level++;
                }
                
                if(rndSrc.nextBoolean())
                {
                        randomDegree = 2;
                }
                else
                {
                        randomDegree = 3;
                }
                
                /*
                 * We can skip straight to step 3 (which is after the if 
                 * statement) of the paper's description of the insert function 
                 * if the leaf's parent is both the LAST node of its level AND 
                 * its either got a degree of 3 OR the random degree we chose 
                 * above is equal to 3.
                 */
                while(ithParent != null)
                {
                    if(!((this.getNeighbor(ithParent, level) == null) && (ithParent.getDegree() == 3 || randomDegree == 3)))
                    {
                        /*
                         * We initialize w to 1
                         */
                        w = 1; 
                        
                        while(w != 0)
                        {
                            ithTemp = ithParent;
                            
                            if(rndSrc.nextBoolean())
                            {
                                    randomDegree = 2;
                            }
                            else
                            {
                                    randomDegree = 3;
                            }
                            
                            if((randomDegree == w) || (this.getNeighbor(ithTemp, level) == null))
                            {
                                if(ithTemp.getParent().getDegree() == 2)
                                {
                                    OTree_Node newChild = new OTree_Node();
                                    ithTemp.getParent().addChild(newChild);
                                    
                                    childRemoveCount = 0;
                                    
                                    while(childRemoveCount < w)
                                    {
                                        newChild.addChild(ithTemp.getChild(ithTemp.getDegree() - 1));
                                        ithTemp.removeChild(ithTemp.getDegree() - 1);
                                        childRemoveCount++;
                                    }
                                }
                                else
                                {
                                    parent = (OTree_Node)ithTemp.getParent();
                                    
                                    while(parent != null)
                                    {
                                        if(parent.getDegree() == 2)
                                        {
                                            OTree_Node newChild = new OTree_Node();
                                            parent.addChild(newChild);

                                            childRemoveCount = 0;

                                            while(childRemoveCount < w)
                                            {
                                                newChild.addChild(ithTemp.getChild(ithTemp.getDegree() - 1));
                                                ithTemp.removeChild(ithTemp.getDegree() - 1);
                                                childRemoveCount++;
                                            }
                                            
                                            break;
                                        }
                                        else
                                        {
                                            parent = (OTree_Node)parent.getParent();
                                        }
                                    }
                                    
                                    
                                }
                                
                                w = 0;
                            }
                            else
                            {
                                ithTemp = this.getNeighbor(ithParent, level);
                                int t = ithTemp.getDegree();
                                
                                childRemoveCount = 0;

                                while(childRemoveCount < w)
                                {
                                    ithTemp.addChild(ithParent.getChild(ithParent.getDegree() - 1));
                                    ithParent.removeChild(ithParent.getDegree() - 1);
                                    childRemoveCount++;
                                }
                                
                                w = java.lang.Math.max(0, t + w - randomDegree);                                                                                                
                            }
                        }
                        
                    }
                    
                    //MUST RECOMPUTE SIZE FIELDS
                    
                    ithParent = (OTree_Node)ithParent.getParent();
                }
	}
	//
        
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
        
	public synchronized boolean delete()
	{
		//OTree_Leaf = deletedNode;
		
		return false;
	} //*/

	/**
	 *  Fetches the ith leaf of the tree
	 *  @param i index of leaf to return (indices start at 1)
	 */
	private synchronized OTree_Leaf getLeaf(int i)
	{
		OTree_Leaf leaf;
		
		leaf = (OTree_Leaf)treeNodes.get((i - 1));
		return leaf;
	}
	
	/** generate the signature output of algorithm
	 * outputs each node in signature as {sig_size}{sig}{degree} in breadth-first order
	 *  @return byte[] of current complete signature, null if failure
	 */
	public byte[] generateSig(){
		byte[] rtn = new byte[128];	// signature output byte array
		int i=0;	// index into output array
		LinkedList<OTree_Elem> nodeQueue = new LinkedList<OTree_Elem>();	// list of unprocessed nodes
		ByteBuffer buf = ByteBuffer.allocate(4);	// bytebuffer for doing int to byte[] conversions
		byte[] tmp;	// temporary array for holding byte rep of each node
		
		// add root to queue to begin
		nodeQueue.add(this.root);
		// output tree with breadth-first traversal
		while(!nodeQueue.isEmpty()){
			// get next node
			OTree_Elem thisNode = nodeQueue.remove();
			// add children of this node to queue
			nodeQueue.addAll(Arrays.asList(thisNode.getChildren()));
			// get byte signature of current node
			tmp = thisNode.getSig();
			// while rtn not big enough
			while((rtn.length-i)<(tmp.length+8)){
				// resize rtn to double
				rtn = Arrays.copyOf(rtn, rtn.length*2);
			}
			// write node into rtn 
			// prepend with signature size
			buf.putInt(0, tmp.length);
			// copy signature into rtn
			System.arraycopy(buf.getInt(0), 0, rtn, i, 4);
			i+=4;
			// copy signature into rtn
			System.arraycopy(tmp, 0, rtn, i, tmp.length);
			i += tmp.length;
			// append with degree
			buf.putInt(0, thisNode.getDegree());
			System.arraycopy(buf.getInt(0), 0, rtn, i, 4);
			i+=4;
		}
		// return truncated array of just signatures
		return Arrays.copyOf(rtn, i);
	}
	/** verify if a signature is correct given the file and public key
	 * accepts signatures in the format produced by generateSig()
	 * verifies each node of tree against children until reaches leaves
	 * for each leaf verifies against matching chunk of file 
	 * @param file
	 * @param sig
	 * @return true if valid, false if invalid
	 */
	public static boolean verifySig(byte[] file, byte[] sig){
		
		
		return false;
	}
	
	
}