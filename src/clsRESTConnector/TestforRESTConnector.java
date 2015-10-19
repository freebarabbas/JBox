package clsRESTConnector;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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

	//@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		//GetToken
		System.out.println("0. GetToken");
		//ebProxy pxy=new ebProxy();
		//ebProxy pxy=new ebProxy("web-proxy.corp.hp.com", 8080, "", "");
		ebProxy pxy=new ebProxy();
		RestResult rr = RestConnector.GetToken("http://svl12-csl-swift-ctl-001/auth/v1.0", "johnnywa", "Chianing2345", pxy);
		//RestResult rr=RestConnector.GetToken("https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/", "10846130789747:johnny.wang2@hp.com", "Johnny634917", pxy);
		System.out.println(rr.token);
		System.out.println(rr.storageurl);
		
		RestResult rrcontainer=RestConnector.GetContainer(rr.token, rr.storageurl, pxy);
		String aaaaaa=new String(rrcontainer.data);
		System.out.println(aaaaaa);
		//PutContainer
		System.out.println("1.PutContainer");
		RestResult rr1=RestConnector.PutContainer(rr.token, rr.storageurl+"/JOHNNY", pxy);
		System.out.println(rr1.result);
		System.out.println(rr1.httpcode);
		
		//GetContainer
		System.out.println("2.Get Obj from Container");
		RestResult rr2=RestConnector.GetContainer(rr.token, rr.storageurl+"/JOHNNY", pxy);
		System.out.println(rr2.result);
		System.out.println(rr2.httpcode);
		System.out.println("--------------------contain Start------------------");
		System.out.println(new String(rr2.data, "UTF-8"));
		System.out.println("--------------------contain End--------------------");
		

		//Load File into datainputstream
		File file = new File("//home//ubuntu//JBox//JBox_Note");
		System.out.println(file.listFiles());
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
		
		//DeleteContainer PS: Container have to be empty
		System.out.println("3.DeleteContainer");
		RestResult rr3=RestConnector.DeleteContainer(rr.token, rr.storageurl+"/JOHNNY", pxy);
		System.out.println(rr3.result);
		System.out.println(rr3.httpcode);

		return;
	}

}