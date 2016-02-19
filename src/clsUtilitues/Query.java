package clsUtilitues;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import clsTypes.*;
import clsCompExtract.ZipProcess;
//import clsCompExtract.ZipProcess;
import clsRESTConnector.*;

public class Query {
	//private List<String> m_syncfolders;
	//private String m_metafile;
	private String m_url;
	private String m_username;
	private String m_pwd;
	private ebProxy m_pxy;
	private String m_tkn;
	private String m_storageurl;
	private int m_initialtype;
	private String m_usercontainer;
	private String m_level;
	private String m_guid;
	
	
	public Query(String p_url,String p_username,String p_pwd,ebProxy p_pxy)
	{
		//m_syncfolders=p_syncfolders;
		//m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_initialtype=1;
	}
	
	public Query(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level)
	{
		//m_syncfolders=p_syncfolders;
		//m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_initialtype=1;
	}
	
	public Query(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_guid)
	{
		//m_syncfolders=p_syncfolders;
		//m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_guid=p_guid;
		m_initialtype=1;
	}
	
	private boolean GetToken()
	{
		try
		{
			RestResult rr = RestConnector.GetToken(m_url, m_username, m_pwd, m_pxy);
			if(rr.httpcode==HttpURLConnection.HTTP_UNAUTHORIZED)
			{
				Config.logger.debug("Invalid Username and Password");
				SyncStatus.SetStatus("Invalid Username and Password");
				return false;
			}
			else if(rr.result==true)
			{
				Config.logger.debug("Get token:"+rr.token);
				Config.logger.debug("Get storageurl:"+rr.storageurl);
				m_tkn=rr.token;
				m_storageurl=rr.storageurl;
				return true;
			}
			else
			{
				SyncStatus.SetStatus("Cannot connect to the server.");
				return false;
			}
				
		}
		catch(Exception e)
		{
			Config.logger.fatal("Error to get token:"+e.getMessage());
			return false;
		}
	}
	
