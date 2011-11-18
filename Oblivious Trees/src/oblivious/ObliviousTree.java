package oblivious;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;
import java.util.Random;


/** Oblivious Tree - COP 6616
 * @author William Strickland and Chris Fontaine
 * @version Abstract Class of Sequential and Concurrent Implementations
 */
public abstract class ObliviousTree {

	// File chunk size in bytes
	public static final int CHUNK_SIZE = 100;
	
	// Instance Methods
	abstract public void insert(byte[] value, int i, Signature signer);
	abstract public void delete(int i, Signature signer);
	abstract public byte[] signatureGenerate();
	abstract public int getSize();
	/** Function for checking 2-3 oblivious tree structure
	 *  @param verifier signature to be used to check
	 *  @return true if valid, false if invalid
	 */
	abstract public boolean verifyTree(Signature verifier);

	// Class Methods
	/** verify if a signature is correct given the file and public key
	 *  accepts signatures in the format produced by generateSig()
	 *  verifies each node of tree against children until reaches leaves
	 *  for each leaf verifies against matching chunk of file 
	 *  @param file document to be verified
	 *  @param sig signature tree to be used
	 *  @param verifier Signature to verify tree and file with
	 *  @return true if valid, false if invalid
	 */
	public static boolean signatureVerify(byte[] file, byte[] sig, Signature verifier){
		// construct SignatureArrays from file and signature input
		ObliviousTree.ByteOutArray fileArray = new ObliviousTree.ByteOutArray(0, 5);
		ObliviousTree.ByteOutArray sigArray = new ObliviousTree.ByteOutArray();
		fileArray.data = file;
		sigArray.data = sig;
		try{
			// try and verify the file using signature file and verifier
			signatureVerifyRecurse(fileArray, sigArray, verifier);
		} catch (GeneralSecurityException e){
			return false;
		}
		return true;
	} //*/
	/** recursive function to reconstruct and verify tree and file using verifying signature
	 *  @return byte[] signature data for parent calculation
	 *  @throws GeneralSecurityException when signature verification fails (I know this is terrible...)
	 */
	private static byte[] signatureVerifyRecurse(ByteOutArray file, ObliviousTree.ByteOutArray sig, Signature verifier) throws GeneralSecurityException{
		int sig_size, degree; // signature size and node degree to be read from input
		ByteBuffer buf = ByteBuffer.allocate(4);	// bytebuffer for doing int to byte[] conversions
		byte[] tmp;		// temporary array for holding byte sig of each node
		byte[] data;	// temporary array for storing data to be verified
		
		// read signature size from file
		buf.put(sig.data, sig.index, 4);
		sig_size = buf.getInt(0);
		buf.clear();
		sig.index+=4;
		// read signature
		tmp = Arrays.copyOfRange(sig.data, sig.index, sig.index+sig_size);
		sig.index+=sig_size;
		// read degree
		buf.put(sig.data, sig.index, 4);
		degree = buf.getInt(0);
		//buf.clear();
		sig.index+=4;
		
		// if has children verify against children
		if (degree>0){
			data = null;
			// gather children signatures
			for (int j=0; j<degree; j++){
				// concatenate child segments together
				data = concatArrays(data, signatureVerifyRecurse(file, sig, verifier));
			}
		}
		// verify against file
		else {
			// use the smaller of default chunk size and remaining file portion
			int chunk_size = (file.data.length-file.index > ObliviousTree.CHUNK_SIZE) ? ObliviousTree.CHUNK_SIZE : file.data.length-file.index;
			data = Arrays.copyOfRange(file.data, file.index, file.index+chunk_size);
			file.index+=chunk_size;
		}
		
		// validate signature
		verifier.update(data);
		if (!verifier.verify(tmp)){
			throw new GeneralSecurityException();
		}
		// 
		return tmp;
	}
	
	
	// General Helping Classes/Methods
	
