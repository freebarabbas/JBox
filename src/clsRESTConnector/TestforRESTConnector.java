package clsRESTConnector;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

//import clsTypes.Config;


//import JBox.RestConnector;
//import JBox.ebProxy;
//import java.nio.file.attribute.FileTime;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.TimeUnit;

/*
GetToken
1.PutContainer
2.GetContainer or 7.GetContainner(File)
3.DeleteContainer
4.PutFile
5.CopyFile
6.DeleteFile

Scenario:
put container JOHNNY
get container UUUU

load file into dis
put dis under container JOHNNY
copy file from JOHNNY to UUUU

delete file JOB.txt under container JOHNNY
delete container JOHNNY

*/



public class TestforRESTConnector {
	
	private static boolean checkUSERMETAFILEETag(String strtoken, String strstorageurl) throws Exception {
	// TODO Auto-generated method stub
		String filename = "/tmp/USERMETAFILE";
		String strETag = "";
		ebProxy pxy=new ebProxy();
		RestResult rr = RestConnector.GetETag(strtoken, strstorageurl + "/dedup/USERMETAFILE", pxy);
		if(rr.result==true)
		{
			//Config.logger.debug("Get ETag:"+rr.msg);
			strETag=rr.msg.toUpperCase();
		         
		    MessageDigest md = MessageDigest.getInstance("MD5");
		    md.update(Files.readAllBytes(Paths.get(filename)));
		    byte[] digest = md.digest();
		    String strMD5sum = DatatypeConverter
		      .printHexBinary(digest).toUpperCase();
		         
		    if(strMD5sum.equals(strETag)){return true;}else{return false;}
		}
		else{return true;}
    }

	//@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
		calendar.add(Calendar.SECOND, 60);
		//long l = (calendar.getTimeInMillis() / 1000L);
	    System.out.println(String.valueOf((calendar.getTimeInMillis() / 1000L)));	
	
		
		
		
		//Get the jvm heap size.
        long heapSize = Runtime.getRuntime().totalMemory();

        //Print the jvm heap size.
        System.out.println("Heap Size = " + heapSize);
		
		//GetToken
		System.out.println("0. GetToken");
		//ebProxy pxy=new ebProxy();
		//ebProxy pxy=new ebProxy("web-proxy.corp.hp.com", 8080, "", "");
		ebProxy pxy=new ebProxy();
		RestResult rr = RestConnector.GetToken("https://cloud.swiftstack.com/auth/v1.0", "dedup", "dedup", pxy);
		//RestResult rr=RestConnector.GetToken("https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/", "10846130789747:johnny.wang2@hp.com", "Johnny634917", pxy);
		System.out.println(rr.token);
		System.out.println(rr.storageurl);
	
		
		RestResult rpost = RestConnector.UpdateObjectRefCount(rr.token, rr.storageurl+"/GenTestNew", "fdad9007ba25f4ca0823cbdaa080a757b", "1505512620", pxy);
		System.out.println(rpost.httpcode);
		System.out.println(checkUSERMETAFILEETag(rr.token, rr.storageurl));
		
		
		
		RestResult rraoc = RestConnector.AddObjectRefCount(rr.token, rr.storageurl+"/GenDB48DaysR2", "c1038df42702af269099857b5539db99dd", pxy);
		System.out.println(rraoc.httpcode);
		//RestResult rrcontainer=RestConnector.GetContainer(rr.token, rr.storageurl, pxy);
		//String aaaaaa=new String(rrcontainer.data);
		//System.out.println(aaaaaa);
		
		//GetObjectContent
		System.out.println("0.GetObjectContent");
		RestResult rr23=RestConnector.GetObjectContent(rr.token, rr.storageurl+"/jb", "fc2eebfa850a44c64928dfeba09754cda", "1-3650", pxy);
		System.out.println(new String(rr23.data, "UTF-8"));
		System.out.println(rr23.result);
		System.out.println(rr23.httpcode);
		