	private Set<String> GetCurrentChunk()
	{
		Set<String> hs = new HashSet<String>();
		try
		{
			RestResult rr=RestConnector.GetContainer(m_tkn, m_usercontainer, m_pxy);
			if(rr.result && rr.data!=null)
			{
				String tmp=new String(rr.data);
				String[] lines = tmp.split("\r\n|\n|\r");
				for(int i=0;i<lines.length;i++)
					if(lines[i].startsWith("c")) // && !lines[i].endsWith("_d"))
						hs.add(lines[i]);
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal("Error to get currecnt chunk list:"+e.getMessage());
			hs.clear();
		}		
		return hs;
	}
	
	private Set<String> GetBackupChunk()
	{
		Set<String> bkhs = new HashSet<String>();
		try
		{
			RestResult rr=RestConnector.GetContainer(m_tkn, m_usercontainer, m_pxy);
			if(rr.result && rr.data!=null)
			{
				String tmp=new String(rr.data);
				String[] lines = tmp.split("\r\n|\n|\r");
				for(int i=0;i<lines.length;i++)
					if(lines[i].startsWith("backup/c")) // && !lines[i].endsWith("_d"))
						bkhs.add(lines[i]);
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal("Error to get currecnt chunk list:"+e.getMessage());
			bkhs.clear();
		}		
		return bkhs;
	}
	
	public  void StartQuery() throws Exception
	{
		
		SyncStatus.SetStatus("Connecting to server");
		if(m_initialtype==1)
		{
			Config.logger.debug("Receiving token from server");
			if(GetToken()==false)
				return;
			int dotIndex=m_username.lastIndexOf(':');
	        if(dotIndex>=0)
	        	m_usercontainer=m_storageurl+"/"+m_username.substring(dotIndex+1);
	        else
	        	m_usercontainer=m_storageurl+"/"+m_username;
		}
		
		RestResult rr=null;
		Set<String> gcc=null;
		Set<String> gbc=null;
		//GetToken==true;
        while (true)
        {

        	if (m_level.equalsIgnoreCase("f")){
	            SyncStatus.SetStatus("Getting user information, file metadata from server");
	            rr=RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE", m_pxy);
	            byte[] remotebin=null;
	            if(rr.httpcode==HttpURLConnection.HTTP_NOT_FOUND)
	            {
	            	//RestConnector.PutContainer(m_tkn, m_usercontainer, m_pxy);
	            	SyncStatus.SetStatus("Can't find user information, file metadata from server");
	            }
	            else
	            	remotebin=rr.data;
	            
	            if (remotebin == null)
	            {                    
	                Config.logger.debug("NO file level metadata file in server at this time.");               
	            }
	            else
	            {                  
	                userMetaData tmpumd=new userMetaData(remotebin);
	                Config.logger.debug(tmpumd.ConvertToHTML("Getting remote file metadata snapshot"));
	                System.out.println("|File Directory and Name|"+"\t"+"|File GUID|"+"\t"+"|File Content Hash|"+"\t"+"|Create Time|"+"\t"+"|Update Time|"+"\t"+"|Size(Bytes)|");
	                Iterator<fileInfo> it = tmpumd.filelist.iterator();
	                while(it.hasNext())
	                {
	                	fileInfo tmp=it.next();
	                	if (tmp.type==0) {
		                	String CreateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.dt);
		                	String UpdateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.lastaction);
		                	System.out.println(tmp.filename.toString()+"\t"+tmp.guid.toString()+"\t"+tmp.filehash.toString()+"\t"+CreateTime+"\t"+UpdateTime+"\t"+tmp.bytelength);
	                	}
	                }                  
	                //Config.logger.debug(lastlocal.ConvertToHTML("Merged with remote metafile"));
	            }
        	}if (m_level.equalsIgnoreCase("c")){
        		
        		String strFileName="";
	            SyncStatus.SetStatus("Getting user information, file metadata from server");
	            rr=RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE", m_pxy);
	            byte[] remotebin=null;
	            if(rr.httpcode==HttpURLConnection.HTTP_NOT_FOUND)
	            {
	            	//RestConnector.PutContainer(m_tkn, m_usercontainer, m_pxy);
	            	SyncStatus.SetStatus("Can't find user information, file metadata from server");
	            }
	            else
	            	remotebin=rr.data;
	            
	            if (remotebin == null)
	            {                    
	                Config.logger.debug("NO file level metadata file in server at this time.");               
	            }
	            else
	            {                  
	                userMetaData tmpumd=new userMetaData(remotebin);
	                Config.logger.debug(tmpumd.ConvertToHTML("Getting remote file metadata snapshot"));
	                System.out.println("|File Directory and Name|"+"\t"+"|File GUID|"+"\t"+"|File Content Hash|"+"\t"+"|Create Time|"+"\t"+"|Update Time|"+"\t"+"|Size(Bytes)|");
	                Iterator<fileInfo> it = tmpumd.filelist.iterator();
	                while(it.hasNext())
	                {
	                	fileInfo tmp=it.next();
	                	if (tmp.type==0 && tmp.guid.equalsIgnoreCase(m_guid)) {
		                	String CreateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.dt);
		                	String UpdateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.lastaction);
		                	strFileName=tmp.filename.toString();
		                	System.out.println(tmp.filename.toString()+"\t"+tmp.guid.toString()+"\t"+tmp.filehash.toString()+"\t"+CreateTime+"\t"+UpdateTime+"\t"+tmp.bytelength);
	                	}
	                } 
	            }
        		
        		if (!m_guid.equalsIgnoreCase("")){
        			int downloadsize=0;
        			
                	rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+m_guid, m_pxy);
                	byte[] filedata = rr.data;
                    fileMetadataWithVersion fmd = new fileMetadataWithVersion(filedata);
                    Collections.sort(fmd.data);
                    int lastversion=fmd.data.size();
                    byte[] realdata = new byte[(int) fmd.data.get(lastversion-1).byteslength];
                    fmd.data.get(lastversion-1).data.size();
                    long dsize = 0;
                    Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
                    
                    for (chunk c : fmd.data.get(lastversion-1).data)
                    {
                        if (ht.get(c.hashvalue)!=null)
                        {
                        	System.arraycopy((byte[])ht.get(c.hashvalue), 0, realdata, (int)c.start, ((byte[])ht.get(c.hashvalue)).length);
                        }
                        else
                        {
                            byte[]  temp=RestConnector.GetContainer(m_tkn, m_usercontainer+"/c"+ Integer.toString(c.flag) +c.hashvalue, m_pxy).data;
                        	downloadsize +=temp.length;
                            if( (c.flag & 1) == 1) //compressed chunk
                            {
                            	temp=ZipProcess.unzip(temp);
                            }
                            ht.put(c.hashvalue, temp.clone());
                            System.arraycopy(temp, 0, realdata, (int)c.start, temp.length);
                            
                        }
                        dsize =dsize + c.end - c.start + 1;
                    }
                    ht.clear();
                    FileOutputStream out = new FileOutputStream(strFileName);
                	out.write(realdata);
                	out.close();
                	Config.logger.info("Downloaded " + strFileName + " with Size(Byte) " + downloadsize);
                	System.out.println("Downloaded " + strFileName + " with Size(Byte) " + downloadsize);
        			/*
	        		SyncStatus.SetStatus("Getting file information, chunk metadata from server");
	 	            rr=RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE/f" + m_name, m_pxy);
	 	            byte[] remotechunkbin=null;
	 	            if(rr.httpcode==HttpURLConnection.HTTP_NOT_FOUND)
	 	            {
	 	            	//RestConnector.PutContainer(m_tkn, m_usercontainer, m_pxy);
	 	            	SyncStatus.SetStatus("Can't find chunk information, chunk metadata from server");
	 	            }
	 	            else
	 	            	remotechunkbin=rr.data;
	 	            
	 	            if (remotechunkbin == null)
	 	            {                    
	 	                Config.logger.debug("NO chunk level metadata file in server at this time.");               
	 	            }
	 	            else
	 	            {                  
	 	                userMetaData tmpfmd=new userMetaData(remotechunkbin);
	 	                Config.logger.debug(tmpfmd.ConvertToHTML("Getting remote chunk metadata snapshot"));
	 	                System.out.println("|File Directory and Name|"+"\t"+"|File GUID|"+"\t"+"|File Content Hash|"+"\t"+"|Create Time|"+"\t"+"|Update Time|"+"\t"+"|Size(Bytes)|");
	 	                Iterator<fileInfo> itc = tmpfmd.filelist.iterator();
	 	                while(itc.hasNext())
	 	                {
	 	                	fileInfo tmpc=itc.next();
	 	                	if (tmpc.type==0) {
	 		                	String CreateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmpc.dt);
	 		                	String UpdateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmpc.lastaction);
	 		                	System.out.println(tmpc.filename.toString()+"\t"+tmpc.guid.toString()+"\t"+tmpc.filehash.toString()+"\t"+CreateTime+"\t"+UpdateTime+"\t"+tmpc.bytelength);
	 	                	}
	 	                }     
	 	            }
	 	            */
        		}else{
        			System.out.println("missing file guid !!!");
        		}
            	SyncStatus.SetStatus("Getting the chunk list from server");
        		if(gcc != null) 
        			gcc.clear();
        		gcc=GetCurrentChunk(); //get object list under user container from swift
        		
        		SyncStatus.SetStatus("Getting the backup chunk list from server");
        		if(gbc != null) 
        			gbc.clear();
        		gbc=GetBackupChunk(); //get object list under user container from swift
        		
            }else{
            	System.out.println("missing level !!! < f: file level or c: chunk level >");
            }
        }
		
	}

}

