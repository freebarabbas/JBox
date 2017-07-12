package clsTypes;

import java.io.FileWriter;

public class clsExperiment {
    public static boolean ExperimentDump(String strFileName, long lngtime, long lnguploadsize, long lngrawsize, long lngtotalsize,  int intmetadata){
    	String strDumpFile = strFileName.substring(strFileName.length()-3);
    	try {
    		FileWriter fw = new FileWriter("/tmp/"+strDumpFile+".txt", true);
    		fw.write(strFileName +"\t"+ lngtime + "\t" + lnguploadsize + "\t" + lngrawsize + "\t" + lngtotalsize + "\t" + intmetadata + System.getProperty("line.separator"));
    		fw.close();
    		return true;
    	}catch(Exception e){System.out.println(e);return false;}
    	//System.out.println("Experiment Dump Success!");
    	//return true;
    }
    
    public static boolean ExperimentMetaDataDump(String strFileName, long lngtime, String strMetaData){
    	String strDumpFile = strFileName.substring(strFileName.length()-3);
    	try {
    		FileWriter fw = new FileWriter("/tmp/"+strDumpFile+".meta", true);
    		fw.write(lngtime + System.getProperty("line.separator"));
    		fw.write(strMetaData + System.getProperty("line.separator"));
    		fw.close();
    		return true;
    	}catch(Exception e){System.out.println(e);return false;}
    	//System.out.println("Experiment Dump Success!");
    	//return true;
    }
    
    public static boolean ExperimentDcountDump(String strFileName, long lngtime, String strMetaData, long lnguploadsize, long lngrawsize, long lngtotalsize,String strPercentage){
    	String strDumpFile = strFileName.substring(strFileName.length()-3);
    	try {
    		FileWriter fw = new FileWriter("/tmp/"+strDumpFile+".dcount", true);
    		fw.write(strFileName +"\t"+ lngtime + "\t" + strMetaData + "\t" + lnguploadsize + "\t" + lngrawsize + "\t" + lngtotalsize + "\t" + strPercentage + System.getProperty("line.separator"));
    		fw.close();
    		return true;
    	}catch(Exception e){System.out.println(e);return false;}
    	//System.out.println("Experiment Dump Success!");
    	//return true;
    }
}
