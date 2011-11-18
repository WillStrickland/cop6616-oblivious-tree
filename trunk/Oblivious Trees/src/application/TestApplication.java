package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import application.Act.OpType;
import oblivious.ObliviousTree;
import oblivious.concurrent.ConcurrentObliviousTree;
import oblivious.sequential.SequentialObliviousTree;

@SuppressWarnings("unused")
public class TestApplication {

	// Some state stuff
	private Random rnd;
	// get signature objects for signing and verifying: [0] signing, [1] verifying
	private Signature[] signatures;
	private ObliviousTree tree;
	private byte[] file;
	private String outFile;
	private long startTime;
	private AtomicInteger ActorCnt;

	
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
	 * @param filesize size (in chunks) of random input to use
	 */
	public TestApplication(int filesize, String outfile){
		// rnd and crypto
		rnd = initPRNG();
		signatures = initSignature();
		// create file
		file = new byte[filesize*ObliviousTree.CHUNK_SIZE];
		rnd.nextBytes(file);
		// build tree
		tree = new SequentialObliviousTree(file, signatures[0]);
		// set output file name
		this.outFile = outfile;
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
		testObliviousMethods();
		//testActMethods_scanByteArray();
	}
	
	// Component testing methods
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
	private static void testRndActors(String outFileName){
		// set run parameters
		int actor_count = 10;
		int actor_actions = 10;
		// generate test instance
		TestApplication test = new TestApplication(1000, outFileName);
		// initalize actors
		RandomActor[] Actors = new RandomActor[actor_count];
		for (int i=0; i<Actors.length; i++){
			Actors[i] = new RandomActor();
			Actors[i].setActCnt(actor_actions);
			Actors[i].setTest(test);
		}
		// record start time
		long startTime = System.currentTimeMillis();
		// start actors running
		test.ActorCnt.set(Actors.length);
		for(TestActor a : Actors){
			a.run();
		}
		// wait until all actors have completed
		while (test.ActorCnt.get()>0){
			try {
				test.wait();
			} catch (Exception e) {}
		}
		// record end time
		long endTime = System.currentTimeMillis();
		// gather up results and write out to file
		try {
			// open/create file
			BufferedWriter output = new BufferedWriter(new FileWriter(test.outFile));
			// output runtime (in milliseconds)
			output.write(endTime-startTime+"\n");
			// output all actions by all actors
			for (TestActor actor : Actors){
				for (Act a : actor.actions){
					output.write(a.toString());
				}
			}

			// close file
			output.close();
		} catch (Exception e){}
		
	}
	
	/** MUSTRUN code for testing the instance methods for Act.scanByteArray
	 */
	private static void testObliviousMethods(){
		int action_count = 10; // number of actions to perform
		boolean showS = true;	// show successful case output
		boolean showF = true;	// show failure case output
		// generate test instance
		TestApplication test = new TestApplication(10, "iamtheverymodelofamodernmajorgeneral");
		// test to make sure oblivious tree created valid
		boolean valid = ObliviousTree.signatureVerify(test.file, test.tree.signatureGenerate(), test.signatures[1]);
		System.out.println("0\tTreeCreate\t - Valid="+valid);
		// for specified number of actions
		for (int i=0; i<action_count; i++){
			// perform a random action
			Act tmp = test.buttonMash();
			// check to make sure that the tree is still valid
			valid = ObliviousTree.signatureVerify(test.file, test.tree.signatureGenerate(), test.signatures[1]);
			if (valid && showS){
				//System.out.println(i+"\t"+tmp.getOperation()+" - Valid="+valid+"\t"+tmp);
			} else if (!valid && showF){
				System.out.println(i+"\t"+tmp.getOperation()+" - Valid="+valid+"\t"+tmp);
				//break;
			}
		}
		
		
		
	}
	
