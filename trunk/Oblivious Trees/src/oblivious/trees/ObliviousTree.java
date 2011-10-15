package oblivious.trees;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
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
	/** Get information about psuedorandom number generator used.
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
	/** Get information about message digest used.
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
                 * We must traverse up the tree and perform the randomization
                 * procedure for every node that is along the path between the
                 * root and the leaf node the user wanted to add.
                 */
                while(ithParent != null)
                {
                    /*
                     * We can skip straight to step 3 (which is after the if 
                     * statement) of the paper's description of the insert function 
                     * if the leaf's parent is both the LAST node of its level AND 
                     * its either got a degree of 3 OR the random degree we chose 
                     * above is equal to 3.
                     */
                    if(!((this.getNeighbor(ithParent, level) == null) && (ithParent.getDegree() == 3 || randomDegree == 3)))
                    {
                        /*
                         * If the above condition is not met, we initialize a 
                         * variable w to 1.
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
                    //Also recomputer the size fields of the parent of the leaf
                    //node to its level neighbor.
                    
                    ithParent = (OTree_Node)ithParent.getParent();
                    level--;
                }
	}
	//
        
        public synchronized void delete(int i)
	{
                /*
		 * Create a new leaf node based on the new data
		 */
		int w, randomDegree, level = 0, childRemoveCount, t;
		int maxLeaves = treeNodes.size();
		OTree_Node tempNode, sibling, ithParent, parent, ithTemp;
                OTree_Elem[] children;
                
                /*
		 * Fetch the current ith (zero-aligned) leaf node
		 */
		OTree_Leaf iThLeaf = (OTree_Leaf)treeNodes.get((i - 1));
		/*
		 * Get the children of the parent of the ith leaf, so we can 
                 * delete the given child before removing it from the Vector
		 */
		children = iThLeaf.getParent().getChildren();
                ithParent = parent =(OTree_Node)iThLeaf.getParent();
                
                while(parent != null)
                {
                    level++;
                    parent = (OTree_Node)parent.getParent();
                }
                
                for(int cnt = 0; cnt < children.length; cnt++)
                {
                    if(iThLeaf.getParent().getChild(cnt) == iThLeaf)
                    {
                        iThLeaf.getParent().removeChild(cnt);
                        break;
                    }
                }
	
                treeNodes.removeElementAt(i);
                
                while(ithParent !=  null)
                {                
                    if(this.getNeighbor(ithParent, level) == null)
                    {
                        if(ithParent.getChildren() == null)
                        {
                            ithTemp = ithParent;
                            ithParent = (OTree_Node)ithParent.getParent();
                        }
                        else
                        {
                            /*
                             * if the node is the root or below it, recompute
                             * the size information for all the nodes along the
                             * path. Else, delete the current root and makes
                             * its child along the path new root.
                             */
                            
                            root = ithParent;
                        }
                    }
                    else
                    {
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
                            
                            ithParent = this.getNeighbor(ithTemp, level);
                            
                            t = ithParent.getDegree();
                            
                            if(w >= t)
                            {
                                childRemoveCount = 0;
                                
                                while(childRemoveCount < w)
                                {
                                    ithParent.addChild(ithTemp.getChild(ithTemp.getDegree() - 1));
                                    ithTemp.removeChild(ithTemp.getDegree() - 1);
                                    childRemoveCount++;
                                }
                                
                            }
                            else
                            {                                                            
                                if(rndSrc.nextBoolean())
                                {
                                    randomDegree = 2;
                                }
                                else
                                {
                                    randomDegree = 3;
                                }
                                
                                childRemoveCount = 0;

                                while(childRemoveCount < w)
                                {
                                    ithParent.addChild(ithTemp.getChild(ithTemp.getDegree() - 1));
                                    ithTemp.removeChild(ithTemp.getDegree() - 1);
                                    childRemoveCount++;
                                }
                                
                                w = java.lang.Math.max(0, t + w - randomDegree);
                            }
                            
                            //--??
                        }
                    }
                    
                    ithParent = (OTree_Node)ithParent.getParent();
                }
                
	} //*/
        
        /*
         * Updates the size information of all nodes along the path from the root
         * to the leaf node i
         */
        public synchronized void updateSizes(int i)
        {
            
        }
        
        public synchronized void updateHashes(int i)
        {
            
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
	
	/* failed attempt at breadth-first implementation
	 * generate the signature output of algorithm
	 * outputs each node in signature as {sig_size}{sig}{degree} in breadth-first order
	 *  @return byte[] of current complete signature, null if failure
	 */ 
	/* 
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
			System.arraycopy(buf.array(), 0, rtn, i, 4);
			buf.clear();
			i+=4;
			// copy signature into rtn
			System.arraycopy(tmp, 0, rtn, i, tmp.length);
			i += tmp.length;
			// append with degree
			buf.putInt(0, thisNode.getDegree());
			System.arraycopy(buf.array(), 0, rtn, i, 4);
			//buf.clear();
			i+=4;
		}
		// return truncated array of just signatures
		return Arrays.copyOf(rtn, i);
	} //*/
	/** generate the signature output of algorithm
	 * outputs each node in signature as {sig_size}{sig}{degree} in depth-first preorder
	 *  @return byte[] of current complete signature, null if failure
	 */
	public synchronized byte[] generateSig(){
		// Initialize output holder; index at 0, initial size of 128 bytes
		ObliviousTree.SignatureArray sig = new ObliviousTree.SignatureArray(0, 128);
		ObliviousTree.generateSigRecurse(this.root, sig);
		// return truncated array of just signatures
		return Arrays.copyOf(sig.data, sig.index);
	} //*/
	/** recursive function to traverse tree and compile complete signature
	 *  @param thisNode current node
	 *  @param sig SignatureArray object holding current state
	 */
	private static void generateSigRecurse(OTree_Elem thisNode, ObliviousTree.SignatureArray sig){
		ByteBuffer buf = ByteBuffer.allocate(4);	// bytebuffer for doing int to byte[] conversions
		byte[] tmp;	// temporary array for holding byte rep of each node
		
		// get byte signature of current node
		tmp = thisNode.getSig();
		
		// while sig data not big enough to append this node
		while((sig.data.length-sig.index)<(tmp.length+8)){
			// resize sig data to double
			sig.data = Arrays.copyOf(sig.data, sig.data.length*2);
		}
		
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
		for (OTree_Elem c : Arrays.asList(thisNode.getChildren())){
			generateSigRecurse(c, sig);
		}
	}
	/** helper class for storing state of signature output
	 *  used for outputting signatures and verification
	 */
	private static class SignatureArray {
		protected int index;
		protected byte[] data;
		protected SignatureArray(){
			index = 0;
			data = new byte[1];
		}
		protected SignatureArray(int i, int size){
			index = i;
			data = new byte[size];
		}
		
	}
	/** verify if a signature is correct given the file and public key
	 *  accepts signatures in the format produced by generateSig()
	 *  verifies each node of tree against children until reaches leaves
	 *  for each leaf verifies against matching chunk of file 
	 *  @param file document to be verified
	 *  @param sig signature tree to be used
	 *  @param verifier Signature to verify tree and file with
	 *  @return true if valid, false if invalid
	 */
	public static boolean verifySig(byte[] file, byte[] sig, Signature verifier){
		// construct SignatureArrays from file and signature input
		ObliviousTree.SignatureArray fileArray = new ObliviousTree.SignatureArray(0, 5);
		ObliviousTree.SignatureArray sigArray = new ObliviousTree.SignatureArray();
		fileArray.data = file;
		sigArray.data = sig;
		try{
			// try and verify the file using signature file and verifier
			verifySigRecurse(fileArray, sigArray, verifier);
		} catch (GeneralSecurityException e){
			return false;
		}
		return true;
	} //*/
	/** recursive function to reconstruct and verify tree and file using verifying signature
	 *  @return byte[] signature data for parent calculation
	 *  @throws GeneralSecurityException when signature verification fails (I know this is terrible...)
	 */
	private static byte[] verifySigRecurse(ObliviousTree.SignatureArray file, ObliviousTree.SignatureArray sig, Signature verifier) throws GeneralSecurityException{
		int sig_size, degree; // signature size and node degree to be read from input
		ByteBuffer buf = ByteBuffer.allocate(4);	// bytebuffer for doing int to byte[] conversions
		byte[] tmp;		// temporary array for holding byte sig of each node
		byte[] data;	// temporary array for storing data to be verified
		
		// read signature size from file
		buf.put(sig.data, sig.index, 4);
		sig_size = buf.getInt(0);
		buf.clear();
		sig.index+=4;
		// read signature
		tmp = Arrays.copyOfRange(sig.data, sig.index, sig.index+sig_size);
		sig.index+=sig_size;
		// read degree
		buf.put(sig.data, sig.index, 4);
		degree = buf.getInt(0);
		//buf.clear();
		sig.index+=4;
		
		// if has children verify against children
		if (degree>0){
			data = null;
			// gather children signatures
			for (int j=0; j<degree; j++){
				// concatenate child segments together
				data = concatArrays(data, verifySigRecurse(file, sig, verifier));
			}
		}
		// verify against file
		else {
			// use the smaller of default chunk size and remaining file portion
			int chunk_size = (file.data.length-file.index > ObliviousTree.CHUNK_SIZE) ? ObliviousTree.CHUNK_SIZE : file.data.length-file.index;
			data = Arrays.copyOfRange(file.data, file.index, file.index+chunk_size);
			file.index+=chunk_size;
		}
		
		// validate signature
		verifier.update(data);
		if (!verifier.verify(tmp)){
			throw new GeneralSecurityException();
		}
		// 
		return tmp;
	}
	/** helper method to concatenate byte arrays together
	 * ordered as AB, will accept either as null
	 * @param A first array
	 * @param B second array
	 * @return byte[] concatenated arrays, null if both arrays null
	 */
	private static byte[] concatArrays(byte[] A, byte[] B){
		if(A==null && B==null){
			return null;
		} else if (A==null){
			return B;
		} else if(B==null){
			return A;
		} else {
			byte[] tmp = Arrays.copyOf(A, A.length+B.length);
			System.arraycopy(B, 0, tmp, A.length, B.length);
			return tmp;
		}
			
	}
	
}