package oblivious.concurrent;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Random;
import java.util.Vector;

import application.TestApplication;

@SuppressWarnings("unused")
public class Tests {

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
			//testVerify();
			
		} //*/
		
		/* test class for making sure verification and such works
		private static void testVerify(){
			// get signature objects for signing and verifying
			// [0] is for signing
			// [1] is for verifying
			Signature[] signatures = TestApplication.initSignature();	
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
	
}
