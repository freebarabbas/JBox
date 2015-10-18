package clsCityHashCalc;

import java.util.List;

import clsTypes.ChunkProcess;
import clsTypes.HashCalc;
import clsTypes.chunk;
import clsTypes.chunkType;


public class TestforCityHash {

	public static void main(String args[]) throws Exception {
		
		String path="C:\\Users\\chencyun\\Downloads\\com.good.gdgma.apk";
		System.out.println(HashCalc.GetFileCityHash(path)); 
		List<chunk> aaa=ChunkProcess.GetFixChunk(path, 4*1024);
		List<chunk> bbb=ChunkProcess.GetChunk(path,21,4*1024,chunkType.VAR);
		System.out.println(bbb);
		System.out.println(clsJavaVariableChunk.GetVariableChunks(path,21));
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
