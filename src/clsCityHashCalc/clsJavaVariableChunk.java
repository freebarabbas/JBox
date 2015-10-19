package clsCityHashCalc;

import java.io.IOException;

public class clsJavaVariableChunk {
	// native method that prints a prompt and reads a line
	private native String getVariableChunkProfile(String prompt, int mod);
	static {
		System.loadLibrary("clsJavaVariableChunk");
	}	
	public static String GetVariableChunks(String strPath, int mod) throws IOException {
		 clsJavaVariableChunk vc = new clsJavaVariableChunk();
		 String input = vc.getVariableChunkProfile(strPath,mod);
		 return input;
	 }
}
