package clsCityHashCalc;

import java.io.IOException;

public class clsJavaVariableChunk {
	// native method that prints a prompt and reads a line
	private native String getVariableChunkProfile(String prompt, int power, int mod, int divide, int refactor, double dblmin, double dblmx);
	static {
		
		System.loadLibrary("clsJavaVariableChunk");
	}	
	public static String GetVariableChunks(String strPath, int power, int mod, int divide, int refactor, double min, double max) throws IOException {
		 clsJavaVariableChunk vc = new clsJavaVariableChunk();
		 String input = vc.getVariableChunkProfile(strPath,power,mod,divide,refactor,min,max);
		 return input;
	 }
}
