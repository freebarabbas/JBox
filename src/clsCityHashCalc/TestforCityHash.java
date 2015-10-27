package clsCityHashCalc;

import java.util.List;

import clsTypes.ChunkProcess;
import clsTypes.HashCalc;
import clsTypes.chunk;
import clsTypes.chunkType;


public class TestforCityHash {

	public static void main(String args[]) throws Exception {
		
		String path="//home//ubuntu//Downloads//test.tar.gz";
		System.out.println(HashCalc.GetFileCityHash(path)); 
		List<chunk> aaa=ChunkProcess.GetChunk(path,0,0,chunkType.FIX);
		System.out.println(aaa);
		List<chunk> bbb=ChunkProcess.GetChunk(path,0,0,chunkType.VAR);
		System.out.println(bbb);
		List<chunk> ccc=ChunkProcess.GetChunk(path,0,0,chunkType.NO);
		System.out.println(ccc);		
		System.out.println(clsJavaVariableChunk.GetVariableChunks(path,0,1));		
		System.out.println(clsJavaVariableChunk.GetVariableChunks(path,0,2));
		return;
		
		//System.out.println("Hello World");
		/* File file = new File("c:\\JBox\\JBox.txt");
		 byte[] fileData = new byte[(int) file.length()];
		 DataInputStream dis = new DataInputStream(new FileInputStream(file));
		 dis.readFully(fileData);
		 dis.close();
		 
		 long[] aaa=CityHash.cityHash128(fileData, 0, fileData.length);
		 for(int i=0;i<aaa.length;i++)
			 System.out.println(aaa[i]); 
		 
		 /*Johnny Add for get Variable Chunk metadata table*/
		 //String strPath = "C:\\JBox\\STS.zip";
		 //System.out.println(clsJavaVariableChunk.GetVariableChunks(strPath));
	 }
}
