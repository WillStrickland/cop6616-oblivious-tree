package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;

import oblivious.trees.ObliviousTree;

@SuppressWarnings("unused")
public class TestApplication {

	public static void main(String[] args) {
		
		

	}
	
	/** code for opening file and creating OTree from it 
	 */
	private static void testOpenFile(){
		// file to be signed
		FileInputStream file;
		String filename = "NONE";
		// open file based on user input
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter file name: ");
			filename = reader.readLine();
			System.out.println("Opening "+filename);
			file = new FileInputStream(filename);
			
			
			//ObliviousTree OT = new ObliviousTree(file);
			
			
			System.out.println("Closing file");
			file.close();
		} catch (FileNotFoundException e){
			System.out.println("could not open file - "+filename);
		} catch (IOException e){
			System.out.println("General IO Error - "+filename);
		} catch (Exception e){
			System.out.println("Unknown Error");
		}
	}
	
	/** code for having random threads do random inserts and deletes on a shared oblivious tree
	 */
	private static void testRndActions(){
		// get signature objects for signing and verifying
		// [0] is for signing
		// [1] is for verifying
		Signature[] signatures = initSignature();
		SecureRandom rnd = initPRNG();
		
		ObliviousTree OT = createOTree(rnd, signatures[0]);
		
		int Actor_Actions = 10;
		TestApplication.rndActor[] Actors = new TestApplication.rndActor[10];
		// initalize actors
		for (int i=0; i<Actors.length; i++){
			Actors[i] = new rndActor();
			Actors[i].setActions(Actor_Actions);
			Actors[i].setOTree(OT);
			Actors[i].setSigs(signatures);
		}
		// start actors running
		for(int i=0; i<Actors.length; i++){
			Actors[i].run();
		}
		
		
	}
	
	/** Initialize psuedorandom number generator
	 *  @return new psuedorandom number generator, null if error
	 */
	private static SecureRandom initPRNG(){	
		SecureRandom tmp;
		try {
			tmp = SecureRandom.getInstance("SHA1PRNG");
			byte[] b = new byte[1];
			tmp.nextBytes(b);
			return tmp;
		} catch (NoSuchAlgorithmException e){
			return null;
		}
	}
	/** Generates a public-private key pair at random and
	 *  returns a signature for signing and another for verifying
	 *  @return Signature[] indices: 0 = signing, 1 = verifying
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
	
	
	private static class rndActor implements Runnable {
		public static final int MAX_SLEEP = 1000;
		private SecureRandom rnd;
		private Signature[] sigs;
		private ObliviousTree tree;
		private int actions;
		public rndActor(){
			this.actions = 0;
			this.tree = null;
			this.sigs = null;
			this.rnd = TestApplication.initPRNG();
		}
		public boolean setActions(int a){
			if (a>0){
				this.actions = a;
				return true;
			} else {
				return false;
			}
		}
		public boolean setOTree(ObliviousTree o){
			if (o!=null){
				this.tree = o;
				return true;
			} else {
				return false;
			}
		}
		public boolean setSigs(Signature[] s){
			if (s!=null && s.length>=2){
				this.sigs[0] = s[0];
				this.sigs[1] = s[1];
				return true;
			} else {
				return false;
			}
		}
		public void run(){
			if (this.tree!=null && this.sigs!=null){
				while (this.actions>0){
					try{
						Thread.sleep(rnd.nextInt(rndActor.MAX_SLEEP));
					} catch (InterruptedException e){
						
					}
					// Roll for random action
					int act = rnd.nextInt(11);
					if (act>=0 && act<=4){
						TestApplication.insertOTree(this.tree, this.rnd, sigs[0]);
					} else if (act>=5 && act<=9){
						TestApplication.deleteOTree(this.tree, this.rnd, sigs[0]);
					} else if (act>=10 && act<=10){
						TestApplication.sigOTree(this.tree);
					}
					this.actions--;
				}
			}
		}
	}
	
	
	/** code to create a OTree from some random data
	 *  @return new Oblivious Tree
	 */
	private static ObliviousTree createOTree(SecureRandom rnd, Signature signer){
		int init_size = 10; // initial size in chunks
		byte[] file = new byte[init_size*ObliviousTree.CHUNK_SIZE];
		rnd.nextBytes(file);
		return new ObliviousTree(file, signer);
	}
	/** insert a random chunk of data into a random position in an oblivious tree
	 *  @param o ObliviousTree
	 *  @param signer signature for signing
	 */
	private static void insertOTree(ObliviousTree o, SecureRandom rnd, Signature signer){
		// position to insert
		int i;
		i = rnd.nextInt(o.getSize()+1);
		// chunk to insert
		byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
		rnd.nextBytes(chunk);
		// do insert
		o.insert(chunk, i, signer);
	}
	/** delete a chunk at random from an oblivious tree
	 *  @param o ObliviousTree
	 *  @param signer signature for signing
	 */
	private static void deleteOTree(ObliviousTree o, SecureRandom rnd, Signature signer){
		// position to delete
		int i;
		i = rnd.nextInt(o.getSize());
		// do delete
		o.delete(i, signer);
	}
	/** get the whole signature of an oblivious tree
	 *  @param o ObliviousTree
	 */
	private static void sigOTree(ObliviousTree o){
		byte[] tmp = o.signatureGenerate();	
	}
	

}
