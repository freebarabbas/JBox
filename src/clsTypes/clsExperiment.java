package clsTypes;

import java.io.FileWriter;

public class clsExperiment {
    public static boolean ExperimentDump(String strFileName, long lngtime, int intuploadsize, long lngtotalsize, int intmetadata){
    	String strDumpFile = strFileName.substring(strFileName.length()-3);
    	try {
    		FileWriter fw = new FileWriter("/tmp/"+strDumpFile+".txt", true);
    		fw.write(strFileName +"\t"+ lngtime + "\t" + intuploadsize + "\t" + lngtotalsize + "\t" + intmetadata + System.getProperty("line.separator"));
    		fw.close();
    		return true;
    	}catch(Exception e){System.out.println(e);return false;}
    	//System.out.println("Experiment Dump Success!");
    	//return true;
    }
}
