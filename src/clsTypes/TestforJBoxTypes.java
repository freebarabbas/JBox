package clsTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/*
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import clsRESTConnector.RestConnector;
import clsUtilitues.SyncStatus;


import clsRESTConnector.RestConnector;
import clsRESTConnector.RestResult;
import clsRESTConnector.ebProxy;
import clsUtilitues.SyncStatus;
import clsUtilitues.sync;
*/

public class TestforJBoxTypes {
	public static List<fileInfo> filelist;
	//@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		/*
		System.out.println(String.format("%-30s",SmallFunctions.Date2String(new Date())));
		System.out.println("MD5"+HashCalc.GetMD5Hash("yeft2l34oi2u34oi234234234"));
		System.out.println(HashC.alc.GetFileMd5Hash("c:\\JBox\\JBOX.txt"));
		System.out.println(HashCalc.HmacSha1Sign("wer", "23423444"));
		System.out.println(HashCalc.GetCityHash("yeft2l34oi2u34oi234234234"));
		System.out.println(HashCalc.GetFileCityHash("c:\\JBox\\JBOX.txt"));
		//return ;
		*/
		
		List<String> aa=new ArrayList<String>();
		aa.add("c:\\Jbox");
		userMetaData umd=userMetaData.GenerateLatestFilesStructure(aa);
		
		System.out.println(umd.filelist.size());
		
		dbop.InitConnection("/tmp/test.db");
		dbop.InsertUserMetaData(new Date(), umd.filelist);
		System.out.println(dbop.GetDBVersion());
		
		List<fileInfo> fi=dbop.GetUserMetaData();
		System.out.println(fi.size());
		//return;
		
		/*
		FileTime ft = FileTime.from(1600, TimeUnit.DAYS);
		System.out.println(ft.toString());
		System.out.println(ft.toMillis());
		
		Date dt=new Date(ft.toMillis());
		System.out.println(dt.toGMTString());
		System.out.println(String.format("%032d", 0));
		//return;
		
		//ebProxy pxy=new ebProxy();
		ebProxy pxy=new ebProxy("web-proxy.corp.hp.com", 8080, "", "");
		RestResult rr=RestConnector.GetToken("https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/", "10846130789747:johnny.wang2@hp.com", "Johnny634917", pxy);
		System.out.println(rr.token);
		System.out.println(rr.storageurl);
		
		RestResult rr1=RestConnector.PutContainer(rr.token, rr.storageurl+"/UUUU", pxy);
		System.out.println(rr1.result);
		System.out.println(rr1.httpcode);
		
		RestResult rr2=RestConnector.GetContainer(rr.token, rr.storageurl+"/USERMETAFILE", pxy);
		System.out.println(rr2.result);
		System.out.println(rr2.httpcode);
		System.out.println("--------------------contain Start------------------");
		System.out.println(new String(rr2.data, "UTF-8"));
		System.out.println("--------------------contain End--------------------");
		
		RestResult rr3=RestConnector.DeleteContainer(rr.token, rr.storageurl+"/UUUU", pxy);
		System.out.println(rr3.result);
		System.out.println(rr3.httpcode);
		
		
		File file = new File("c:\\JBox\\JBOX.txt");
		byte[] fileData = new byte[(int) file.length()];
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		dis.readFully(fileData);
		dis.close();
		RestResult rr4=RestConnector.PutFile(rr.token, rr.storageurl+"/UUUU","JBOX.txt",fileData, pxy);
		System.out.println(rr4.result);
		System.out.println(rr4.httpcode);
		 
		RestResult rr6=RestConnector.CopyFile(rr.token, "/UUUU/JBOX.txt",rr.storageurl+"/UUUU/JBOX_cp.txt",pxy);
		System.out.println(rr6.result);
		System.out.println(rr6.httpcode);
		 
		RestResult rr5=RestConnector.DeleteFile(rr.token, rr.storageurl+"/UUUU","JBOX.txt" ,pxy);
		System.out.println(rr5.result);
		System.out.println(rr5.httpcode);
		*/

