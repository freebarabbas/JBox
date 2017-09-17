package clsTypes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class clsProperties {
	String result = "";
	InputStream inputStream;
 
	String authurl = "";
	String username = "";
	String password = "";
	String run = "";
	String type = "";
	String divider = "";
	String refactor = "";
	String min = "";
	String max = "";
	String client = "";
	String synctime = "";
	String containername = "";
	
	//@SuppressWarnings("finally")
	@SuppressWarnings("finally")
	public boolean getPropValues() throws IOException {
 
		try {
			Properties prop = new Properties();
			//String propFileName = "/pkgHelloWorld/config.properties";
			//String basePath = getClass().getResource("/").getPath();
			//String absolute = getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm();
			String propFileName = "JBoxconfig.properties";
			
			//inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			inputStream = new FileInputStream("./JBoxconfig.properties");
			
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the same root path with JBox.jar");
			}

			if (prop.getProperty("syncfolders") != null && !prop.getProperty("syncfolders").isEmpty()){Config.setsyncfolder(prop.getProperty("syncfolders"));}
			if (prop.getProperty("containername") != null && !prop.getProperty("containername").isEmpty()){Config.setcontainername(prop.getProperty("containername"));}
			//if (prop.getProperty("usermetafile") != null && !prop.getProperty("usermetafile").isEmpty()){Config.setswiftusr(prop.getProperty("usermetafile"));}
			
			// get the property value and print it out
			if (prop.getProperty("authurl") != null && !prop.getProperty("authurl").isEmpty()){Config.setserverlogin(prop.getProperty("authurl"));}
			if (prop.getProperty("username") != null && !prop.getProperty("username").isEmpty()){Config.setswiftusr(prop.getProperty("username"));}
			if (prop.getProperty("password") != null && !prop.getProperty("password").isEmpty()){Config.setswiftpwd(prop.getProperty("password"));}
			if (prop.getProperty("type") != null && !prop.getProperty("type").isEmpty()){
				if (prop.getProperty("type").equals("var")) {Config.ct = chunkType.VAR;}
				else if (prop.getProperty("type").equals("fix")) {Config.ct = chunkType.FIX;}
				else if (prop.getProperty("type").equals("no")) {Config.ct = chunkType.NO;}
				else {Config.ct = chunkType.VAR;}
			}
			if (prop.getProperty("power") != null && !prop.getProperty("power").isEmpty()){Config.setswiftpwr(Integer.parseInt(prop.getProperty("power")));}
			if (prop.getProperty("divider") != null && !prop.getProperty("divider").isEmpty()){Config.setswiftdiv(Integer.parseInt(prop.getProperty("divider")));}
			if (prop.getProperty("refactor") != null && !prop.getProperty("refactor").isEmpty()){Config.setswiftrefactor(Integer.parseInt(prop.getProperty("refactor")));}
			if (prop.getProperty("min") != null && !prop.getProperty("min").isEmpty()){Config.setswiftmin(Double.parseDouble(prop.getProperty("min")));}
			if (prop.getProperty("max") != null && !prop.getProperty("max").isEmpty()){Config.setswiftmax(Double.parseDouble(prop.getProperty("max")));}
			if (prop.getProperty("synctime") != null && !prop.getProperty("synctime").isEmpty()){Config.setswiftsynctime(Long.parseLong(prop.getProperty("synctime")));}
			if (prop.getProperty("refcounter") != null && !prop.getProperty("refcounter").isEmpty()){Config.setswiftrefcounter(Integer.parseInt(prop.getProperty("refcounter")));}
			if (prop.getProperty("clientnum") != null && !prop.getProperty("clientnum").isEmpty()){Config.setswiftclientnum(Integer.parseInt(prop.getProperty("clientnum")));}
			if (prop.getProperty("runmode") != null && !prop.getProperty("runmode").isEmpty()){Config.setrunmode(Integer.parseInt(prop.getProperty("runmode")));}

			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
			return true;
		}
		//return result;
	}
}
