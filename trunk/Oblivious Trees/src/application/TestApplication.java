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
import java.util.Random;

import oblivious.concurrent.ConcurrentObliviousTree;
import oblivious.sequential.ObliviousTree;

@SuppressWarnings("unused")
public class TestApplication {

	// Some state stuff
	ObliviousTree tree;
	Random rnd;
	// get signature objects for signing and verifying
	// [0] is for signing
	// [1] is for verifying
	Signature[] signatures;
	
	public TestApplication(){
		rnd = initPRNG();
		signatures = initSignature();
	}
	
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
		TestApplication test = new TestApplication();		
		ObliviousTree OT = createOTree(test.rnd, test.signatures[0]);
		
		int Actor_Actions = 10;
		RandomActor[] Actors = new RandomActor[10];
		// initalize actors
		for (int i=0; i<Actors.length; i++){
			Actors[i] = new RandomActor();
			Actors[i].setActions(Actor_Actions);
			Actors[i].setTest(test);
		}
		// start actors running
		for(int i=0; i<Actors.length; i++){
			Actors[i].run();
		}
		
		
	}
	
	/** Initialize psuedorandom number generator
	 *  @return new psuedorandom number generator, null if error
	 */
	public static SecureRandom initPRNG(){	
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
	public static Signature[] initSignature(){
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
	
	/** code to create a OTree from some random data
	 *  @return new Oblivious Tree
	 */
	public static ObliviousTree createOTree(Random rnd, Signature signer){
		int init_size = 10; // initial size in chunks
		byte[] file = new byte[init_size*ObliviousTree.CHUNK_SIZE];
		rnd.nextBytes(file);
		return new ObliviousTree(file, signer);
	}
	/** insert a random chunk of data into a random position in an oblivious tree
	 *  @param o ObliviousTree
	 *  @param signer signature for signing
	 */
	public static void insertOTree(ObliviousTree o, Random rnd, Signature signer){
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
	public static void deleteOTree(ObliviousTree o, Random rnd, Signature signer){
		// position to delete
		int i;
		i = rnd.nextInt(o.getSize());
		// do delete
		o.delete(i, signer);
	}
	/** get the whole signature of an oblivious tree
	 *  @param o ObliviousTree
	 */
	public static void sigOTree(ObliviousTree o){
		byte[] tmp = o.signatureGenerate();	
	}
	
	public void buttonMash(){
		// Roll for random action
		int act = rnd.nextInt(11);
		if (act>=0 && act<=4){
			TestApplication.insertOTree(this.tree, this.rnd, this.signatures[0]);
		} else if (act>=5 && act<=9){
			TestApplication.deleteOTree(this.tree, this.rnd, this.signatures[0]);
		} else if (act>=10 && act<=10){
			TestApplication.sigOTree(this.tree);
		}
	}
	public void buttonMash(int i){
		int act = i;
		if (act>=0 && act<=4){
			TestApplication.insertOTree(this.tree, this.rnd, this.signatures[0]);
		} else if (act>=5 && act<=9){
			TestApplication.deleteOTree(this.tree, this.rnd, this.signatures[0]);
		} else if (act>=10 && act<=10){
			TestApplication.sigOTree(this.tree);
		}
	}
	public int buttonPick(){
		return 1;
	}
	
	

}
