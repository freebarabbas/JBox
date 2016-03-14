package pkgTypes;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Some small useful functions.
 */
public class SmallFunctions {
	
	/**
	 * Convert string to date.
	 *
	 * @param s the date string, like:2014-05-28 GMT+00:00
	 * @return the Date
	 * @throws Exception the exception
	 */
	public static Date String2Date(String s) throws Exception
	{
		String[] parts = s.split("\\+");
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return sdf.parse(parts[0]);
	}
	
	/**
	 * Convert Date to string.
	 *
	 * @param d the date
	 * @return the converted string, like:2014-05-28 GMT+00:00
	 * @throws Exception the exception
	 */
	public static String Date2String(Date d) throws Exception
	{
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return sdf.format(d)+"+00:00";
	}
	
	/**
	 * Generate a new  guid.
	 *
	 * @return the GUID, like 9b6adc73cfda4ece9aaedda1e398bf99
	 */
	public static String GenerateGUID()
	{
		 UUID uuid = UUID.randomUUID();  
	     String str = uuid.toString();  
	     String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
	     return temp;
	}
	
	/**
	 * Generate a dummy guid.
	 *
	 * @return the dummy guid:00000000000000000000000000000000
	 */
	public static String GetDummyGUID()
	{
		return String.format("%032d", 0);
	}
	
	@SuppressWarnings("resource")
	public static void copyFile(File sourceFile, File destFile) throws Exception {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;
	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();

	        // previous code: destination.transferFrom(source, 0, source.size());
	        // to avoid infinite loops, should be:
	        long count = 0;
	        long size = source.size();              
	        while((count += destination.transferFrom(source, count, size-count))<size);
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}

}
