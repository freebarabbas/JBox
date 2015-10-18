package clsCompExtract;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ExtraFromZIP {
	public ExtraFromZIP(String strZIP) {
		
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile("c:\\JBox\\ZIP\\" + strZIP + ".zip");
			
			// Extracts all files to the path specified under the file name folder
			//zipFile.extractAll("c:\\JBox\\UNZIP\\" + strZIP.substring(0, strZIP.indexOf(".")));
			
			// Specify the file name which has to be extracted and the path to which
			// this file has to be extracted
			zipFile.extractFile(strZIP, "c:\\JBox\\UNZIP\\");
			
		} catch (ZipException e) {
			e.printStackTrace();
		}
		
	}
	
}
