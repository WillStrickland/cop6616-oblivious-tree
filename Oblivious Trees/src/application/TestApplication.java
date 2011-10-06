package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
