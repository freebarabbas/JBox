package clsTypes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
//import java.io.InputStream;
//import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import clsCityHashCalc.CityHash;
import clsCityHashCalc.clsJavaVariableChunk;

/**
 * The Class HashCalc to calculate MD5 and CityHash code for byte array/String/File.
 */
public class HashCalc {
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	/**
	 * Byte array to hex string.
	 *
	 * @param b the byte array
	 * @return the  hex string
	 */
	public static String byteArrayToHexString(byte[] b)
	{
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}
	
	
	/**
	 * Gets the MD5 hash.
	 *
	 * @param input the input byte array
	 * @return  MD5 value
	 * @throws Exception the exception
	 */
	public static String GetMD5Hash(byte[] input) throws Exception
    {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(input);
		return byteArrayToHexString(thedigest);
    }
	
	/**
	 * Gets the MD5 hash.
	 *
	 * @param input the string
	 * @return MD5 value
	 * @throws Exception the exception
	 */
	public static String GetMD5Hash(String input) throws Exception
    {
		byte[] theinput=input.getBytes("UTF-8");
		return GetMD5Hash(theinput);
    }

	/**
	 * Gets the file MD5 hash.
	 *
	 * @param filename the filename
	 * @return MD5 value
	 * @throws Exception the exception
	 */
	public static String GetFileMd5Hash(String filename) throws Exception
	{
		File file = new File(filename);
		int BufferSize =100000000;
		byte[] buffer = new byte[BufferSize];
		FileInputStream fis=new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);		
		int readcount;
		MessageDigest md = MessageDigest.getInstance("MD5");
		while((readcount=dis.read(buffer,0,BufferSize))>0)
		{
			md.update(buffer, 0, readcount);
		}
		if(dis!=null)
			dis.close();
		if(fis!=null)
			fis.close();
		byte[] thedigest = md.digest();
		return byteArrayToHexString(thedigest);		
	}
	
	
	/**
	* Computes RFC 2104-compliant HMAC signature.
	* @param message
	* The message to be signed.
	* @param key
	* The signing key.
	* @return
	* The HMAC signature.
	 * @throws Exception the exception
	*/
	public static String HmacSha1Sign(String message, String key)  throws Exception
	{    
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
		return byteArrayToHexString(rawHmac);
		
    }
	
	/**
	 * Gets the city hash.
	 *
	 * @param input the input byte array
	 * @return the cityhash value
	 * @throws Exception the exception
	 */
	public static String GetCityHash(byte[] input) throws Exception
    {
		long[] tmp=CityHash.cityHash128(input, 0, input.length);		
		return Long.toHexString(tmp[0])+Long.toHexString(tmp[1]);
    }
	
	/**
	 * Gets the city hash.
	 *
	 * @param input the input string
	 * @return the cityhash string
	 * @throws Exception the exception
	 */
	public static String GetCityHash(String input) throws Exception
    {
		byte[] theinput=input.getBytes("UTF-8");
		return GetCityHash(theinput);
    }
	
	/**
	 * Gets the file city hash.
	 *
	 * @param filename the filename
	 * @return the cityhash string
	 * @throws Exception the exception
	 */
	public static String GetFileCityHash_Old(String filename) throws Exception
	{
		/*File file = new File(filename);
		long BufferSize =file.length();
		byte[] buffer = new byte[(int) BufferSize];
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		dis.readFully(buffer);
		dis.close();
		return GetCityHash(buffer);*/
		
		File f = new File(filename);
		FileInputStream fin = null;
		FileChannel ch = null;
		ByteArrayOutputStream out=null;
		try {
			fin = new FileInputStream(f);
			/*ch = fin.getChannel();
			int size = (int) ch.size();
			MappedByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, size);
			byte[] bytes = new byte[size];
			buf.get(bytes);
			return GetCityHash(bytes);*/
			out = new ByteArrayOutputStream((int)f.length());  
			byte[] cache = new byte[1048576];  
			for(int i = fin.read(cache);i != -1;i = fin.read(cache)){  
			  out.write(cache, 0, i);  
			}   
			return GetCityHash(out.toByteArray());
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (ch != null) {
					ch.close();
				}
				if (fin != null) {
					fin.close();
				}
				if(out!=null)
					out.close();
				
			} catch (Exception e) {
				throw e;
			}
		}
		
		/*Path path = Paths.get(filename);
		byte[] buf=Files.readAllBytes(path);
		return GetCityHash(buf);*/
		
		/*InputStream in = new FileInputStream(filename);
	    long length = new File(filename).length();

	    if (length > Integer.MAX_VALUE) {
	    	in.close();
	        throw new Exception("File is too large!");
	    }

	    byte[] bytes = new byte[(int) length];

	    int offset = 0;
	    int numRead = 0;

	    while (offset < bytes.length && (numRead = in.read(bytes, offset, bytes.length - offset)) >= 0) {
	        offset += numRead;
	    }

	    if (offset < bytes.length) {
	    	in.close();
	        throw new Exception("Could not completely read file " + filename);
	    }

	    in.close();
	    return GetCityHash(bytes);*/
		
	}	
	
	public static String GetFileCityHash(String filename) throws Exception
	{
		String ret=clsJavaVariableChunk.GetVariableChunks(filename,0,0,0,0);
		String[] lines=ret.split("\r\n|\n|\r");
		String[] tmp=lines[0].split("\\s+");
		return tmp[4];
	}
	
}
