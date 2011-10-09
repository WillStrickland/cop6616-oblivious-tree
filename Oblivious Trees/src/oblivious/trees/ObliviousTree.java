package oblivious.trees;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * William Strickland and Chris Fontaine
 * COP 6616 - Multi-core programming
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
	
	// Setup of cryptographic objects
	// PRNG for randomness and Message Digest for signatures
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
	public static String PRNG_Info(){
		if (rndSrc != null){
			return rndSrc.getAlgorithm()+ " - " + rndSrc.getProvider().toString();
		} else {
			return "PRNG not initialized!";
		}
	}
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
	public static String Digest_Info(){
		if (digest != null){
			return digest.getAlgorithm()+ " - " + digest.getProvider().toString();
		} else {
			return "Digest not initialized!";
		}
	}
	
	/**
	 * @param byte[] file
	 * @return void
	 * Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 * and, after taking a number between two and three, generate a number of non-leaf, which
	 * is randomly chosen between 2 and 3.
	 */
	
	//Generate set of leaves with hashes for chunks of file
	private void generateLeaves(FileInputStream file)
	{	
		int this_size;
		byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
		try {
			// loop until reaches end of file
			while(true){
				this_size = file.read(chunk);
				treeNodes.add(new OTree_Leaf(ObliviousTree.digest.digest(Arrays.copyOf(chunk,this_size))));
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
	
	private void generateTree()
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
       
        /*
         * @param byte[] value
         * @param int i
         * In order to insert a new node, you must provide data (in the form of
         * a byte array) and a position. You want to insert the value into 
         * position i.
         * @return void
         */
        public void insert(byte[] value, int i)
	{
            /*
             * Create a new leaf node based on the new data
             */
            int w, randomDegree;
            int maxLeaves = treeNodes.size();
            OTree_Node tempNode;
            OTree_Leaf newLeaf = new OTree_Leaf(value);
            /*
             * Fetch the current ith (zero-aligned) leaf node
             */
            OTree_Leaf iThLeaf = (OTree_Leaf)treeNodes.get((i - 1));
            /*
             * Get the parent of the current ith node
             */
            OTree_Node parent = (OTree_Node)iThLeaf.getParent();
          
            /*
             * Insert the new ith node into the ith (zero-aligned) position
             */
            treeNodes.add((i-1), newLeaf);
            /*
             * Now we work from left to right, starting from the parent of i. We 
             * know when we have reached the root because the parent of the root 
             * is always null.             
             */
            while(parent.getParent() != null)
            {
                w = 1;
                tempNode = parent;                
                randomDegree = (rndSrc.nextBoolean()) ? 2 : 3;
                
                /*
                 * if(parent.getNextSibling() == null)
                 *  go to parent and give it a new child. Take the next w 
                 *  children of the current node and the new child/sibling their
                 *  parent.
                 * else     
                 */
                parent = (OTree_Node)parent.getParent();
            }                                                
	}
        	/*
	public OTree_Leaf delete()
	{
		OTree_Leaf = deletedNode;
		
		return deletedNode;
	} //*/
        
        /*
         * Fetches the ith leaf of the tree
         */
        private OTree_Leaf getLeaf(int i)
        {
            OTree_Leaf leaf;
            
            leaf = (OTree_Leaf)treeNodes.get((i - 1));
            
            return leaf;
        }
        


}