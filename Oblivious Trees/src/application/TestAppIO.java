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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class TestAppIO {
	
	// General helpers
	/** Create a mapping of all unique callers identified in list of
	 *  Acts to a unique assigned number. Callers are identified by
	 *  the getCallerNm() string method. Numbering starts at zero 
	 *  and increments by one for new caller found. Callers are number
	 *  in the order they occur in the list given by list iterator.
	 *  @param acts list of all acts to 
	 *  @return Map<String,Integer> map of unique caller names to unique numbers
	 */
	static Map<String,Integer> getPlacements(List<Act> acts){
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
	/** Reads in the actions performed in a given log file.
	 *  Compatible with writeLogFile method below.
	 *  @param txt Scanner containing the input
	 *  @return list of Actions recorded in log file
	 */
	static List<Act> scanLogFile(Scanner txt){
		// ignore first line
		txt.nextLine();
		// read in and return all remaining actions
		return scanActs(txt);
	}
	/** Reads in the actions performed in a given log file.
	 *  Compatible with writeLogFile method below.
	 *  @param txt Scanner containing the input
	 *  @return list of Actions recorded in log file
	 */
	static List<Act> scanLogFile(String fileName){
		List<Act> rtn = null;
		try {
			// open file with scanner
			Scanner txt = new Scanner(new File(fileName));
			// call overloaded version that works on scanner
			rtn = scanLogFile(txt);
		} catch (IOException e) {}
		// return result from scanner version
		return rtn;
	}
	/** Writes test log information out to a specified file.
	 *  Compatible with scanLogFile method above.
	 *  @param fileName name of output file
	 *  @param elapseTime total elapsed time of test
	 *  @param actors list of actors for which to output actions
	 */
	static void writeLogFile(String fileName, int treeSize, long elapseTime, List<TestActor> actors){
		try {
			// open/create file
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			//get the act count
			int actCnt =0;
			for (TestActor a : actors){
				actCnt += a.getActions().size();
			}
			// write general test information on first line
			output.write(fileName);
			output.write("\t"+treeSize);
			output.write("\t"+actors.size());
			output.write("\t"+actCnt);
			output.write("\t"+elapseTime);
			output.write("\n");
			// output all actions by all actors
			output.write(writeActors(actors));
			// close file
			output.close();
		} catch (Exception e){}
	}
	/** Writes test log information out to a specified file.
	 *  Compatible with scanLogFile method above.
	 *  @param fileName name of output file
	 *  @param elapseTime total elapsed time of test
	 *  @param actors array of actors for which to output actions
	 */
	static void writeLogFile(String fileName, int treeSize, long elapseTime, TestActor[] actors){
		try {
			// open/create file
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
			//get the act count
			int actCnt =0;
			for (TestActor a : actors){
				actCnt += a.getActions().size();
			}
			// write general test information on first line
			output.write(fileName);
			output.write("\t"+treeSize);
			output.write("\t"+actors.length);
			output.write("\t"+actCnt);
			output.write("\t"+elapseTime);
			output.write("\n");
			// output all actions by all actors
			output.write(writeActors(actors));
			// close file
			output.close();
		} catch (IOException e){}
	}
	/** Writes multi-test log information out to a specified file.
	 *  Meant for multiple test runs, does not print act log and is not compatible with scanLogFile above.
	 *  @param output output file to be appended to
	 *  @param testName name of this test run
	 *  @param elapseTime total elapsed time of test
	 *  @param actors list of actors for which to output actions
	 */
	static void writeLogFile(Writer output, String testName, int treeSize, long elapseTime, List<TestActor> actors){
		try {
			//get the act count
			int actCnt =0;
			for (TestActor a : actors){
				actCnt += a.getActions().size();
			}
			// write general test information on first line
			output.write(testName);
			output.write("\t"+treeSize);
			output.write("\t"+actors.size());
			output.write("\t"+actCnt);
			output.write("\t"+elapseTime);
			output.write("\n");
			// output all actions by all actors
			output.write(writeActors(actors));
		} catch (Exception e){}
	}
	/** Writes multi-test log information out to a specified file.
	 *  Meant for multiple test runs, does not print act log and is not compatible with scanLogFile above.
	 *  @param output output file to be appended to
	 *  @param testName name of this test run
	 *  @param elapseTime total elapsed time of test
	 *  @param actors array of actors for which to output actions
	 */
	static void writeLogFile(Writer output, String testName, int treeSize, long elapseTime, TestActor[] actors){
		try {
			//get the act count
			int actCnt =0;
			for (TestActor a : actors){
				actCnt += a.getActions().size();
			}
			// write general test information on first line
			output.write(testName);
			output.write("\t"+treeSize);
			output.write("\t"+actors.length);
			output.write("\t"+actCnt);
			output.write("\t"+elapseTime);
			output.write("\n");
		} catch (Exception e){}
	}
	/** Reads in scanner of previous test output and constructs list
	 *  of all acts. Utilizes Act.scanAct method to accomplish this.
	 *  Compatible with writeActors and writeActs methods below.
	 *  @param txt Scanner holding input
	 *  @return List of Acts scanned from input
	 */
	static List<Act> scanActs(Scanner txt){
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
	static String writeActors(List<TestActor> actors){
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
	static String writeActors(TestActor[] actors){
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
	static String writeActs(List<Act> acts){
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
	static String writeActs(Act[] acts){
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

	// Misc methods
	/** SUCCESSFUL code for testing the instance methods for Act.scanAct
	 */
	static void testActMethods_scanAct(){
		Random rnd = TestApplication.initPRNG();	// Random source
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
	static void testActMethods_scanByteArray(){
		Random rnd = TestApplication.initPRNG();	// Random source
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
	/** code for opening file and creating OTree from it 
	 */
	@SuppressWarnings("unused")
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
	
	
}
