package oblivious.trees;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Vector;

/**
 *
 * William Strickland and Chris Fontaine
 * COP 6616 
 */

public class ObliviousTree {

	/*
	public static void main(String[] args) {
		System.out.println("Hello, World!!!");
		System.out.println("chunkSize = "+ObliviousTree.getChunkSize());
		System.out.println("PRNG = "+ObliviousTree.PRNG_Info());
		//System.out.println("initPRNG = "+ObliviousTree.initPRNG());
		System.out.println("mk oblivioustree");
		ObliviousTree O = new ObliviousTree();
		System.out.println("PRNG = "+ObliviousTree.PRNG_Info());
		
		
	} //*/

	/**
	 * Constructor generates initial leaf node using
	 * using a given input file.
	 *
	 */
	
	/* Class Properties */
	private static SecureRandom rndSrc;			// Random source for creating obliviousness
	private final static int CHUNK_SIZE = 100;	// File chunk size
	
	/* Instance Properties */
	private OTree_Node root;	// root node of tree
	private Vector<OTree_Elem> treeNodes;	// list of nodes and leaves for rapid access

	
	public ObliviousTree(){
		initPRNG();
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		treeNodes.add(root);
	}
	
	public ObliviousTree(byte[] file)
	{
		initPRNG();
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		treeNodes.add(root);
		//fileChunks = new Vector<OTree_Elem>();
		
		//2). Generate leaf nodes from the byte array
		//3). Create Oblivious Tree
		//generateLeaves(file);
		//create();
	}
	
	// Initialize cryptographically secure random source
	// skip if already initialized, return false if fail
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
	public static int getChunkSize(){
		return CHUNK_SIZE;
	}
	
	/**
	 * @param byte[] file
	 * @return void
	 * Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 * and, after taking a number between two and three, generate a number of non-leaf, which
	 * is randomly chosen between 2 and 3.
	 */
	/*
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
	} //*/
	
	/*
	private void generateLeaves(byte[] file)
	{
		long byteLen = file.length;
		long bytesRead = 0
		
		while(bytesRead < byteLen)
		{
			treeNodes.addElement(new OTree_Leaf(copyOfRange(file, bytesRead, bytesRead + 10));
			bytesRead += 10;
		}
	} //*/
	
	/*
	public void add(OTree_Leaf newLeaf)
	{
	} //*/
	
	/*
	public OTree_Leaf delete()
	{
		OTree_Leaf = deletedNode;
		
		return deletedNode;
	} //*/
	

}