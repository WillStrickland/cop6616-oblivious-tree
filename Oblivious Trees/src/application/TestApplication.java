package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import oblivious.trees.ObliviousTree;

public class TestApplication {

	public static void main(String[] args) {
		
		// file to be signed
		FileInputStream file;
		String filename = "NONE";
		// open file based on user input
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			filename = reader.readLine();
			System.out.println("Opening "+filename);
			file = new FileInputStream(filename); 
		} catch (Exception e){
			System.out.println("could not open file - "+filename);
			return;
		}
		
		//ObliviousTree OT = new ObliviousTree(file);

	}

}
