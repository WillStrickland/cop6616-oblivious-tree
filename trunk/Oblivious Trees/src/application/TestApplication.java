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
import java.util.Arrays;
import java.util.Random;

import application.Act.OpType;

import oblivious.ObliviousTree;
import oblivious.concurrent.ConcurrentObliviousTree;
import oblivious.sequential.SequentialObliviousTree;

@SuppressWarnings("unused")
public class TestApplication {

	// Some state stuff
	Random rnd;
	// get signature objects for signing and verifying: [0] signing, [1] verifying
	Signature[] signatures;
	ObliviousTree tree;
	byte[] file;
	long startTime;
	
	/** default constructor 
	 */
	public TestApplication(){
		// rnd and crypto
		rnd = initPRNG();
		signatures = initSignature();
		// create file
		int init_size = 10; // initial size in chunks
		file = new byte[init_size*ObliviousTree.CHUNK_SIZE];
		rnd.nextBytes(file);
		// build tree
		tree = new SequentialObliviousTree(file, signatures[0]);
		// set start time
		startTime = System.currentTimeMillis();
	}
	/** TestApplication constructor
	 * @param filesize size of random input to use
	 */
	public TestApplication(int filesize){
		// rnd and crypto
		rnd = initPRNG();
		signatures = initSignature();
		// create file
		file = new byte[filesize*ObliviousTree.CHUNK_SIZE];
		rnd.nextBytes(file);
		// build tree
		tree = new SequentialObliviousTree(file, signatures[0]);
		// set start time
		startTime = System.currentTimeMillis();
	}
	/** TestApplication constructor
	 * @param f byte file to use as input
	 */
	public TestApplication(byte[] f){
		// rnd and crypto
		rnd = initPRNG();
		signatures = initSignature();
		// create file
		file = Arrays.copyOf(f, f.length);
		rnd.nextBytes(file);
		// build tree
		tree = new SequentialObliviousTree(file, signatures[0]);
		// set start time
		startTime = System.currentTimeMillis();
	}
	/** Initialize psuedorandom number generator
	 *  @return new psuedorandom number generator, null if error
	 */
	public static Random initPRNG(){	
		Random tmp;
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
		// generate test application
		TestApplication test = new TestApplication();
		
		int Actor_Actions = 10;
		RandomActor[] Actors = new RandomActor[10];
		// initalize actors
		for (int i=0; i<Actors.length; i++){
			Actors[i] = new RandomActor();
			Actors[i].setActCnt(Actor_Actions);
			Actors[i].setTest(test);
		}
		// start actors running
		for(int i=0; i<Actors.length; i++){
			Actors[i].run();
		}
	}
	
	// Methods for Actors to perform actions on instance oblivious Tree
	/** Perform a random action then on instance oblivious tree
	 * @return Act describing the action done
	 */
	public Act buttonMash(){
		Act a = new Act();
		// Roll for random action
		int act = rnd.nextInt(11);
		if (act>=0 && act<=4){
			// position to insert
			int i = this.rnd.nextInt(this.tree.getSize()+1);
			// chunk to insert
			byte[] chunk = new byte[ObliviousTree.CHUNK_SIZE];
			this.rnd.nextBytes(chunk);
			// set act parameters
			a.setOperation(OpType.INSERT);
			a.setLocation(i);
			a.setData(chunk);
			a.setTime(System.currentTimeMillis()-this.startTime);
			// do insert
			this.tree.insert(chunk, i, this.signatures[0]);
		} else if (act>=5 && act<=9){
			// position to insert
			int i = this.rnd.nextInt(this.tree.getSize()+1);
			// set act parameters
			a.setOperation(OpType.DELETE);
			a.setLocation(i);
			a.setTime(System.currentTimeMillis()-this.startTime);
			// do delete
			this.tree.delete(i, this.signatures[0]);
		} else if (act>=10 && act<=10){
			// set act parameters
			a.setOperation(OpType.GENSIG);
			a.setTime(System.currentTimeMillis()-this.startTime);
			// do generate signatures
			this.tree.signatureGenerate();
		}
		return a;
	}
	/** execute scripted action described by Act
	 *  @param a Action to perform on instance oblivious tree
	 */
	public void buttonPush(Act a){
		switch (a.getOperation()){
			case INSERT:
				this.tree.insert(a.getData(), a.getLocation(), this.signatures[0]);
				break;
			case DELETE:
				this.tree.delete(a.getLocation(), this.signatures[0]);
				break;
			case GENSIG:
				this.tree.signatureGenerate();
				break;
			default:
				break;
		}	
	}
	

}