	/** SUCCESSFUL code for testing the instance methods for Act.scanByteArray
	 */
	private static void testActMethods_scanAct(){
		Random rnd = initPRNG();	// Random source
		byte[] data = new byte[12];
		rnd.nextBytes(data);
		Act a1 = new Act();
		a1.setCallerNm("BLAHS");
		a1.setOperation(Act.OpType.INSERT);
		a1.setTime(214214);
		a1.setLocation(3235);
		a1.setData(data);
		String tmp1 = a1.toString();
		Act a2 = Act.scanAct(new Scanner(tmp1));
		String tmp2 = a2.toString();
		if (tmp1.equals(tmp2)){
			System.out.print("success\n"+tmp1+"\n"+tmp2+"\n");
		} else {
			System.out.print("failure\n"+tmp1+"\n"+tmp2+"\n");
		}
	}
	/** SUCCESSFUL code for testing the instance methods for Act.scanByteArray
	 */
	private static void testActMethods_scanByteArray(){
		Random rnd = initPRNG();	// Random source
		int numRounds = 100000;		// number to rounds to test
		int maxdata = 1000;		// maximum data size
		boolean showS = false;	// show successful case output
		boolean showF = true;	// show failure case output
		int s=0, f=0; //success and failure count
		for (int i=0; i<numRounds; i++){
			byte[] data = null;
			String tmp1 = null;
			String tmp2 = null;
			try{
				boolean success = false;
				data = new byte[rnd.nextInt(maxdata)];
				rnd.nextBytes(data);
				tmp1 = Arrays.toString(data);
				tmp2 = Arrays.toString(Act.scanByteArray(new Scanner(tmp1)));
				// compute success or fail
				if(tmp1.equals(tmp2)){
					success = true;
					s++;
				} else {
					f++;
				} 
				// display output (conditionally)
				if (success && showS){
					System.out.print("success#"+s+"\n"+tmp1+"\n"+tmp2+"\n");
				} else if (!success && showF){
					System.out.print("failure#"+f+"\n"+tmp1+"\n"+tmp2+"\n");
				}
			} catch (Exception e){
				// print error state to screen
				System.out.println("(s="+s+",f="+(f++)+")\tERROR: n="+data.length+" tmp1="+tmp1+" tmp2="+tmp2);
			}
		}
		System.out.print("#successes="+s+" #failures="+f);
	}
	
