package clsCityHashCalc;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class clsTestByteReadAll {
	/*
	public static void main(String args[]) throws Exception {
	//Path path = Paths.get("//tmp/JBox/CSV_ALL.csv");
	Path path = Paths.get("//home//johnny//Desktop//JBoxJar//JBox.jar");
	File f = new File(path.toString());
    long l = f.length();
    int n = (int)Math.ceil(l / Integer.MAX_VALUE);
    if (l > Integer.MAX_VALUE){
    	System.out.println("Too Large");
    	//int n = (int)Math.ceil(l / Integer.MAX_VALUE);
    	System.out.println(n);
    }
	//@SuppressWarnings("unused")
    byte [] data;
    for (int i=0;i<=n;i++){
		data = Files.readAllBytes(path);
    }
    long = bytesToLong(data);
	return;
	}
	*/
	
	private static byte[] getFileByteArray(String filepath, int dcount) throws IOException{
		RandomAccessFile aFile = new RandomAccessFile(filepath, "r");
        FileChannel inChannel = aFile.getChannel();
        long fileSize = inChannel.size();
        System.out.println(fileSize);
        ByteBuffer buffer = ByteBuffer.allocate(1*1024*1024*1024);
		byte[] filedata = new byte[1*1024*1024*1024];
		int buffercount = 1;
        while(inChannel.read(buffer) > 0)
        {
            System.out.println(buffercount);
        	buffer.flip();
            
            if (dcount == buffercount){
            	filedata = new byte[buffer.remaining()];
                int intsize = filedata.length;
                System.out.println(intsize);
            	buffer.clear();
            	break;
            }
            buffer.clear(); // do something with the data and clear/compact it.
            buffercount = buffercount + 1;
        }
        inChannel.close();
        aFile.close();
		return filedata;
	}

	public static void main(String[] args) throws IOException 
    {
		byte[] filedatareturn = getFileByteArray("//tmp/JBox/CSV_ALL.csv", 1);
		System.out.println("return byte array size: "+filedatareturn.length);
		System.gc();
		
		filedatareturn = getFileByteArray("//tmp/JBox/CSV_ALL.csv", 2);
		System.out.println("return byte array size: "+filedatareturn.length);
		System.gc();
		
		filedatareturn = getFileByteArray("//tmp/JBox/CSV_ALL.csv", 3);
		System.out.println("return byte array size: "+filedatareturn.length);
		System.gc();
		
		filedatareturn = getFileByteArray("//tmp/JBox/CSV_ALL.csv", 4);
		System.out.println("return byte array size: "+filedatareturn.length);
		System.gc();
		
		filedatareturn = getFileByteArray("//tmp/JBox/CSV_ALL.csv", 5);
		System.out.println("return byte array size: "+filedatareturn.length);
		System.gc();
		
		filedatareturn = getFileByteArray("//tmp/JBox/CSV_ALL.csv", 6);
		System.out.println("return byte array size: "+filedatareturn.length);
		System.gc();
    }
	/*
	public static void main(String[] args) throws IOException 
    {
        RandomAccessFile aFile = new RandomAccessFile
                ("//tmp/JBox/CSV_ALL.csv", "r");
        FileChannel inChannel = aFile.getChannel();
        long fileSize = inChannel.size();
        ByteBuffer buffer = ByteBuffer.allocate(1*1024*1024*1024);
        while(inChannel.read(buffer) > 0)
        {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            //long[] data = new byte[buffer.re]
            buffer.get(data);
            //for (int i = 0; i < buffer.limit(); i++)
            //{
            //    System.out.print((char) buffer.get());
            //}
            int intsize = data.length;
            System.out.println(intsize);
            buffer.clear(); // do something with the data and clear/compact it.
        }
        inChannel.close();
        aFile.close();
    }
    */
	/*
	 public static void main(String[] args) throws IOException 
	    {
	        RandomAccessFile aFile = new RandomAccessFile
	                ("//tmp/JBox/CSV_ALL.csv", "r");
	        FileChannel inChannel = aFile.getChannel();
	        MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size()/2);
	        //MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
	        buffer.load();  
	        for (int i = 0; i < buffer.limit(); i++)
	        {
	            //System.out.print((char) buffer.get());
	            
	            byte[] data = new byte[buffer.remaining()];
	            buffer.get(data);
	            //for (int i = 0; i < buffer.limit(); i++)
	            //{
	            //    System.out.print((char) buffer.get());
	            //}
	            int intsize = data.length;
	            System.out.println(intsize);
	        }
	        buffer.clear(); // do something with the data and clear/compact it.
	        inChannel.close();
	        aFile.close();
	    }
	    */
}
