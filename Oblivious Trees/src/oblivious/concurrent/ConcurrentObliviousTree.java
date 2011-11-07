package oblivious.concurrent;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

/** Oblivious Tree - COP 6616
 * @author William Strickland and Chris Fontaine
 * @version Concurrent Implementation
 */
public class ConcurrentObliviousTree {

	// main testing method
	public static void main(String[] args) {
		System.out.println("Hello, World!!!");
		/*System.out.println("chunkSize = "+ObliviousTree.CHUNK_SIZE);
		System.out.println("PRNG = "+ObliviousTree.PRNG_Info());
		System.out.println("DIGEST = "+ObliviousTree.Digest_Info());
		//System.out.println("initPRNG = "+ObliviousTree.initPRNG());
		System.out.println("mk oblivioustree");
		ObliviousTree O = new ObliviousTree();
		System.out.println("PRNG = "+ObliviousTree.PRNG_Info());
		System.out.println("DIGEST = "+ObliviousTree.Digest_Info());
		//*/
		ConcurrentObliviousTree.testVerify();
		
	} //*/
	
	// test class for making sure verification and such works
	private static void testVerify(){
		// get signature objects for signing and verifying
		// [0] is for signing
		// [1] is for verifying
		Signature[] signatures = initSignature();	
		byte[] testfile = new byte[550];
		byte[] signOut;
		Random this_rnd = ConcurrentObliviousTree.rndSrc.get();
		ConcurrentObliviousTree test = new ConcurrentObliviousTree();
		test.treeNodes = new Vector<OTree_Elem>();
		this_rnd.nextBytes(testfile);
		Vector<OTree_Elem> tmpNodes = new Vector<OTree_Elem>();
		// parse out file chunk signatures to each node
		int i=0;
		while(i<testfile.length){
			OTree_Leaf tmp = new OTree_Leaf();
			int this_chunk = (testfile.length-i>ConcurrentObliviousTree.CHUNK_SIZE) ? ConcurrentObliviousTree.CHUNK_SIZE : testfile.length-i ;
			try {
				signatures[0].update(testfile, i, this_chunk);
				tmp.setSig(signatures[0].sign());
				i+=this_chunk;
			} catch (SignatureException e) { ; }
			test.treeNodes.add(tmp);
		}
		// manually build tree
		tmpNodes.add(new OTree_Node());
		tmpNodes.get(0).addChild(test.treeNodes.get(0));
		test.treeNodes.get(0).setParent(tmpNodes.get(0));
		tmpNodes.get(0).addChild(test.treeNodes.get(1));
		test.treeNodes.get(1).setParent(tmpNodes.get(0));
		tmpNodes.add(new OTree_Node());
		tmpNodes.get(1).addChild(test.treeNodes.get(2));
		test.treeNodes.get(2).setParent(tmpNodes.get(1));
		tmpNodes.get(1).addChild(test.treeNodes.get(3));
		test.treeNodes.get(3).setParent(tmpNodes.get(1));
		tmpNodes.get(1).addChild(test.treeNodes.get(4));
		test.treeNodes.get(4).setParent(tmpNodes.get(1));
		tmpNodes.add(new OTree_Node());
		tmpNodes.get(2).addChild(test.treeNodes.get(5));
		test.treeNodes.get(5).setParent(tmpNodes.get(2));
		tmpNodes.add(test.root);
		tmpNodes.get(3).addChild(tmpNodes.get(0));
		tmpNodes.get(0).setParent(tmpNodes.get(3));
		tmpNodes.get(3).addChild(tmpNodes.get(1));
		tmpNodes.get(1).setParent(tmpNodes.get(3));
		tmpNodes.get(3).addChild(tmpNodes.get(2));
		tmpNodes.get(2).setParent(tmpNodes.get(3));
		// set signatures for internal nodes
		ConcurrentObliviousTree.updateSig(tmpNodes, signatures[0]);
		// internally verify
		System.out.println("internal verify = "+ConcurrentObliviousTree.verifySig(tmpNodes, signatures[1]));
		// output signature
		signOut = test.signatureGenerate();
		// verify signature
		System.out.println("output verify = "+ConcurrentObliviousTree.signatureVerify(testfile, signOut, signatures[1]));
		// sabotage signature
		//testfile[54]= (byte) (testfile[54]+1);
		byte[] sabtmp = tmpNodes.get(0).getSig();
		sabtmp[5] = (byte) (sabtmp[5]+1);
		tmpNodes.get(0).setSig(sabtmp);
		// output signature
		signOut = test.signatureGenerate();
		// verify signature
		System.out.println("sabotaged verify = "+ConcurrentObliviousTree.signatureVerify(testfile, signOut, signatures[1]));
	} //*/
	