	// Methods for starting up Actors (random and scripted)
	/** Method to create a specified number of random actors
	 *  with specified number of actions to take
	 *  @param actors number of random actors to create
	 *  @param acts number of random actors are to perform
	 *  @param useTiming sets the useTiming flag of each actor
	 *  @return TestActor[] of RandomActor objects
	 */
	private TestActor[] castRandomActors(int actors, int acts, boolean useTiming){
		// create array of actors
		RandomActor[] Actors = new RandomActor[actors];
		// Initialize actors
		for (int i=0; i<Actors.length; i++){
			Actors[i] = new RandomActor();
			Actors[i].setActCnt(acts);
			Actors[i].setTest(this);
			Actors[i].setUseTiming(useTiming);
		}
		// return actor array
		return Actors;
	}
	/** Method to create a specified number of random actors
	 *  with specified number of actions to take
	 *  @param acts list of all acts to be performed
	 *  @param placement map specifying the map of each act to a numbered actor
	 *  @param useTiming sets the useTiming flag of each actor
	 *  @return TestActor[] of RandomActor objects
	 */
	private TestActor[] castScriptedActors(List<Act> acts, Map<String,Integer> placement, boolean useTiming){
		// create array of actors
		ScriptedActor[] Actors = new ScriptedActor[placement.size()];
		// create list of acts for each actor
		ArrayList<ArrayList<Act>> ActSets = new ArrayList<ArrayList<Act>>(placement.size());
		// Initialize first dimension of ActSets
		for (ArrayList<Act> AL : ActSets){
			AL = new ArrayList<Act>();
		}
		// Iterate all acts and place into correct list for correct actor using placement map
		for (Act a : acts){
			ActSets.get(placement.get(a.getCallerNm()).intValue()).add(a);
		}
		// Initialize actors
		for (int i=0; i<Actors.length; i++){
			Actors[i] = new ScriptedActor();
			Actors[i].setActions(ActSets.get(i));
			Actors[i].setTest(this);
			Actors[i].setUseTiming(useTiming);
		}
		// return actors array
		return Actors;
	}
	/** Create a mapping of all unique callers identified in list of
	 *  Acts to a unique assigned number. Callers are identified by
	 *  the getCallerNm() string method. Numbering starts at zero 
	 *  and increments by one for new caller found. Callers are number
	 *  in the order they occur in the list given by list iterator.
	 *  @param acts list of all acts to 
	 *  @return Map<String,Integer> map of unique caller names to unique numbers
	 */
	private static Map<String,Integer> getPlacements(List<Act> acts){
		// set initial actor number
		int i = 0;
		// Initialize map of caller name to caller index
		Map<String,Integer> tmp = new HashMap<String,Integer>();
		// iterate acts list
		for (Act a : acts){
			// if the map does not yet contain this caller
			if(!tmp.containsKey(a.getCallerNm())){
				// add the 
				tmp.put(a.getCallerNm(), Integer.valueOf(i++));
			}
		}
		return tmp;
	}
	// Psuedo-serialization methods
	/** Reads in scanner of previous test output and constructs list
	 *  of all acts. Utilizes Act.scanAct method to accomplish this.
	 *  Compatible with writeActors and writeActs methods below.
	 *  @param txt Scanner holding input
	 *  @return List of Acts scanned from input
	 */
	private static List<Act> scanActs(Scanner txt){
		ArrayList<Act> alist = new ArrayList<Act>();
		while (txt.hasNext()){
			// scan Act from input
			Act tmp = Act.scanAct(txt);
			if(tmp!=null){
				// if success, add to list
				alist.add(tmp);
			} else {
				// else, there was a problem!
				break;
			}
		}
		// return list
		return alist;
	}
	/** Outputs list of all actions by all actors provided
	 *  Compatible with scanActs method above
	 *  @param actors List of actors with actions to output
	 *  @return String representing list of all Acts from all Actors
	 */
	private static String writeActors(List<TestActor> actors){
		// Initialize temporary text string
		String txt = new String();
		// for each actor
		for(TestActor A : actors){
			// for each act of that actor
			for(Act a : A.getActions()){
				// concatenate this act.toString() onto the
				// temporary string followed by a newline
				txt += a.toString() + "\n";
			}
		}
		// return resulting string
		return txt;
	}
	/** Outputs list of all actions by all actors provided
	 *  Compatible with scanActs method above
	 *  @param actors array of actors with actions to output
	 *  @return String representing list of all Acts from all Actors
	 */
	private static String writeActors(TestActor[] actors){
		// Initialize temporary text string
		String txt = new String();
		// for each actor
		for(TestActor A : actors){
			// for each act of that actor
			for(Act a : A.getActions()){
				// concatenate this act.toString() onto the
				// temporary string followed by a newline
				txt += a.toString() + "\n";
			}
		}
		// return resulting string
		return txt;
	}
	/** Outputs list of all actions by all actors
	 *  Compatible with scanActs method above
	 *  @param acts List<Act> to be outputted
	 *  @return String representing list of all Acts
	 */
	private static String writeActs(List<Act> acts){
		// Initialize temporary text string
		String txt = new String();
		// for each act
		for(Act a : acts){
			// concatenate this act.toString() onto the
			// temporary string followed by a newline
			txt += a.toString() + "\n";
		}
		// return resulting string
		return txt;
	}
	/** Outputs list of all actions provided
	 *  Compatible with scanActs method above
	 *  @param acts Act[] to be outputted
	 *  @return String representing list of all Acts
	 */
	private static String writeActs(Act[] acts){
		// Initialize temporary text string
		String txt = new String();
		// for each act
		for(Act a : acts){
			// concatenate this act.toString() onto the
			// temporary string followed by a newline
			txt += a.toString() + "\n";
		}
		// return resulting string
		return txt;
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
	/** Execute scripted action described by Act
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
	/** Used to notify the main method that an actor has completed.
	 *  decrements the actor count and notifies the main (which 
	 *  will be waiting on this test object).
	 */
	public void notifyActorComplete(){
		this.ActorCnt.decrementAndGet();
		this.notify();
	}
	
}