		if(!Config.InitLogger())
		{
			System.out.println("Cannot start test program");
			return;
		}
		
		try{
			System.out.println("Start the Read MetaData");

			//initial configuration to config object
			Config.InitConfig(args);

        	//userMetaData local = null;
            File localmetafile=new File(Config.usermetafile);
            if (localmetafile.exists())
            {
            	userMetaData local = new userMetaData(Config.usermetafile);
                //Config.logger.debug(local.ConvertToHTML("Last snapshot"));
            	//filelist.addAll(local.filelist.i);
	        	//Collections.sort(filelist);      	
	        	Iterator<fileInfo> it = local.filelist.iterator();
	        	//fileInfo pre=null;
	        	while (it.hasNext()) 
	            {
	        		fileInfo fi1=it.next();
	        		System.out.println(fi1.bytelength);
	        		//System.out.println(fi.dbid);
	        		System.out.println(fi1.filehash);
	        		System.out.println(fi1.filename);
	        		System.out.println(fi1.guid);
	        		//System.out.println(fi.owner);
	        		System.out.println(fi1.parentguid);
	        		System.out.println(fi1.status); //0 sync done, 1 syncing
	        		System.out.println(fi1.type);
	        		System.out.println(fi1.versionflag);
	        		System.out.println(fi1.dt);
	        		//System.out.println(fi.fop);	  
	        		System.out.println(fi1.lastaction);	  
	        		//System.out.println(fi.);	   	        		

	            }
	        
	        }
		}
		catch(Exception e)
		{
			Config.logger.fatal(e.getMessage());
			e.printStackTrace();
		}
		
		//fileMetadata localfmd = fileMetadata.GetMetadata("c:\\JBoxLog\\10846130789747_JBOX_hp_com", chunksize, ct)GetMetadata(fi.filename, Config.fixedchunksize,Config.ct);
		//fileMetadata fmd=new fileMetadata("c:\\JBox\\JBOX.txt");
		//System.out.println(localfmd.byteslength);
		//System.out.println(localfmd.ConvertToString());
		//fmd.WriteToDisk("c:\\JBox\\JBOX_1.txt");
		
		//return;
		
	}

}
/*package JBoxTest;

import static org.junit.Assert.*;

import org.junit.AfterClass;
//import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
//import org.junit.After;

import JBox.SmallFunctions;
import JBox.dbop;

public class dbopTest {

	@BeforeClass
	public static void setUp() throws Exception {
		dbop.InitConnection("C:\\JBox\\userdata.db");
	}

	@Test
	public void testGetDBVersion() throws Exception {
		assertEquals(1, dbop.GetDBVersion());
	}
	
	@Test
	public void testGetGuidByFileName() throws Exception {
		assertEquals("405d3e0e7fbf4a2ab879e3f95759f3d0", dbop.GetGuidByFileName("c:\\JBox\\JBOX.txt"));
	}
	
	@Test
	public void testClearUserMetaData() throws Exception {
		assertEquals(true, dbop.ClearUserMetaData());
	}
	
	@Test
	public void testGetUserMetaDataTS() throws Exception {
		assertEquals(SmallFunctions.String2Date("2014-06-03 07:06:46+00:00"), dbop.GetUserMetaDataTS());
	}
	
	@Test
	public void testInsertFileStatus() throws Exception {
		assertEquals(true, dbop.InsertFileStatus("12322", 456));
	}
	
	/*@Test
	public void testClearFileStatus() throws Exception {
		assertEquals(true, dbop.ClearFileStatus());
	}
	
	@Test
	public void testGetFileStatus() throws Exception {
		assertEquals(21, dbop.GetFileStatus("c:\\JBox\\JBOX.txt"));
	}
	
	@AfterClass
	public static void Clear() throws Exception {
		dbop.CloseConnection();
	}

}
*/