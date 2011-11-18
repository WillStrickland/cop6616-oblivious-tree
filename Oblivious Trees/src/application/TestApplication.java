package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
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
		// initialize actor count 
		ActorCnt = new AtomicInteger(0);
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
		// initialize actor count 
		ActorCnt = new AtomicInteger(0);
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
		// initialize actor count 
		ActorCnt = new AtomicInteger(0);
	}
	/** Initialize psuedo-random number generator
	 *  @return new psuedo-random number generator, null if error
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
	
	// main test driver method
	public static void main(String[] args) {
		//TestAppIO.testActMethods_scanAct();
		//TestAppIO.testActMethods_scanByteArray();
		//testObliviousMethods();
		randomTests(true);
	}
	/** driver method for running multiple random tests to generate 
	 */
	private static void randomTests(boolean summary){
		// set output file names
		List<String> files = new ArrayList<String>();
		files.add("testlog");
		// set tree sizes
		List<Integer> treeSizes = new ArrayList<Integer>();
		//treeSizes.add(Integer.valueOf(10));
		//treeSizes.add(Integer.valueOf(100));
		treeSizes.add(Integer.valueOf(1000));
		treeSizes.add(Integer.valueOf(5000));
		//treeSizes.add(Integer.valueOf(10000));
		// set actor counts
		List<Integer> actorCnts = new ArrayList<Integer>();
		actorCnts.add(Integer.valueOf(1));
		actorCnts.add(Integer.valueOf(2));
		actorCnts.add(Integer.valueOf(4));
		actorCnts.add(Integer.valueOf(8));
		//actorCnts.add(Integer.valueOf(16));
		//actorCnts.add(Integer.valueOf(32));
		// set action counts
		List<Integer> actionCnts = new ArrayList<Integer>();
		//actionCnts.add(Integer.valueOf(1));
		//actionCnts.add(Integer.valueOf(5));
		actionCnts.add(Integer.valueOf(10));
		actionCnts.add(Integer.valueOf(25));
		//actionCnts.add(Integer.valueOf(50));
		//actionCnts.add(Integer.valueOf(100));
		// set number of trials to do with each configuration
		int trials = 3;
		// in case summary, create single output file
		BufferedWriter summaryOut = null;
		if (summary){
			try {
				summaryOut = new BufferedWriter(new FileWriter(files.get(0)+".txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Run every configuration for set number of trials
		for(String fn : files){
			for(Integer treeSize : treeSizes){
				for(Integer actorCnt : actorCnts){
					for(Integer actionCnt : actionCnts){
						for(int i=0; i<trials; i++){
							// create new test instance
							TestApplication test = new TestApplication(treeSize.intValue(),fn+"_"+treeSize+"_"+actorCnt+"_"+actionCnt+"_"+i+".txt");							
							// make actors and actions
							TestActor[] actors = test.mkRandomActors(actorCnt, actionCnt, false);
							// run tests and output results
							if(summary){
								// write summarized (without actions) logs to single file
								test.runTest(summaryOut, actors);
							} else {
								// write detailed (with actions) logs to individual files
								test.runTest(actors);
							}
						}
					}
				}
			}
		}
		// close file
		try {
			summaryOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {}
	}
	/** driver method for running multiple scripted tests to generate
	 */
	private static void scriptedTests(boolean summary){
		// set input file names
		List<String> files = new ArrayList<String>();
		files.add("testlog1.txt");
		int treeSize = 100;
		int trials = 3;
		// in case summary, create single output file
		BufferedWriter summaryOut = null;
		try {
			summaryOut = new BufferedWriter(new FileWriter(files.get(0)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Run every configuration for set number of trials
		for(String fn : files){
			for(int i=0; i<trials; i++){
				// create new test instance

				TestApplication test = new TestApplication(treeSize,fn);							
				// make actors and actions
				TestActor[] actors = test.mkScriptedActors(fn, false);
				// run tests and output results
				if(summary){
					// write summarized (without actions) logs to single file
					test.runTest(summaryOut, actors);
				} else {
					// write detailed (with actions) logs to individual files
					test.runTest(actors);
				}
			}
		}
		// close file
		try {
			summaryOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Component testing methods
	/** code for having random threads do random inserts and deletes on a shared oblivious tree
	 */
	private static void testRndActors(String outFileName){
		// set run parameters
		int actor_count = 10;
		int actor_actions = 10;
		// generate test instance
		TestApplication test = new TestApplication(1000, outFileName);
		// initialize actors
		RandomActor[] actorslist = new RandomActor[actor_count];
		for (int i=0; i<actorslist.length; i++){
			actorslist[i] = new RandomActor();
			actorslist[i].setActCnt(actor_actions);
			actorslist[i].setTest(test);
		}
		// record start time
		long startTime = System.currentTimeMillis();
		// start actors running
		test.ActorCnt.set(actorslist.length);
		for(TestActor a : actorslist){
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
		TestAppIO.writeLogFile(test.outFile, test.tree.getSize(), endTime-startTime, actorslist);
	}
	/** FAILING code for testing the instance methods for Act.scanByteArray
	 */
	private static void testObliviousMethods(){
		int action_count = 10; // number of actions to perform
		boolean showS = true;	// show successful output
		boolean showF = true;	// show failure output
		// generate test instance
		TestApplication test = new TestApplication(10001, "testfile1");
		// test to make sure oblivious tree created valid
		boolean valid = test.tree.verifyTree(test.signatures[1]);
		boolean verified = ObliviousTree.signatureVerify(test.file, test.tree.signatureGenerate(), test.signatures[1]);
		System.out.println("0\tCREATE\t-"+"\tValid="+valid+"\tVerified="+verified);
		// for specified number of actions
		for (int i=0; i<action_count; i++){
			// perform a random action
			Act tmp = test.buttonMash();
			// check to make sure that the tree is still valid
			valid = test.tree.verifyTree(test.signatures[1]);
			verified = ObliviousTree.signatureVerify(test.file, test.tree.signatureGenerate(), test.signatures[1]);
			// if was success and showing successes
			if (valid && verified && showS){
				// print details to screen
				System.out.println((i+1)+"\t"+tmp.getOperation()+"\t-"+"\tValid="+valid+"\tVerified="+verified+"\tAct={"+tmp+"}");
			}
			// if was failure and showing failures
			else if ((!valid || !verified) && showF){
				// print details to screen
				System.out.println((i+1)+"\t"+tmp.getOperation()+"\t-"+"\tValid="+valid+"\tVerified="+verified+"\tAct={"+tmp+"}");
				//break; // exit out of loop
			}
		}
	}
	
	
	// Methods for starting up Actors (random and scripted)
	/** Method to create a specified number of random actors
	 *  with specified number of actions to take
	 *  @param actors number of random actors to create
	 *  @param acts number of random actors are to perform
	 *  @param useTiming sets the useTiming flag of each actor
	 *  @return TestActor[] of RandomActor objects
	 */
	private TestActor[] mkRandomActors(int actors, int acts, boolean useTiming){
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
	 *  @param txt name of file to read acts from
	 *  @param useTiming sets the useTiming flag of each actor
	 *  @return TestActor[] of ScriptedActor objects
	 */
	private TestActor[] mkScriptedActors(String fileName, boolean useTiming){
		return this.mkScriptedActors(TestAppIO.scanLogFile(fileName), useTiming);
	}
	/** Method to create a specified number of random actors
	 *  with specified number of actions to take
	 *  @param txt scanner to read acts from
	 *  @param useTiming sets the useTiming flag of each actor
	 *  @return TestActor[] of ScriptedActor objects
	 */
	private TestActor[] mkScriptedActors(Scanner txt, boolean useTiming){
		return this.mkScriptedActors(TestAppIO.scanLogFile(txt), useTiming);
	}
	/** Method to create a specified number of random actors
	 *  with specified number of actions to take
	 *  @param acts list of all acts to be performed
	 *  @param placement map specifying the map of each act to a numbered actor
	 *  @param useTiming sets the useTiming flag of each actor
	 *  @return TestActor[] of ScriptedActor objects
	 */
	private TestActor[] mkScriptedActors(List<Act> acts, boolean useTiming){
		// compute placement map
		Map<String,Integer> placement = TestAppIO.getPlacements(acts);
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
	/** General method for running a set of actors, getting results and outputting to file
	 * @param actors list of actors to run test with
	 */
	private void runTest(List<TestActor> actors){
		// record start time
		long startTime = System.currentTimeMillis();
		// start actors running
		this.ActorCnt.set(actors.size());
		for(TestActor a : actors){
			a.run();
		}
		// wait until all actors have completed
		while (this.ActorCnt.get()>0){
			try {
				//this.wait();
			} catch (Exception e) {}
		}
		// record end time
		long endTime = System.currentTimeMillis();
		// gather up results and write out to file
		TestAppIO.writeLogFile(this.outFile, this.tree.getSize(), endTime-startTime, actors);
	}
	/** General method for running a set of actors, getting results and outputting to file
	 * @param actors array of actors to run test with
	 */
	private void runTest(TestActor[] actors){
		// record start time
		long startTime = System.currentTimeMillis();
		// start actors running
		this.ActorCnt.set(actors.length);
		for(TestActor a : actors){
			a.run();
		}
		// wait until all actors have completed
		while (this.ActorCnt.get()>0){
			try {
				//this.wait();
			} catch (Exception e) {}
		}
		// record end time
		long endTime = System.currentTimeMillis();
		// gather up results and write out to file
		TestAppIO.writeLogFile(this.outFile, this.tree.getSize(), endTime-startTime, actors);
	}
	/** General method for running a set of actors, getting results and outputting to file
	 * @param actors list of actors to run test with
	 */
	private void runTest(Writer output, List<TestActor> actors){
		// record start time
		long startTime = System.currentTimeMillis();
		// start actors running
		this.ActorCnt.set(actors.size());
		for(TestActor a : actors){
			a.run();
		}
		// wait until all actors have completed
		while (this.ActorCnt.get()>0){
			try {
				//this.wait();
			} catch (Exception e) {}
		}
		// record end time
		long endTime = System.currentTimeMillis();
		// gather up results and write out to file
		TestAppIO.writeLogFile(output, this.outFile, this.tree.getSize(), endTime-startTime, actors);
	}
	/** General method for running a set of actors, getting results and outputting to file
	 * @param actors array of actors to run test with
	 */
	private void runTest(Writer output, TestActor[] actors){
		// record start time
		long startTime = System.currentTimeMillis();
		// start actors running
		this.ActorCnt.set(actors.length);
		for(TestActor a : actors){
			a.run();
		}
		// wait until all actors have completed
		while (this.ActorCnt.get()>0){
			try {
				//this.wait();
			} catch (Exception e) {}
		}
		// record end time
		long endTime = System.currentTimeMillis();
		// gather up results and write out to file
		TestAppIO.writeLogFile(output, this.outFile, this.tree.getSize(), endTime-startTime, actors);
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
			//this.tree.insert(chunk, i, this.signatures[0]);
		} else if (act>=5 && act<=9){
			// position to insert
			int i = this.rnd.nextInt(this.tree.getSize()+1);
			// set act parameters
			a.setOperation(OpType.DELETE);
			a.setLocation(i);
			a.setTime(System.currentTimeMillis()-this.startTime);
			// do delete
			//this.tree.delete(i, this.signatures[0]);
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
		//this.notify();
	}
	
}
