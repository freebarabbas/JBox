package pkgCityHashCalc;

import java.util.List;

import pkgTypes.ChunkProcess;
import pkgTypes.HashCalc;
import pkgTypes.chunk;
import pkgTypes.chunkType;


public class TestforCityHash {

	public static void main(String args[]) throws Exception {
		
		String path="//home//ubuntu//Downloads//test.tar.gz";
		System.out.println(HashCalc.GetFileCityHash(path)); 
		List<chunk> aaa=ChunkProcess.GetChunk(path,0,8,0,0,chunkType.FIX);
		System.out.println(aaa);
		List<chunk> bbb=ChunkProcess.GetChunk(path,0,16,0,0,chunkType.VAR);
		System.out.println(bbb);
		List<chunk> ccc=ChunkProcess.GetChunk(path,0,64,0,0,chunkType.NO);
		System.out.println(ccc);		
		System.out.println(clsJavaVariableChunk.GetVariableChunks(path,0,1,32,0));		
		System.out.println(clsJavaVariableChunk.GetVariableChunks(path,22,2,8,0));
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