		//GetContainer
		System.out.println("7.GetContainner(File)");
		RestResult rr24=RestConnector.GetContainer(rr.token, rr.storageurl+"/jb/fc2eebfa850a44c64928dfeba09754cda", pxy);
		System.out.println(rr24.result);
		System.out.println(rr24.httpcode);
		System.out.println("--------------------contain Start------------------");
		System.out.println(new String(rr24.data, "UTF-8"));
		System.out.println("--------------------contain End--------------------");
		
		
		
		//PutContainer
		System.out.println("1.PutContainer");
		RestResult rr1=RestConnector.PutContainer(rr.token, rr.storageurl+"/JOHNNY", pxy);
		System.out.println(rr1.result);
		System.out.println(rr1.httpcode);
		
		//GetContainer
		System.out.println("2.Get Obj from Container");
		RestResult rr2=RestConnector.GetContainer(rr.token, rr.storageurl+"", pxy);
		System.out.println(rr2.result);
		System.out.println(rr2.httpcode);
		System.out.println("--------------------contain Start------------------");
		System.out.println(new String(rr2.data, "UTF-8"));
		System.out.println("--------------------contain End--------------------");
		

		//Load File into datainputstream
		File file = new File("/home/ubuntu/JBox_Backup/JBox_Note");
		//System.out.println(file.listFiles());
		byte[] fileData = new byte[(int) file.length()];
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		dis.readFully(fileData);
		dis.close();
		
		//put DataInputStream into container
		System.out.println("4.PutFile");
		RestResult rr4=RestConnector.PutFile(rr.token, rr.storageurl+"/JOHNNY","JBOX.txt",fileData, pxy);
		System.out.println(rr4.result);
		System.out.println(rr4.httpcode);
		
		//copy file
		System.out.println("5.CopyFile");
		RestResult rr5=RestConnector.CopyFile(rr.token, "/JOHNNY/JBOX.txt",rr.storageurl+"/JOHNNY/JBOX_cp.txt",pxy);
		System.out.println(rr5.result);
		System.out.println(rr5.httpcode);
		 
		//copy file
		System.out.println("5.CopyFile");
		RestResult rr15=RestConnector.CopyFile(rr.token,"/var/c139482d0736b5323fcc4b3b85b4e73452",rr.storageurl+"/var/c139482d0736b5323fcc4b3b85b4e73452_d",pxy);
		System.out.println(rr15.result);
		System.out.println(rr15.httpcode);
		
		//GetFile
		//GetContainer
		System.out.println("7.GetContainner(File)");
		RestResult rr7=RestConnector.GetContainer(rr.token, rr.storageurl+"/JOHNNY/JBOX.txt", pxy);
		System.out.println(rr7.result);
		System.out.println(rr7.httpcode);
		System.out.println("--------------------contain Start------------------");
		System.out.println(new String(rr7.data, "UTF-8"));
		System.out.println("--------------------contain End--------------------");
		
		//DeleteFile
		System.out.println("6.DeleteFile");
		RestResult rr6=RestConnector.DeleteFile(rr.token, rr.storageurl+"/JOHNNY","JBOX.txt" ,pxy);
		System.out.println(rr6.result);
		System.out.println(rr6.httpcode);
		
		//DeleteFile
		System.out.println("7.DeleteFile another copy file");
		RestResult rr8=RestConnector.DeleteFile(rr.token, rr.storageurl+"/JOHNNY","JBOX_cp.txt" ,pxy);
		System.out.println(rr8.result);
		System.out.println(rr8.httpcode);		
		
		//DeleteContainer PS: Container have to be empty
		System.out.println("3.DeleteContainer");
		RestResult rr3=RestConnector.DeleteContainer(rr.token, rr.storageurl+"/varc1d073f1a20678d56d0b35cee8544dfeb6", pxy);
		System.out.println(rr3.result);
		System.out.println(rr3.httpcode);

		//DeleteContainer PS: Container have to be empty
		System.out.println("3.DeleteContainer");
		RestResult rr13=RestConnector.DeleteContainer(rr.token, rr.storageurl+"/JOHNNY", pxy);
		System.out.println(rr13.result);
		System.out.println(rr13.httpcode);
		
		return;
	}

}