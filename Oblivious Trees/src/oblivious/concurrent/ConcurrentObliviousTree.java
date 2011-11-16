package oblivious.concurrent;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.security.SignatureException;
import java.util.NoSuchElementException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import oblivious.ObliviousTree;

/** Oblivious Tree - COP 6616
 * @author William Strickland and Chris Fontaine
 * @version Concurrent Implementation
 */
public class ConcurrentObliviousTree extends oblivious.ObliviousTree{

	/* Class Properties */
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
	private AtomicReference<TaskDesc> curTask;	// current task to be completed
	private ConcurrentLinkedQueue<TaskDesc> taskQueue;	// queue of pending tasks
	
	/** Constructor generates empty initial tree.
	 */
	public ConcurrentObliviousTree(){
		root = new OTree_Node();
		// initialize task queue system
		curTask = new AtomicReference<TaskDesc>(new TaskDesc());
		taskQueue = new ConcurrentLinkedQueue<TaskDesc>();
	}
	/** Constructor generates initial leaf node using using a given input file.
	 */
	public ConcurrentObliviousTree(FileInputStream file, Signature signer){
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//2). Create Oblivious Tree
		create(generateLeaves(file, signer),signer);
		// initialize task queue system
		curTask = new AtomicReference<TaskDesc>(new TaskDesc());
		taskQueue = new ConcurrentLinkedQueue<TaskDesc>();
	}
	/** Constructor generates initial leaf node using using a given input byte array.
	 */
	public ConcurrentObliviousTree(byte[] file, Signature signer){
		//1). Instantiate root node
		root = new OTree_Node();
		treeNodes = new Vector<OTree_Elem>();
		//2). Create Oblivious Tree
		create(generateLeaves(file, signer),signer);
		// initialize task queue system
		curTask = new AtomicReference<TaskDesc>(new TaskDesc());
		taskQueue = new ConcurrentLinkedQueue<TaskDesc>();
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
		byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
		try {
			// loop until reaches end of file
			while(true){
				this_size = file.read(chunk);
				OTree_Leaf newLeaf = new OTree_Leaf();
				signer.update(chunk, 0, this_size);
				newLeaf.setSig(signer.sign());
				tmp.add(newLeaf);
				
				// if less than whole chunk, reached end of file
				if (this_size < ObliviousTree.CHUNK_SIZE){
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
				this_size = (file.length-i>ObliviousTree.CHUNK_SIZE) ? ObliviousTree.CHUNK_SIZE : file.length-i;
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
                            tempNode.setPrevNeighbor(currentLevel.lastElement());
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
        
        public void concurrentDelete(TaskDesc t)
        {
            
        }
        
        /**
         * 
         * @param byte[] value
         * @param int i
         * @param Signature signer
         * @return void
         */
        public void concurrentInsert(TaskDesc t)
        {
            Random this_rnd = rndSrc.get();
            int randomDegree, parentIndex = 0;
            OTree_Node newSubTree;
            //DescStatus oldStatus = t.status.get();
            //OTree_Node currentNode = (OTree_Node)oldStatus.currentNode;
            //LinkedList<OTree_Elem> unassigned = oldStatus.unassigned;
            OTree_Node neighbor;
            
            while(t.status.get().stage != DescStatus.StatusType.DONE)
            {
                if(t.status.get().stage == DescStatus.StatusType.NEW)
                {
                    OTree_Leaf newLeaf = new OTree_Leaf();
                    OTree_Leaf leaf = (OTree_Leaf)getNode(t.index);
                    OTree_Node parent = (OTree_Node)leaf.getParent();
                    OTree_Elem[] children = parent.getChildren();
                    OTree_Elem[] parentChildren = parent.getParent().getChildren();
                    
                    newSubTree = new OTree_Node();
                    
                    for(int transfer1 = 0; transfer1 < children.length; transfer1++)
                    {
                        newSubTree.addChild(children[transfer1]);
                    }
                    for(int transfer2 = 0; transfer2 < parentChildren.length; transfer2++)
                    {
                        if(parent == parentChildren[transfer2])
                        {
                            /*
                             * Here we are fetching the index of the parent
                             * node we wish to swap out with our new tree.
                             */
                            parentIndex = transfer2;
                            break;
                        }
                    }
                    
                    newLeaf.setSig(t.data.get().get());
                    newSubTree.addChild(newLeaf);
                    newLeaf.setParent(newSubTree);
                    newSubTree.setNeighbor(parent.getNeighbor());
                    newSubTree.setPrevNeighbor(parent.getPrevNeighbor());
                    
                    DescStatus newStatus = new DescStatus(DescStatus.StatusType.LINK);
                    newStatus.index = parentIndex;
                    newStatus.parent = parent.getParent();
                    newStatus.currentNode = newSubTree;
                    newStatus.previousNode = parent.getPrevNeighbor();
                    
                    if(t.status.compareAndSet(t.status.get(), newStatus))
                    {
                        /*
                         * Updates to the tree can ONLY be made by the thread 
                         * that passes the compareAndSet
                         */
                    }
                }
                else if(t.status.get().stage == DescStatus.StatusType.OPEN)
                {
                    //If the status is open, then that means we need to propose
                    //a new subtree to replace whatever the currentNode is
                    
                    newSubTree = new OTree_Node();
                    OTree_Elem[] children = t.status.get().currentNode.getChildren();
                    
                    if(t.status.get().unassigned.size() > 0)
                    {
                        int limit =  t.status.get().unassigned.size();
                        //insert unassigned nodes into currentNode
                        for(int transfer1 = 0; transfer1 < limit; transfer1++)
                        {                            
                            newSubTree.addChild(t.status.get().unassigned.pop());
                        }                        
                    }
                    
                    for(int transfer2 = 0; transfer2 < children.length; transfer2++)
                    {
                        newSubTree.addChild(children[transfer2]);
                    }
                    
                    randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
                    
                    for(int transfer3 = 0; transfer3 < randomDegree; transfer3++)
                    {
                        t.status.get().unassigned.push(newSubTree.getChild(newSubTree.getDegree() - 1));
                        newSubTree.removeChild(newSubTree.getDegree() - 1);
                    }
                    //Save previous node in descriptor so we can reattach the 
                    //level links when we replace the old node with our new one
                    
                    //The first thing that must be done is to figure where you
                    //are in the tree based on the currentNode. Do I need to 
                    //go up one level or continue on the current level?
                    if(t.status.get().currentNode.getParent() != null)
                    {
                        if(t.status.get().currentNode.getNeighbor() == null)
                        {
                            
                        }
                    }
                    else
                    {
                        DescStatus newStatus = new DescStatus(DescStatus.StatusType.DONE);
                        
                        if(t.status.compareAndSet(t.status.get(), newStatus))
                        {
                            
                        }
                    }
                }
                else if(t.status.get().stage == DescStatus.StatusType.LINK)
                {
                    OTree_Node previousNode = (OTree_Node)t.status.get().previousNode;
                    OTree_Node currentNode = (OTree_Node)t.status.get().currentNode;
                    OTree_Node parent = (OTree_Node)t.status.get().parent;
                    int index = t.status.get().index;
                    
                    previousNode.setNeighbor(currentNode);
                    currentNode.getNeighbor().setPrevNeighbor(currentNode);
                    currentNode.setParent(parent);
                    parent.setChild(index, currentNode);
                    //Attach proposed subtree that created during the NEW or 
                    //OPEN phase
                    DescStatus newStatus = new DescStatus(DescStatus.StatusType.OPEN);

                    
                    if(t.status.compareAndSet(t.status.get(), newStatus))
                    {
                        
                    }
                }
            };
            
//            while(currentNode != null)
//            {
//                randomDegree = (this_rnd.nextBoolean()) ? 2 : 3;
//                
//                if(currentNode.getNeighbor() != null)
//                {
//                    neighbor = (OTree_Node)currentNode.getNeighbor();
//                }
//                else
//                {
//                    //Coming here means you've reached the end of the level,
//                    //and you need to go up to the next one.s
//                    //currentNode = currentNode.getParent();
//                }
//                
//                currentNode = (OTree_Node)currentNode.getParent();
//            }
            
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
	
	/** 
         *  Insert acts a front end for the actual insert function. Insert()
         *  appends a new Insert task into the Task Queue
	 *  @param value value to be inserted
	 *  @param i index of chunk/leaf to insert into
         *  @param Signature Object
	 *  @return void
	 */
	public void insert(byte[] value, int i, Signature signer)
	{
                byte[] signedValue = {127,127,127,0};
                TaskDesc task_descriptor = new TaskDesc(TaskDesc.OpType.INSERT);
                
                task_descriptor.index = i;
                task_descriptor.sig = signer;
                task_descriptor.status = new AtomicReference<DescStatus>(new DescStatus(DescStatus.StatusType.NEW));
                
                try
                {
                    signer.update(value);
                }
                catch(SignatureException e)
                {
                    
                }
                
                try
                {
                    signedValue = signer.sign();
                }
                catch(SignatureException e)
                {
                    
                }
                
                task_descriptor.data = new AtomicReference<ByteArrayWrapper>(new ByteArrayWrapper(signedValue));
                taskQueue.add(task_descriptor);                                     
        }
	//
        /**
         * Delete acts as a front end for the actual delete function. Delete()
         * appends a new Delete task into the Task Queue
         * @param int i
         * @param Signature signer 
         */
        public void delete(int i, Signature signer)
        {
            TaskDesc task_descriptor = new TaskDesc(TaskDesc.OpType.DELETE);
            task_descriptor.status = new AtomicReference<DescStatus>(new DescStatus(DescStatus.StatusType.NEW));
            taskQueue.add(task_descriptor);
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
	
	/** generate the signature output of algorithm
	 * outputs each node in signature as {sig_size}{sig}{degree} in depth-first preorder
	 *  @return byte[] of current complete signature, null if failure
	 */
	public byte[] signatureGenerate(){
		// Initialize output holder; index at 0, initial size of 128 bytes
		ObliviousTree.ByteOutArray sig = new ObliviousTree.ByteOutArray(0, 128);
		ConcurrentObliviousTree.signatureGenerateRecurse(this.root, sig);
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
	
	// Operation Processors
	private void processQueue(TaskDesc t){
		// while my task is still pending
		while(isPendingTask(t)){
			// get current task
			TaskDesc current = this.curTask.get();
			// invoke the correct handler for this task
			switch (current.operation){
				case INSERT:
					processInvoke(t);
					break;
				case DELETE:
					processDelete(t);
					break;
				case GENSIG:
					processGenSig(t);
					break;
				case VOID:
				default:
					// get next operation from queue
					popTask();
					break;
			}
		}
	}
	private void processInvoke(TaskDesc t){
		//this.concurrentInsert(t);          
	}
	private void processDelete(TaskDesc t){
		
	}
	private void processGenSig(TaskDesc t){
		DescStatus now = t.status.get();
		if (now.stage == DescStatus.StatusType.OPEN){
			byte[] tmpdata = signatureGenerate();
			t.data.compareAndSet(null, new ByteArrayWrapper(tmpdata));
			DescStatus newStat = new DescStatus(DescStatus.StatusType.DONE);
			t.status.compareAndSet(now, newStat);
		}
		completeTask(t);
	}
	
	// Task Queue Management
	/** Atomically takes the next task from the queue and set curTask pointer
	 *  (the task all threads are to to assist completing). Queue is the shared 
	 *  list of all pending operations to the oblivious tree. Requires that a 
	 *  VOID task currently be in the curTask slot (indicating the previous 
	 *  task was completed). Can fail if queue is empty, curTask is not void or
	 *  another thread succeeds setting the head before this one.
	 *  @return true if successful, false if failed
	 */
	private boolean popTask(){
		// get current task in queue
		TaskDesc current = this.curTask.get();
		// check if task is done or  void type (meaning last task was completed)
		if (current.status.get().stage == DescStatus.StatusType.DONE || current.operation == TaskDesc.OpType.VOID){
			// get peek at head of queue
			TaskDesc head = this.taskQueue.peek();
			if(head!=null && this.curTask.compareAndSet(current, head)){
				this.taskQueue.remove(head);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	/** Atomically sets a void TaskDesc into the curTask pointer. Used to clear
	 *  a completed task from the system and allow all other threads to grab a
	 *  new task from the queue.
	 *  @return true if successful, false if failed
	 */
	private boolean completeTask(){
		// get current task in queue
		TaskDesc current = this.curTask.get();
		// check if task is a void type (meaning last task already completed)
		if (current.status.get().stage == DescStatus.StatusType.DONE ||  current.operation != TaskDesc.OpType.VOID){
			// set a new void task into the current head
			return this.curTask.compareAndSet(current, new TaskDesc());
		} else {
			return false;
		}
	}
	/** Atomically sets a void TaskDesc into the curTask pointer. Used to clear
	 *  a completed task from the system and allow all other threads to grab a
	 *  new task from the queue. This version clears a particular task from the
	 *  curTask pointer and is therefore  it is a bit safer.
	 *  @param t TaskDesc assumed to be curTask
	 *  @return true if successful, false if failed
	 */
	private boolean completeTask(TaskDesc t){
		// check if task is marked done or void type (meaning last task already completed)
		if (t.status.get().stage == DescStatus.StatusType.DONE ||  t.operation != TaskDesc.OpType.VOID){
			// set a new void task into the current head
			return this.curTask.compareAndSet(t, new TaskDesc());
		} else {
			return false;
		}
	}
	/** Checks if task is has already been completed by consulting 
	 *  the current task (curTask) and the task queue (taskQueue).
	 *  @param t Task descriptor being checked
	 *  @return true if task is curTask or in queue, else false.
	 */
	private boolean isPendingTask(TaskDesc t){
		return this.taskQueue.contains(t) || this.curTask.get()==t;
	}
	
}