	/* Class Properties */
	// File chunk size in bytes
	public static final int CHUNK_SIZE = 100;	
	// Random source for creating obliviousness
	private static final ThreadLocal<Random> rndSrc = 
			new ThreadLocal <Random> () {
				@Override protected Random initialValue() {
					return ConcurrentObliviousTree.initPRNG();
				}
			};
	
	/* Instance Properties */
	private OTree_Node root;	// root node of tree
	private Vector<OTree_Elem> treeNodes;	// list of nodes and leaves for rapid access
	
	/** Constructor generates empty initial tree.
	 */
	public ConcurrentObliviousTree(){
		root = new OTree_Node();
	}
	/** Constructor generates initial leaf node using using a given input file.
	 */
	public ConcurrentObliviousTree(FileInputStream file, Signature signer){
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//2). Create Oblivious Tree
		create(generateLeaves(file, signer),signer);
	}
	/** Constructor generates initial leaf node using using a given input byte array.
	 */
	public ConcurrentObliviousTree(byte[] file, Signature signer){
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//2). Create Oblivious Tree
		create(generateLeaves(file, signer),signer);
	}
	
	/** Initialize psuedorandom number generator for class if not already initialized.
	 *  @return new PRNG, null if failure
	 */
	private static Random initPRNG(){	
		try {
			Random tmpRnd = SecureRandom.getInstance("SHA1PRNG");
			byte[] b = new byte[1];
			tmpRnd.nextBytes(b);
			return tmpRnd;
		} catch (NoSuchAlgorithmException e){
			return null;
		}
	}
	/** Get information about psuedorandom number generator used.
	 *  @return String describing psuedorandom number generator algorithm
	 */
	public static String PRNG_Info(){
		Random this_rnd = rndSrc.get();
		if (this_rnd != null){
			String txt;
			try {
				txt = " - " + ((SecureRandom) this_rnd).getAlgorithm() + " - " + ((SecureRandom) this_rnd).getProvider().toString();
			} catch (ClassCastException e){
				txt = "";
			}
			return this_rnd.getClass().getName() + txt;
		} else {
			return "PRNG not initialized!";
		}
	}
	/** Generates a public-private key pair at random and
	 *  returns a signature for signing and another for verifying
	 *  @return Signature[] index 0 = signing, index 1 = verifying
	 */
	private static Signature[] initSignature(){
		Signature[] sig = new Signature[2];
		try {
			// create random source for key generation
			SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
			// create public-private key pair
			KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
			gen.initialize(1024, rnd);
			KeyPair keys = gen.generateKeyPair();
			// create signature object
			sig[0] = Signature.getInstance("SHA1withDSA");
			sig[0].initSign(keys.getPrivate());
			sig[1] = Signature.getInstance("SHA1withDSA");
			sig[1].initVerify(keys.getPublic());
		} catch (Exception e){
			return null;
		}
		return sig;
	}
	
	/** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 *  and, after taking a number between two and three, generate a number of non-leaf, which
	 *  @param file file to be put signed in leaves
	 *  @param signer signature to be used for signing leaves
	 *  @return list of leaves created
	 */
	private synchronized Vector<OTree_Elem> generateLeaves(FileInputStream file, Signature signer){
		int this_size;
		Vector<OTree_Elem> tmp = new Vector<OTree_Elem>();
		byte[] chunk = new byte[ConcurrentObliviousTree.CHUNK_SIZE];
		try {
			// loop until reaches end of file
			while(true){
				this_size = file.read(chunk);
				OTree_Leaf newLeaf = new OTree_Leaf();
				signer.update(chunk, 0, this_size);
				newLeaf.setSig(signer.sign());
				tmp.add(newLeaf);
				
				// if less than whole chunk, reached end of file
				if (this_size < ConcurrentObliviousTree.CHUNK_SIZE){
					// break out of loop
					break;
				}
			}
			return tmp;
		} catch (Exception e){
			return null;
		}
	} //*/
	/** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
	 *  and, after taking a number between two and three, generate a number of non-leaf, which
	 *  @param file byte array to be put signed in leaves
	 *  @param signer signature to be used for signing leaves
	 *  @return list of leaves created
	 */
	private synchronized Vector<OTree_Elem> generateLeaves(byte[] file, Signature signer){
		int this_size=0;
		Vector<OTree_Elem> tmp = new Vector<OTree_Elem>();
		// clear current leaves
		try {
			// loop until reaches end of file
			for(int i=0; i<file.length; i+=this_size){
				OTree_Leaf newLeaf = new OTree_Leaf();
				this_size = (file.length-i>ConcurrentObliviousTree.CHUNK_SIZE) ? ConcurrentObliviousTree.CHUNK_SIZE : file.length-i;
				signer.update(file, 0, this_size);
				newLeaf.setSig(signer.sign());
				tmp.add(newLeaf);
			}
			return tmp;
		} catch (Exception e){
			return null;
		}
	} //*/
	
