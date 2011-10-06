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
	private static MessageDigest digest;		// Secure hashing function for leaves
	public final static int CHUNK_SIZE = 1000;	// File chunk size in bytes
	
	/* Instance Properties */
	private OTree_Node root;	// root node of tree
	private Vector<OTree_Elem> treeNodes;	// list of nodes and leaves for rapid access

	
	public ObliviousTree(){
		// Initialize crypto stuff
		initPRNG();
		this.initDigest();
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//treeNodes.add(root);
	}
	
	public ObliviousTree(FileInputStream file)
	{
		// Initialize crypto stuff
		initPRNG();
		this.initDigest();
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//treeNodes.add(root);
		
		//fileChunks = new Vector<OTree_Elem>();
		
		//2). Generate leaf nodes from the byte array
		
		//3). Create Oblivious Tree
		//generateLeaves(file);
		//create();
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
	private boolean initDigest(){	
		if (digest==null){
			try {
				digest = MessageDigest.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e){
				return false;
			}
		}
		return true;
	}
	public String Digest_Info(){
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
	
	/*
	private void create()
	{		
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
	
	//Generate set of leaves with hashes for chunks of file
	private void generateLeaves(FileInputStream file)
	{	
		int this_size;
		byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
		try {
			while(true){
				this_size = file.read(chunk);
				treeNodes.add(new OTree_Leaf(Arrays.copyOf(chunk,this_size)));
				if (this_size < ObliviousTree.CHUNK_SIZE){
					break;
				}
			}
		} catch (Exception e){
			
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
	
	// Create Leaf node from raw data
	public static OTree_Leaf createLeaf(byte[] b){
		return new OTree_Leaf(b);
	}

}