	/** Initialize psuedorandom number generator for class if not already initialized.
	 *  @return new PRNG, null if failure
	 */
	public static Random initPRNG(){	
		try {
			Random tmpRnd = SecureRandom.getInstance("SHA1PRNG");
			byte[] b = new byte[1];
			tmpRnd.nextBytes(b);
			return tmpRnd;
		} catch (NoSuchAlgorithmException e){
			return null;
		}
	}
	/** Get information about psuedorandom number generator used.
	 *  @return String describing psuedorandom number generator algorithm
	 */
	public static String PRNG_Info(Random rndSrc){
		if (rndSrc != null){
			String txt;
			try {
				txt = " - " + ((SecureRandom) rndSrc).getAlgorithm() + " - " + ((SecureRandom) rndSrc).getProvider().toString();
			} catch (ClassCastException e){
				txt = "";
			}
			return rndSrc.getClass().getName() + txt;
		} else {
			return "PRNG not initialized!";
		}
	}
	/** helper class for storing state of signature output
	 *  used for outputting signatures and verification
	 */
	public static class ByteOutArray {
		public int index;
		public byte[] data;
		public ByteOutArray(){
			index = 0;
			data = new byte[1];
		}
		public ByteOutArray(int i, int size){
			index = i;
			data = new byte[size];
		}
		/** Append given array onto end of storage array
		 *  double size of storage array as needed to store new data
		 *  @param b byte array to be appended
		 */
		public void append(byte[] n){
			int newsize = this.data.length;
			// while out array is not big enough to append new data
			while((newsize-this.index)<n.length){
				// double size
				newsize *= 2;
			}
			// if required size bigger than current size
			if (this.data.length < newsize){
				// when big enough remake and copy old data
				this.data = Arrays.copyOf(this.data, newsize);
			}
			// append new data onto end
			System.arraycopy(n, 0, this.data, this.index, n.length);
			this.index += n.length;
		}
		/** Prepare storage array to store additional data
		 *  If array is not large enough to store given amount 
		 *  Will double size (repeatedly).
		 * @param n amount of additional data to appended
		 */
		public void append(int n){
			int newsize = this.data.length;
			// while out array is not big enough to append new data
			while((newsize-this.index)<n){
				// double size
				newsize *= 2;
			}
			// if required size bigger than current size
			if (this.data.length < newsize){
				// when big enough and copy old data into new big array
				this.data = Arrays.copyOf(this.data, newsize);
			}
		}		
	}
	/** helper method to concatenate byte arrays together
	 * ordered as AB, will accept either as null
	 * @param A first array
	 * @param B second array
	 * @return byte[] concatenated arrays, null if both arrays null
	 */
	protected static byte[] concatArrays(byte[] A, byte[] B){
		if(A==null && B==null){
			return null;
		} else if (A==null){
			return B;
		} else if(B==null){
			return A;
		} else {
			byte[] tmp = Arrays.copyOf(A, A.length+B.length);
			System.arraycopy(B, 0, tmp, A.length, B.length);
			return tmp;
		}
	}
	
	/* Misc Junk Code */
	
	/* failed attempt at breadth-first implementation
	 * generate the signature output of algorithm
	 * outputs each node in signature as {sig_size}{sig}{degree} in breadth-first order
	 *  @return byte[] of current complete signature, null if failure
	 */ 
	/* BFS Generate Signature
	public byte[] generateSig(){
		byte[] rtn = new byte[128];	// signature output byte array
		int i=0;	// index into output array
		LinkedList<OTree_Elem> nodeQueue = new LinkedList<OTree_Elem>();	// list of unprocessed nodes
		ByteBuffer buf = ByteBuffer.allocate(4);	// bytebuffer for doing int to byte[] conversions
		byte[] tmp;	// temporary array for holding byte rep of each node
		
		// add root to queue to begin
		nodeQueue.add(this.root);
		// output tree with breadth-first traversal
		while(!nodeQueue.isEmpty()){
			// get next node
			OTree_Elem thisNode = nodeQueue.remove();
			// add children of this node to queue
			nodeQueue.addAll(Arrays.asList(thisNode.getChildren()));
			// get byte signature of current node
			tmp = thisNode.getSig();
			// while rtn not big enough
			while((rtn.length-i)<(tmp.length+8)){
				// resize rtn to double
				rtn = Arrays.copyOf(rtn, rtn.length*2);
			}
			// write node into rtn 
			// prepend with signature size
			buf.putInt(0, tmp.length);
			// copy signature into rtn
			System.arraycopy(buf.array(), 0, rtn, i, 4);
			buf.clear();
			i+=4;
			// copy signature into rtn
			System.arraycopy(tmp, 0, rtn, i, tmp.length);
			i += tmp.length;
			// append with degree
			buf.putInt(0, thisNode.getDegree());
			System.arraycopy(buf.array(), 0, rtn, i, 4);
			//buf.clear();
			i+=4;
		}
		// return truncated array of just signatures
		return Arrays.copyOf(rtn, i);
	} //*/
}