        /** Oblivious are generated from the ground up. Meaning we take a number of leaf nodes
         *  and, after taking a number between two and three, generate a number of non-leaf, which
         */
        private synchronized final void create(Vector<OTree_Elem> leaves, Signature signer)
        {
                Random this_rnd = rndSrc.get();
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
                Vector<OTree_Elem> previousLevel = leaves;
                /*
                 * Holds the nodes being added to the current level.
                 */
                Vector<OTree_Elem> currentLevel;
                
                while(previousLevel.size() > 1)
                {
                    currentLevel = new Vector<OTree_Elem>();
                    numOfNodesAtLevel = previousLevel.size();
                    
                    for(addCount = 0; addCount < numOfNodesAtLevel; addCount += randomDegree)
                    {
                        randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
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
                            currentLevel.lastElement().setNeighbor(tempNode);
                        }
                        catch(NoSuchElementException e)
                        {
                        }
                        
                        tempNode.calcLeafCnt();
                        currentLevel.add(tempNode);
                    }
                    
                    previousLevel = currentLevel;
                }
                
                try
                {
                    root = (OTree_Node)previousLevel.firstElement();
                }
                catch(NoSuchElementException e)
                {                    
                }
        }
        
        /**
         * 
         * @param int i
         * @return OTree_Elem node
         */
        public OTree_Elem getNode(int i)
        {
            OTree_Elem tempNode;
            int rangeStart = 1, rangeEnd, rngeCnt, randomDegree;
            OTree_Elem[] children;
            
            tempNode = root;
            children = tempNode.getChildren();
            
            while(children != null)
            {                
                for(rngeCnt = 0; rngeCnt < children.length; rngeCnt++)
                {
                    rangeEnd = rangeStart + ((children[rngeCnt].getLeafCnt()) - 1);
                    
                    if(i >= rangeStart && i <= rangeEnd)
                    {
                        tempNode = children[rngeCnt];
                        break;
                    }
                    else
                    {
                        rangeStart += ((children[rngeCnt + 1].getLeafCnt()) - 1) ;
                    }                                        
                }
                
                children = tempNode.getChildren();
            }
            
            return tempNode;
        }
        
        /**
         * 
         * @param byte[] value
         * @param int i
         * @param Signature signer
         * @return void
         */
        public void concurrentInsert(byte[] value, int i, Signature signer)
        {
            Random this_rnd = rndSrc.get();
            OTree_Elem currentNode = this.getNode(i);
            OTree_Node parent = (OTree_Node)currentNode.getParent();
            OTree_Leaf newLeaf = new OTree_Leaf();
            OTree_Node newNode, newRoot;
            int randomDegree;
            
            newLeaf.setSig(value);
            parent.addChild(newLeaf);
            newLeaf.setParent(parent);
            
            randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
            
            /*
             * The algorithm keeps going up level by level until we pass the
             * root, at which point we stop
             */      
            
            while(parent != null)
            {
                currentNode = parent;
                
                if(parent.getNeighbor() == null)
                {
                    if(parent.getDegree() == 2 || (parent.getDegree() == 3 && randomDegree == 3))
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
                        
                        /*
                         * According to the Structural Agreement lemma, we're 
                         * finished because all nodes are accounted for. We just
                         * need to update the size information along the path
                         * from the leaf to the root.
                         */
                    }
                }
                
                /*
                 * Occurs after all the previous operations
                 */
                parent = (OTree_Node)parent.getParent();
            }
        }
        
        /**
         * 
         * @param byte[] value
         * @param int i
         * @param Signature signer
         * @return void
         * 
         * Inserts a new leaf into the ith position of the leaf level, then
         * re-randomized the tree based on the optimized insert algorithm
         * presented.
         */
        public synchronized void newInsert(byte[] value, int i, Signature signer)
        {
            Random this_rnd = rndSrc.get();
            i = i - 1;
            OTree_Leaf ithLeaf = (OTree_Leaf)treeNodes.get(i);
            OTree_Leaf newLeaf = new OTree_Leaf();
            OTree_Node ithParent = (OTree_Node)ithLeaf.getParent();
            OTree_Node currentNode, newNode, newRoot, neighbor;
            int w, randomDegree, oldDegree;
            
            newLeaf.setSig(value);            
            ithParent.addChild(newLeaf);
            newLeaf.setParent(ithParent);
            treeNodes.add(i, newLeaf);
            
            randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
            
            /*
             * The algorithm keeps going up level by level until we pass the
             * root, at which point we stop
             */
            while(ithParent.getParent() != null)
            {
                ithParent = (OTree_Node)ithParent.getParent();
                currentNode = ithParent;
                
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
                        randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
                        
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
        }
        
        public synchronized void newDelete(int i)
        {
            Random this_rnd = rndSrc.get();
            i = i - 1;
            OTree_Elem ithChild = treeNodes.get(i);
            OTree_Elem[] ithParentChildren;
            OTree_Node ithParent = (OTree_Node)ithChild.getParent();
            OTree_Node currentNode, newNode, newRoot, neighbor;
            int w, randomDegree, oldDegree;                        
            
            while(ithParent.getParent() != null)
            {
                currentNode = ithParent;
                ithParentChildren = ithParent.getChildren();
                
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
                        randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
                        neighbor = (OTree_Node)currentNode.getNeighbor();
                        oldDegree = neighbor.getDegree();
                        
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
            
        }
	
	/** In order to insert a new node, you must provide data (in the form of
	 *  a byte array) and a position. You want to insert the value into 
	 *  position i.
	 *  @param value value to be inserted
	 *  @param i index of chunk/leaf to insert into
         * @param Signature Object
	 *  @return void
	 */
	public synchronized void insert(byte[] value, int i, Signature signer)
	{
                Random this_rnd = rndSrc.get();
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
                
                if(this_rnd.nextBoolean())
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
                            /*
                             * If the node we are on the last node of its level,
                             * then insert a new node and set its degree to w.
                             * Make the last w children of the current node the 
                             * next w children of the newly inserted node.
                             * 
                             * If we are not on the last node of the level,
                             * (which means we are still propogating
                             * through the level), then find the level 
                             * neighbor of the current, and set the last w 
                             * children of the current node to the first w
                             * children of the neighbor.
                             */
                            ithTemp = ithParent;
                            
                            if(this_rnd.nextBoolean())
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
        
        public synchronized void delete(int i, Signature signer)
         {
            Random this_rnd = rndSrc.get();
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
                            
                            if(this_rnd.nextBoolean())
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
                                if(this_rnd.nextBoolean())
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
	private static boolean updateSig(Collection<OTree_Elem> l, Signature signer){
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
	/** Verify the signature for all the OTree_Elem in list.
	  * Method designed to operate only on internal nodes.
	  * no limits imposed on order of nodes
	  * @param l collection that holds nodes to be updated (children first)
	  * @param verifier signature for verification must be initialized for verification
	  * @return true if successful, false if failure
	  */
	private static boolean verifySig(Collection<OTree_Elem> l, Signature verifier){
		// for node each in collection
		for (OTree_Elem n : l){
			// run update on this node
			// return false if verify failed
			if (!verifySig(n,verifier)){
				return false;
			}
		}
		// return true if all succeed
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
	public synchronized byte[] signatureGenerate(){
		// Initialize output holder; index at 0, initial size of 128 bytes
		ConcurrentObliviousTree.SignatureArray sig = new ConcurrentObliviousTree.SignatureArray(0, 128);
		ConcurrentObliviousTree.signatureGenerateRecurse(this.root, sig);
		// return truncated array of just signatures
		return Arrays.copyOf(sig.data, sig.index);
	} //*/
	/** recursive function to traverse tree and compile complete signature
	 *  @param thisNode current node
	 *  @param sig SignatureArray object holding current state
	 */
	private static void signatureGenerateRecurse(OTree_Elem thisNode, ConcurrentObliviousTree.SignatureArray sig){
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
		if(thisNode.getDegree()>0){
			for (OTree_Elem c : Arrays.asList(thisNode.getChildren())){
				signatureGenerateRecurse(c, sig);
			}	
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
	public static boolean signatureVerify(byte[] file, byte[] sig, Signature verifier){
		// construct SignatureArrays from file and signature input
		ConcurrentObliviousTree.SignatureArray fileArray = new ConcurrentObliviousTree.SignatureArray(0, 5);
		ConcurrentObliviousTree.SignatureArray sigArray = new ConcurrentObliviousTree.SignatureArray();
		fileArray.data = file;
		sigArray.data = sig;
		try{
			// try and verify the file using signature file and verifier
			signatureVerifyRecurse(fileArray, sigArray, verifier);
		} catch (GeneralSecurityException e){
			return false;
		}
		return true;
	} //*/
	/** recursive function to reconstruct and verify tree and file using verifying signature
	 *  @return byte[] signature data for parent calculation
	 *  @throws GeneralSecurityException when signature verification fails (I know this is terrible...)
	 */
	private static byte[] signatureVerifyRecurse(ConcurrentObliviousTree.SignatureArray file, ConcurrentObliviousTree.SignatureArray sig, Signature verifier) throws GeneralSecurityException{
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
				data = concatArrays(data, signatureVerifyRecurse(file, sig, verifier));
			}
		}
		// verify against file
		else {
			// use the smaller of default chunk size and remaining file portion
			int chunk_size = (file.data.length-file.index > ConcurrentObliviousTree.CHUNK_SIZE) ? ConcurrentObliviousTree.CHUNK_SIZE : file.data.length-file.index;
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