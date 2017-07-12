package clsUtilitues;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import clsTypes.*;
import clsCompExtract.ZipProcess;
import clsRESTConnector.*;

public class Sync implements Runnable {
	private List<String> m_syncfolders;
	private String m_metafile;
	private String m_url;
	private String m_username;
	private String m_pwd;
	private ebProxy m_pxy;
	private String m_tkn;
	private String m_storageurl;
	private int m_initialtype;
	private String m_usercontainer;
	private int m_mod;
	//private double m_min;
	//private double m_max;
	private long m_synctime;
	private String m_containername;
	private static long l_buffer=1*1024*1024*1024;
	private static int l_worker=20;
	private boolean fthread = true;
	//private static long l_buffer=10*1024*1024;
	
	public Sync(List<String> p_syncfolders,String p_metafile, String p_url,String p_username,String p_pwd,ebProxy p_pxy,int p_mod, long p_synctime, String p_containername)
	{
		m_syncfolders=p_syncfolders;
		m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_mod =p_mod;
		m_initialtype=1;
		//m_min=p_min;
		//m_max=p_max;
		m_synctime=p_synctime;
		m_containername=p_containername;
	}
	
	public Sync(List<String> p_syncfolders,String p_metafile,String p_url,String p_storageurl,String p_username,String p_tkn,ebProxy p_pxy,int p_mod, long p_synctime, String p_containername)
	{
		m_syncfolders=p_syncfolders;
		m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_storageurl=p_storageurl;
		m_tkn=p_tkn;
		m_pxy=p_pxy;
		m_containername=p_containername;
		int dotIndex=p_username.lastIndexOf(':');
        if(dotIndex>=0){m_usercontainer=m_storageurl+"/"+p_username.substring(dotIndex+1);}
        else{
        	if(m_containername.isEmpty()){m_usercontainer=m_storageurl+"/"+p_username;}
        	else{m_usercontainer=m_storageurl+"/"+m_containername;}
        }
		m_initialtype=2;
		m_mod=p_mod;
		//m_min=p_min;
		//m_max=p_max;
		m_synctime=p_synctime;
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
		int counter=0;
		String strmarker="";
		Set<String> hs = new HashSet<String>();
		try
		{
			RestResult rr=RestConnector.GetContainer(m_tkn, m_usercontainer, m_pxy);
			if(rr.result && rr.data!=null)
			{
				String tmp=new String(rr.data);
				String[] lines = tmp.split("\r\n|\n|\r");
				for(int i=0;i<lines.length;i++){
					if(lines[i].startsWith("c")) // && !lines[i].endsWith("_d"))
						hs.add(lines[i]);
						strmarker = lines[i];
						counter++;
				}
			}
			while ( counter !=0 && ( counter % 10000 ) == 0){
				RestResult rrmore=RestConnector.GetContainer(m_tkn, m_usercontainer + "?marker=" + strmarker, m_pxy);
				if(rrmore.result && rrmore.data!=null)
				{
					String tmp=new String(rrmore.data);
					String[] lines = tmp.split("\r\n|\n|\r");
					for(int i=0;i<lines.length;i++){
						if(lines[i].startsWith("c")) // && !lines[i].endsWith("_d"))
							hs.add(lines[i]);
						    strmarker = lines[i];
							counter++;
					}
				}
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal("Error to get currecnt chunk list:"+e.getMessage());
			hs.clear();
		}		
		System.out.println(counter);
		return hs;
	}
	
	private Set<String> GetBackupChunk()
	{
		int counter=0;
		String strmarker="";
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
			while ( counter !=0 && ( counter % 10000 ) == 0){
				RestResult rrmore=RestConnector.GetContainer(m_tkn, m_usercontainer + "?marker=" + strmarker, m_pxy);
				if(rrmore.result && rrmore.data!=null)
				{
					String tmp=new String(rrmore.data);
					String[] lines = tmp.split("\r\n|\n|\r");
					for(int i=0;i<lines.length;i++){
						if(lines[i].startsWith("backup/c")) // && !lines[i].endsWith("_d"))
							bkhs.add(lines[i]);
						    strmarker = lines[i];
							counter++;
					}
				}
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal("Error to get currecnt chunk list:"+e.getMessage());
			bkhs.clear();
		}		
		System.out.println(counter);
		return bkhs;
	}
	
	private  boolean UpdateRemoteUserMetaFile(fileInfo fi)
    {
        try
        {
        	Config.logger.debug("Start to update user meta file for " + fi.filename);
            RestResult rr= RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE-L", m_pxy);
            byte[] lockbin=rr.data;
            while (lockbin != null)//have lock at this time
            {
            	Config.logger.debug("find lock when update user meta file for " + fi.filename);
            	String tmp=new String(lockbin);
            	Date lockdt=SmallFunctions.String2Date(tmp);
            	Calendar ca=Calendar.getInstance();
            	ca.setTime(lockdt);
            	ca.add(Calendar.MINUTE, 1);
                if (ca.getTime().after(lockdt))//exceeded 1 minute to be locked
                {
                	Config.logger.debug("Remove lock forcely to update user meta file for " + fi.filename);                       
                    break;
                }
				Thread.sleep(1000);
                rr= RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE-L", m_pxy);
                lockbin=rr.data;

            }
            Config.logger.debug("Add lock  to update user meta file for " + fi.filename); 
            RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE-L", SmallFunctions.Date2String(new Date()).getBytes(), m_pxy);
            rr = RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE", m_pxy);
            byte[] remotebin=rr.data;
            if (remotebin == null)
            {
                userMetaData umd = new userMetaData();
                umd.user = m_username;
                umd.filelist = new ArrayList<fileInfo>(); 
                fi.fop = FOP.NONE;
                umd.filelist.add(fi);
                RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);
            }
            else
            {
            	userMetaData umd = new userMetaData(remotebin); //remote meta data version
                umd.dt = new Date();
                if (fi.fop == FOP.UPLOAD || fi.fop==FOP.COPY) // new and copy will insert a new record in file level meta data
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	boolean found=false;
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next(); // for new and copy, as long as file name and hash is the same, which means it's copy
                		if(tmp.filename.compareToIgnoreCase(fi.filename)==0 && tmp.filehash.compareToIgnoreCase(fi.filehash)==0)
                		{
                			if(fi.dt.after(tmp.dt))//local is more latest
                			{
                				fi.fop=FOP.NONE;
                				tmp.copyfrom(fi);
                				RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);               						
                			}
                			found=true;
                			break;
                		}
                	}
                	if(found==false)//if no found in existing file level metadata, it must be new, then just insert a new record.
                	{
                		fi.fop=FOP.NONE;
                		umd.filelist.add(fi);
                		RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);
                	}
                }
                else if (fi.fop == FOP.REMOTE_NEED_OVERWRITE) //remote need to be overwrite when modify
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next(); //file name and file guid are the same but file hash might be not because content might be changed
                		if(tmp.filename.compareToIgnoreCase(fi.filename)==0)
                		{
                			if(fi.dt.after(tmp.dt))//local is more latest
                			{
                				fi.fop=FOP.NONE;
                				tmp.copyfrom(fi);
                				RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);               						
                			}
                			break;
                		}
                	}
                }
                else if (fi.fop == FOP.MOVE_FROM_REF) //remote need to be overwrite when move/rename
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next(); //file name and file guid are the same but file hash might be not because content might be changed
                		if(tmp.filename.compareToIgnoreCase(fi.filename)!=0 && tmp.guid.compareToIgnoreCase(fi.guid)==0 && tmp.filehash.compareToIgnoreCase(fi.filehash)==0)
                		{
            				fi.fop=FOP.NONE; // clean the flag
            				tmp.copyfrom(fi);
            				RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);               						
                			break;
                		}
                	}
                }//local has deleted = remote need to be deleted
                else if(fi.fop == FOP.LOCAL_HAS_DELETED || fi.fop == FOP.REMOTE_NEED_TOBE_DELETED || fi.fop == FOP.REMOTE_HAS_DELETED || fi.fop == FOP.LOCAL_NEED_TOBE_DELETED) 
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next(); //filename, fileguid and filehash are all the same
                		if(tmp.filename.compareToIgnoreCase(fi.filename)==0 && tmp.filehash.compareToIgnoreCase(fi.filehash)==0 && tmp.guid.compareToIgnoreCase(fi.guid)==0)
                		{
                			if (tmp.status > 1){
                				tmp.status = tmp.status -1;
                    			if(tmp.status == 1){//all the clients removed
    	                			umd.filelist.remove(tmp);
    	                			RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);              						
    	                			break;
                    			}else{
                    				RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);
                    				break;
                    			}
                			}else if (tmp.status == 0){
                				if (Config.clientnum == 1){
    	                			umd.filelist.remove(tmp);
    	                			RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);              						
    	                			break;
                				}
                				else{
                    				fi.status = Config.clientnum;
                    				fi.fop = FOP.NONE;
                    				tmp.copyfrom(fi);
                    				RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);
                    				break;
                				}
                			}
                		}
                	}
                }
            }
            RestConnector.DeleteFile(m_tkn, m_usercontainer , "USERMETAFILE-L", m_pxy);
            Config.logger.debug("Remove lock for update user meta file for " + fi.filename);
            Config.logger.debug("Done to update user meta file for " + fi.filename);
            return true;
        }
        catch (Exception e)
        {
        	Config.logger.error("Error to update user meta file for " + fi.filename+ ".Error:"+e.getMessage());
            return false;
        }
    }
	
	private  boolean UpdateRemoteUserMetaFile(fileInfo fito, fileInfo fifrom)
    {
        try
        {
        	Config.logger.debug("Start to update user meta file for " + fito.filename);
            RestResult rr= RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE-L", m_pxy);
            byte[] lockbin=rr.data;
            while (lockbin != null)//have lock at this time
            {
            	Config.logger.debug("find lock when update user meta file for " + fito.filename);
            	String tmp=new String(lockbin);
            	Date lockdt=SmallFunctions.String2Date(tmp);
            	Calendar ca=Calendar.getInstance();
            	ca.setTime(lockdt);
            	ca.add(Calendar.MINUTE, 1);
                if (ca.getTime().after(lockdt))//exceeded 1 minute to be locked
                {
                	Config.logger.debug("Remove lock forcely to update user meta file for " + fito.filename);                       
                    break;
                }
				Thread.sleep(1000);
                rr= RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE-L", m_pxy);
                lockbin=rr.data;

            }
            Config.logger.debug("Add lock  to update user meta file for " + fito.filename); 
            RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE-L", SmallFunctions.Date2String(new Date()).getBytes(), m_pxy);
            rr = RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE", m_pxy);
            byte[] remotebin=rr.data;
            if (remotebin == null)
            {
                userMetaData umd = new userMetaData();
                umd.user = m_username;
                umd.filelist = new ArrayList<fileInfo>(); 
                fito.fop = FOP.NONE;
                umd.filelist.add(fito);
                RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);
            }
            else
            {
            	userMetaData umd = new userMetaData(remotebin); //remote meta data version
                umd.dt = new Date();
                if (fito.fop == FOP.MOVE_FROM_REF) //remote need to be overwrite when move/rename
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next(); //file name and file guid are the same but file hash might be not because content might be changed
                		if(tmp.filename.compareToIgnoreCase(fifrom.filename)==0 && tmp.guid.compareToIgnoreCase(fifrom.guid)==0 && tmp.filehash.compareToIgnoreCase(fifrom.filehash)==0)
                		{
            				fito.fop=FOP.NONE; // clean the flag, only none will write local file level metadata
            				tmp.copyfrom(fito);
            				RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);               						
                			break;
                		}
                	}
                }
            }
            RestConnector.DeleteFile(m_tkn, m_usercontainer , "USERMETAFILE-L", m_pxy);
            Config.logger.debug("Remove lock for update user meta file for " + fito.filename);
            Config.logger.debug("Done to update user meta file for " + fito.filename);
            return true;
        }
        catch (Exception e)
        {
        	Config.logger.error("Error to update user meta file for " + fifrom.filename+ ".Error:"+e.getMessage());
            return false;
        }
    }
		
	private  boolean ReduceRefCounterAndPurgeFile(fileInfo fi)
    {
		  try
	        {
	        	Config.logger.debug("Start to Reduce ref counter in file level meta file for " + fi.filename);
	        	
                String srcguid = fi.guid;
                //delete file level metadata after 2 min
        		String objcount = String.valueOf((System.currentTimeMillis() / 1000L) + Config.containerpurgetime);
        		RestConnector.UpdateObjectRefCount(m_tkn, m_usercontainer, "f"+srcguid,objcount,m_pxy);
        		
        		if (Config.refcounter == 1) {
        			Config.logger.debug("sum deletion flag at file level for " + "f"+srcguid);
	                RestResult rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+srcguid, m_pxy);
	                byte[] filedata = rr.data;
	                //fileMetadataWithVersion fmds = new fileMetadataWithVersion(filedata);
	                fileMetadata fmds = new fileMetadata(filedata, true);
	                Collections.sort(fmds.data);
		        	
	                if(fi.fop == FOP.LOCAL_HAS_DELETED)
	                {
	                	 for(chunk c : fmds.data)
	                     {
	                     	//reduce ref counter or delete object in 1 min
	                		RestConnector.ReduceObjectRefCount(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, m_pxy);
	                        Config.logger.debug("reduce ref counter at object level for " + "c"+ Integer.toString(c.flag) + c.hashvalue);
	                     }  
	                	
	                	//Iterator<fileMetadata> it = fmds.data.iterator();
	                	//while(it.hasNext()){
	                	//	//fileInfo tmp=it.hashCode();
	                	//	RestConnector.ReduceObjectRefCount(m_tkn, m_usercontainer, Integer.toString(it.hashCode()), m_pxy);
	                    //   Config.logger.debug("reduce ref counter at object level for " + Integer.toString(it.hashCode()));
	                	//}
	                }
        		}

                Config.logger.debug("Done to delete file leve meta data or reduce ref counter at file level meta file for " + fi.filename);
                return true;
	        }
	        catch (Exception e)
	        {
	        	Config.logger.error("Error to reduce ref counter at file level meta file for" + fi.filename+ ".Error:"+e.getMessage());
	            return false;
	        }
    }
	
	
	public static byte[] GetFileByteArray(String filepath, int dcount) throws IOException{
		RandomAccessFile aFile = new RandomAccessFile(filepath, "r");
		/*
        FileChannel inChannel = aFile.getChannel();
        long fileSize = inChannel.size();
        System.out.println(fileSize);
        ByteBuffer buffer = ByteBuffer.allocate((int)l_buffer);
		byte[] filedata = new byte[(int)l_buffer];
		int buffercount = 1;
        while(inChannel.read(buffer) > 0)
        {
            System.out.println(buffercount);
        	buffer.flip();
            
            if (dcount == buffercount){
            	filedata = new byte[buffer.remaining()];
            	buffer.get(filedata);
                int intsize = filedata.length;
                System.out.println(intsize);
            	buffer.clear();
            	break;
            }
            buffer.clear(); // do something with the data and clear/compact it.
            buffercount = buffercount + 1;
        }
        inChannel.close();
        aFile.close();
		return filedata;
		*/
		
		//read startPosition ~ endPosition or end of file to byteArray[]
		long startPosition = (dcount-1) * l_buffer;
		//move filepointer to startPosition
		aFile.seek(startPosition);

        FileChannel inChannel = aFile.getChannel();

        long fileSize = inChannel.size();
        System.out.println(fileSize);
        ByteBuffer buffer = ByteBuffer.allocate((int)l_buffer);
		byte[] filedata = new byte[(int)l_buffer];

        if (inChannel.read(buffer) > 0)
        {
        	buffer.flip();
        	filedata = new byte[buffer.remaining()];
        	buffer.get(filedata);
            int intsize = filedata.length;
            System.out.println(intsize);
            buffer.clear(); 
        }
        inChannel.close();
        aFile.close();
        return filedata;
	}
	
	private  void StartSync() throws Exception
	{
		Config.logger.info("Starting sync process");
		SyncStatus.SetStatus("Connecting to server");
		if(m_initialtype==1)
		{
			Config.logger.debug("Receiving token from m_usernameserver");
			if(GetToken()==false)
				return;
			else
				Config.logger.debug("Got 1st token: " + m_tkn);
			int dotIndex=m_username.lastIndexOf(':');
	        if(dotIndex>=0)
	        	m_usercontainer=m_storageurl+"/"+m_username.substring(dotIndex+1);
	        else
	        	if(m_containername.isEmpty() && m_containername == null){m_usercontainer=m_storageurl+"/"+m_username;}
	        	else{m_usercontainer=m_storageurl+"/"+m_containername;}
		}
		
		RestResult rr=null;
		Set<String> gcc=null;
		Set<String> gbc=null;
		//Set<String> cccc=GetCurrentChunk();
        while (true)
        {
            try
            {
            	SyncStatus.SetStatus("Getting the chunk list from server");
        		if(gcc != null) 
        			gcc.clear();
        		gcc=GetCurrentChunk(); //get object list under user container from swift
        		
        		SyncStatus.SetStatus("Getting the backup chunk list from server");
        		if(gbc != null)
        			gbc.clear();
        		gbc=GetBackupChunk(); //get cold storage layer , backup chunk
        		//gbc=GetCurrentChunk();
        		
        		SyncStatus.SetStatus("Identitying the changes between current snapshot and local");
            	userMetaData local = null;
            	
                File localmetafile=new File(m_metafile);
                if (localmetafile.exists())
                {
                    local = new userMetaData(m_metafile);
                    Config.logger.debug(local.ConvertToHTML("Last snapshot"));
                }
                
                userMetaData lastlocal = new userMetaData();              

                lastlocal.GenerateFilesStructure(m_syncfolders);
                Config.logger.debug(lastlocal.ConvertToHTML("Current snapshot"));
                if (local != null)
                {
                    lastlocal.MergeWithLocal(local);
                    Config.logger.debug(lastlocal.ConvertToHTML("Merged with last snapshot"));                          	
                }

                boolean flgchanged = false; // default set flgchanged into false , don't delete any object
        		//if (Config.refcounter == 1) {
        		//	flgchanged = true;
        		//}
                

                

                SyncStatus.SetStatus("Getting user information, file metadata from server");
                rr=RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE", m_pxy);
                byte[] remotebin=null;
                if(rr.httpcode==HttpURLConnection.HTTP_NOT_FOUND)
                {
                	RestConnector.PutContainer(m_tkn, m_usercontainer, m_pxy);
                }
                else
                	remotebin=rr.data;
                
                SyncStatus.SetStatus("Identitying the changes between local merge and remote");
                if (remotebin == null)
                {                    
                    Config.logger.debug("NO user metadata file in server at this time.");
                	Iterator<fileInfo> it = lastlocal.filelist.iterator();
                    while(it.hasNext())
                    {
                    	fileInfo tmp=it.next();
                    	if(tmp.fop != FOP.LOCAL_HAS_DELETED)
                    	{
                    		tmp.fop=FOP.UPLOAD;
                    		if(tmp.type ==0 && tmp.filehash=="")
                    			tmp.filehash= HashCalc.GetFileCityHash(tmp.filename);
                    	}
                    }                   
                }
                else
                {                  
                    userMetaData tmpumd=new userMetaData(remotebin);
                    Config.logger.debug(tmpumd.ConvertToHTML("Getting remote file metadata snapshot"));
                    lastlocal.Merge(tmpumd);
                    Config.logger.debug(lastlocal.ConvertToHTML("Merged with remote metafile"));
                }
                
                userMetaData merged;
                merged = lastlocal;
                               
                Iterator<fileInfo> it = merged.filelist.iterator();
                
                SyncStatus.SetStatus("Identitying done and start to processsing objects");
                
                while(it.hasNext())
                {
                    fileInfo fi=it.next();
                    Config.logger.debug("Start to process:"+fi.ConvertToHTML());
                    SyncStatus.SetStatus(fi.filename,"start to sync","Syncing (upload/download/copy/overwrite) "+ fi.filename);
                	if (fi.type == 1 || fi.type == 2)//2 root folder, 1 sub folder
                    {
                        if(fi.fop == FOP.NEW)//fi.fop 1. NEW, 2. DOWNLOAD, 3. UPLOAD and 4. NONE
                        	flgchanged=true;
                		if (fi.fop == FOP.DOWNLOAD)
                        {
                        	File dir = new File(fi.filename);
                        	dir.mkdir();
                            flgchanged = true;
                            Config.logger.info("Create local Folder:"+fi.filename);
                        }
                        if (fi.fop == FOP.UPLOAD || fi.fop == FOP.REMOTE_NEED_TOBE_DELETED)
                        {
                            flgchanged = true;
                            UpdateRemoteUserMetaFile(fi);  
                        //}else if (fi.top == FOP.REMOTE_NEED_TOBE_DELETED)
                        //{
                        	
                        }
                        fi.fop=FOP.NONE;
                        continue;
                    }
                    switch (fi.fop)//except 2 and 1, 0 represent files
                    {
                        case COPY:
                            {
                                try
                                {
                                    flgchanged = true;
                                    Date dts=new Date();
                                    Iterator<fileInfo> it1 = merged.filelist.iterator();
                                    boolean found= false;
                                    fileInfo f=null;
                                    while(it1.hasNext())
                                    {
                                    	f=it1.next(); //find copy but with delete then it's rename or move
                                    	if(f.filehash.compareToIgnoreCase( fi.filehash)==0 && (f.fop==FOP.LOCAL_HAS_DELETED || f.fop==FOP.REMOTE_HAS_DELETED ))
                                    	{
                                    		found=true;
                                    		break;
                                    	}
                                    }
                                    
                                    if (found == true) //Move or Rename: it is a move action, since local deleted or remote deleted
                                    {
                                        fi.guid = f.guid;
                                        fi.parentguid = f.parentguid;
                                        fi.status = f.status;
                                        f.fop=FOP.MOVE_FROM_REF;
                                        Config.logger.info("File:Move/Rename----" + fi.filename +" from "+ f.filename);
                                    }
                                    else //COPY: not share the same object for multi-versions purpose. just copy the object in server side.
                                    {   //desguid (destination) is new, srcguid ( source ) is old, copy chunk metadata from old but give a new CLM
                                        String desguid = SmallFunctions.GenerateGUID();
                                        String srcguid = fi.guid;
                                        fi.guid = desguid;

                                        rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+srcguid, m_pxy);
                                        byte[] filedata = rr.data;
                                        fileMetadataWithVersion fmds = new fileMetadataWithVersion(filedata);
                                        Collections.sort(fmds.data);
                                        
                                        //Put File Level Meta Data fxxxxxx
                                        //RestConnector.PutFile(m_tkn, m_usercontainer, "f"+fi.guid, fmds.data.get(0).ConvertToByteArray(), m_pxy);
                                        
                                        boolean bolbreak = false;
                                        for(fileMetadata m : fmds.data)
                                        {
                                        	if (bolbreak){break;} //if did it already then skip , end loop
                                        	if (m.hashcode.toString().equals(fi.filehash.toString())){ //if it's the correct file hash, then insert file level meta and add ref counter
                                                //Put File Level Meta Data fxxxxxx
                                                RestConnector.PutFile(m_tkn, m_usercontainer, "f"+fi.guid, m.ConvertToByteArray(), m_pxy);
                                                if (Config.refcounter == 1) {
			                                        for(chunk c : m.data)
			                                        {
		                                        	//reduce ref counter or delete object in 1 min
			                                   		    RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, m_pxy);
			                                            Config.logger.debug("add last batch of ref counter at object level for COPY" + "c"+ Integer.toString(c.flag) + c.hashvalue);
		                                    		}
		                                        } 
		                                        bolbreak = true;
	                                        }
                                        }
                                        
                                        Config.logger.info("File:COPY----" + fi.filename);;
                                    }
                                    
                                    UpdateRemoteUserMetaFile(fi);   
                                    Date dte=new Date();                                    
                                    Config.logger.info("Cost----Times:" + (dte.getTime()-dts.getTime()) + " MillSeconds    Traffic: 0/" + fi.bytelength);
                                    fi.fop = FOP.NONE;
                                    
                                }
                                catch (Exception ex)
                                {
                                    fi.fop = FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
                        case DOWNLOAD:
                            {

                                try
                                {
                                    flgchanged = true;
                                    Date dts=new Date();                                   
                                    boolean needdownload = true;
                                    boolean found=false;
                                    Iterator<fileInfo> it1 = merged.filelist.iterator();
                                    fileInfo f=null;
                                    while(it1.hasNext())
                                    {
                                    	f=it1.next();
                                    	if(f.filehash==fi.filehash && f.filename.compareToIgnoreCase(fi.filename)!=0 && (f.fop==FOP.LOCAL_HAS_DELETED || f.fop== FOP.REMOTE_HAS_DELETED) )
                                    	{
                                    		found=true;
                                    		break;		
                                    	}
                                    }
                                    
                                    if (found)
                                    {
                                        //this is a move/rename action
                                        if (new File(f.filename).exists())
                                        {
                                        	File file = new File(fi.filename);
                                        	String dirPath = file.getAbsoluteFile().getParentFile().getAbsolutePath();
                                        	File dir = new File(dirPath);
                                        	dir.mkdir();
                                        	new File(f.filename).renameTo(new File(fi.filename));
                                        	Config.logger.info("Rename "+ f.filename +" to  "+ fi.filename);
                                            needdownload = false;
                                        }
                                    }
                                    else
                                    {
                                    	boolean found2=false;
                                    	fileInfo f2=null;
                                        Iterator<fileInfo> it2 = merged.filelist.iterator();
                                        while(it2.hasNext())
                                        {
                                        	f2=it2.next();
                                        	if(f2.filehash==fi.filehash && f.filename.compareToIgnoreCase(fi.filename)!=0 && (f.fop==FOP.NONE || f.fop== FOP.REMOTE_NEED_OVERWRITE) )
                                        	{
                                        		found2=true;
                                        		break;
                                        	}
                                        }
                                        if (found2)
                                        {
                                            //this is a copy action
                                            if (new File(f2.filename).exists())
                                            {
                                            	File file = new File(fi.filename);
                                            	String dirPath = file.getAbsoluteFile().getParentFile().getAbsolutePath();
                                            	File dir = new File(dirPath);
                                            	dir.mkdir();
                                                SmallFunctions.copyFile(new File(f2.filename), file);
                                                Config.logger.info("Copy "+ f2.filename +" to  "+ fi.filename);
                                                needdownload = false;
                                            }
                                        }
                                    }
                                    int downloadsize=0;
                                    if (needdownload)
                                    {
                                        
                                    	rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+fi.guid, m_pxy);
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
                                        FileOutputStream out = new FileOutputStream(fi.filename);
                                    	out.write(realdata);
                                    	out.close();
                                    	Config.logger.info("Downloaded " + fi.filename);
                                    }
                                    new File(fi.filename).setLastModified(fi.dt.getTime());
                                    Date dte=new Date();                                                            
                                    Config.logger.info("Cost----Times:" + (dte.getTime() - dts.getTime()) + " MillSeconds    Traffic:" + (needdownload ? downloadsize : 0) + "/" + fi.bytelength);
                                    fi.fop = FOP.NONE;
                                    //Logs.WriteStatusFile();
                                }
                                catch (Exception ex)
                                {
                                    fi.fop = FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
                        case UPLOAD:
                            {
                                try
                                {
                                    flgchanged = true;
                                    boolean found=false;
                                    fileInfo tmp2=null;
                                    Date dts=new Date();
                                    for(fileInfo f: merged.filelist)
                                    {
                                    	tmp2=f;
                                    	if(f.filehash==fi.filehash && f.filename.compareToIgnoreCase(fi.filename)!=0 && f.fop== FOP.LOCAL_HAS_DELETED)
                                    	{
                                    		found=true;
                                    		break;
                                    	}
                                    }
                                    boolean  needupload = true;
                                    if (found)// it is rename or move
                                    {
                                       fi.guid = tmp2.guid; 
                                       needupload = false;                                           
                                    }
                                    long uploadsize=0;
                                    long dsize = 0;
                                    fileMetadataWithVersion fmds = new fileMetadataWithVersion();
                                    if (needupload)
                                    {
                                    	fileMetadata fmd = fileMetadata.GetMetadata(fi.filename, m_mod,Config.divider,Config.refactor,Config.min,Config.max,Config.fixedchunksize,Config.ct);                                                                              
                                    	Date dtm=new Date();

	                                    if(clsExperiment.ExperimentMetaDataDump(fi.filename, (dtm.getTime() - dts.getTime()), fmd.data.toString()))
	                                    {Config.logger.debug("Experiment Dump Meta OK");}
	                                    else{Config.logger.debug("Experiment Dump Meta Fail");}
                                    	
                                        //fmd.data.size();
                                        if (fmd.byteslength > l_buffer){
                                            
                                            int dcount = 1;
                                            //File FilePath = new File(fi.filename).toPath();
                                            
                                            byte[] filedata = GetFileByteArray(fi.filename, dcount);
                                            
                                        	Date dti=new Date();
                                        	
    	                                    if(clsExperiment.ExperimentDcountDump(fi.filename, (dti.getTime() - dts.getTime()), Integer.toString(dcount), 0, 0, 0, "0.00%"))
    	                                    {Config.logger.debug("Experiment Dump Dcount First OK");}
    	                                    else{Config.logger.debug("Experiment Dump Dcount Frist Fail");}	
                                            
    	                                    Hashtable<String, String> ht = new Hashtable<String, String>();
                                	        
	                                        
    	                                    //Get ExecutorService from Executors utility class, thread pool size is 10
    	                                    ExecutorService executor = Executors.newFixedThreadPool(l_worker);
    	                                    //create a list to hold the Future object associated with Callable
    	                                    List<Future<String>> list = new ArrayList<Future<String>>();
    	                                    
	                                        for(chunk c : fmd.data)
	                                        {	    
	                                        		//if hash table can't find it
		                                            if (ht.get(c.hashvalue)==null)
		                                            {
		                                               int tmpf=-1;
		                                               
		                                               // cc is current object ( hot data ) and ccc is backup object ( cold data )
		                                            	if(gcc.contains("c1"+c.hashvalue)){
		                                            		tmpf=1;
		                                            		if (Config.refcounter == 1) {
		                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
		                                            		}
		                                            	}
		                                            	else if (gcc.contains("c0"+c.hashvalue)){
		                                            		tmpf=0;
		                                            		if (Config.refcounter == 1){
		                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
		                                            		}
		                                            	}else if(gbc.contains("backup/" + "c1"+c.hashvalue)){
		                                            		tmpf=1;
		                                            		if (Config.refcounter == 1) {//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
		                                            			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c1"+c.hashvalue, m_usercontainer, "/" + "c1"+c.hashvalue, m_pxy);
		                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
		                                            			gcc.add("c1" + c.hashvalue);
		                                            			gbc.remove("backup/" + "c1"+c.hashvalue);
		                                            		}
		                                            	}
		                                            	else if (gbc.contains("backup/" + "c0"+c.hashvalue)){
		                                            		tmpf=0;
		                                            		if (Config.refcounter == 1){//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
		                                            			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c0"+c.hashvalue, m_usercontainer, "/" + "c0"+c.hashvalue, m_pxy);                                           			
		                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
		                                            			gcc.add("c0" + c.hashvalue);
		                                            			gbc.remove("backup/" + "c0"+c.hashvalue);
		                                            		}
		                                            	}
		                                            	
		                                            	if(tmpf==1)
		                                            		c.flag= c.flag | 1; //zip size is smaller than real size, c1+cityhash                                           	
		                                            	else if(tmpf==0)
		                                            		c.flag= c.flag & ~1; //real size is smaller than zip , c0+cityhash
		                                            	if(tmpf>=0)
		                                            		continue;

		                                        		int intstart = (int) (c.start - (l_buffer*(dcount-1)));
		                                        		int intend = (int) (c.end - (l_buffer*(dcount-1)));
			                                        	/**if ( (dsize + c.end - c.start + 1) > 1*1024*1024*1024*dcount ) **/
		                                        		byte[] tmp = new byte[(intend - intstart + 1)];
			                                        	if ( intend > ( l_buffer ))
			                                        	{
			                                        		dcount = dcount + 1;
			                                        		byte[] tmpfront = new byte[(int)l_buffer - intstart];
			                                        		System.arraycopy(filedata, intstart, tmpfront, 0, tmpfront.length);
			                                        		//get next 1G buffer
			                                        		try {
			                                        			filedata = GetFileByteArray(fi.filename, dcount);
			                                        			
			                                                	Date dtj=new Date();
			                                                	
			                    		                        double dbpercentage = (double)dsize / (double)fi.bytelength;
			                    		                        DecimalFormat percentFormat= new DecimalFormat("#.##%");
			                    		                        
			            	                                    if(clsExperiment.ExperimentDcountDump(fi.filename, (dtj.getTime() - dti.getTime()), Integer.toString(dcount), uploadsize, dsize,fi.bytelength, String.valueOf(percentFormat.format(dbpercentage))))
			            	                                    {Config.logger.debug("Experiment Dump Dcount Loop OK");}
			            	                                    else{Config.logger.debug("Experiment Dump Dcount Loop Fail");}	
			            	                                    
			            	                        			Config.logger.debug("Renew token from m_usernameserver when new upload");
			            	                        			if(GetToken()==false)
			            	                        				return;
			            	                        			else
			            	                        				Config.logger.debug("Got token: " + m_tkn);
			            	                        			
			            	                        			dti = dtj;
			            	                                    
			                                        		} catch(IOException ex){
			                                                	System.out.println(ex.toString());
			                                                }
			                                        		byte[] tmpback = new byte[intend - (int)l_buffer+1];
			                                        		System.arraycopy(filedata, 0, tmpback, 0, tmpback.length);
			                                        		//comcat tmpfront and tmpback into tmp
			                                        		System.arraycopy(tmpfront, 0, tmp, 0, tmpfront.length);
			                                        		System.arraycopy(tmpback, 0, tmp, tmpfront.length, tmpback.length);

			                                        	}
			                                        	else
			                                        	{
			                                        		System.arraycopy(filedata, intstart, tmp, 0, tmp.length); 
			                                        	}
			                                        	
			                                        	byte[] ztmp=ZipProcess.zip(tmp);
			                                        	
		                                                if(ztmp.length < tmp.length)
		                                                {
		                                                	c.flag= c.flag | 1;
		                                                	
		                                                	if (fthread){
			                                                	//multithreading sub start
			        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, ztmp);
			        	                                        //submit Callable tasks to be executed by thread pool
			        	                                        Future<String> future = executor.submit(callable);
			        	                                        //add Future to the list, we can get return value using Future
			        	                                        list.add(future);
			                                                	//multithreading sub end	                                                		
		                                                	}else{
		                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
		                                                	}
		                                                	
		                                                	ht.put(c.hashvalue,"1");
		                                                	uploadsize+=ztmp.length;
		                                                	
		                                                }
		                                                else
		                                                {
		                                                	c.flag= c.flag & ~1;
		                                                	
		                                                	if (fthread){
			                                                	//multithreading sub start
			        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, tmp);
			        	                                        //submit Callable tasks to be executed by thread pool
			        	                                        Future<String> future = executor.submit(callable);
			        	                                        //add Future to the list, we can get return value using Future
			        	                                        list.add(future);
			                                                	//multithreading sub end	                                                		
		                                                	}else{
		                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
		                                                	}
	                                                			                                                	
		                                                	ht.put(c.hashvalue,"0");
		                                                	uploadsize+=tmp.length;
		                                                }
		                                            	
		                                                gcc.add("c"+ Integer.toString(c.flag) + c.hashvalue);
	                                            
		                                            }
		                                            else
		                                            {
		                                            	//when has table can get the has ( inter dedup )
		                                            	if(Integer.parseInt(ht.get(c.hashvalue))==1)
		                                            		c.flag=c.flag|1;
		                                            	else
		                                            		c.flag= c.flag & ~1;     
		                                            	
		                                        		int intend = (int) (c.end - (l_buffer*(dcount-1)));

			                                        	if ( intend > ( l_buffer ))
			                                        	{
			                                        		dcount = dcount + 1;

			                                        		try {
			                                        			filedata = GetFileByteArray(fi.filename, dcount);
			                                        			
			                                                	Date dtj=new Date();
			                                                	
			                    		                        double dbpercentage = (double)dsize / (double)fi.bytelength;
			                    		                        DecimalFormat percentFormat= new DecimalFormat("#.##%");
			                    		                        
			            	                                    if(clsExperiment.ExperimentDcountDump(fi.filename, (dtj.getTime() - dti.getTime()), Integer.toString(dcount), uploadsize, dsize,fi.bytelength, String.valueOf(percentFormat.format(dbpercentage))))
			            	                                    {Config.logger.debug("Experiment Dump Dcount Loop OK");}
			            	                                    else{Config.logger.debug("Experiment Dump Dcount Loop Fail");}	
			            	                                    
			            	                        			Config.logger.debug("Renew token from m_usernameserver when new upload");
			            	                        			if(GetToken()==false)
			            	                        				return;
			            	                        			else
			            	                        				Config.logger.debug("Got token: " + m_tkn);
			            	                                    
			                                        		} catch(IOException ex){
			                                                	System.out.println(ex.toString());
			                                                }

			                                        	}
		                                            	
		                                            }

	                                            dsize = dsize + c.end - c.start + 1;
	                                        }
	                                        //Collect future list
    	                                    for(Future<String> fut : list){
    	                                        try {
    	                                            //print the return value of Future, notice the output delay in console
    	                                            // because Future.get() waits for task to get completed
    	                                            System.out.println(new Date()+ "::"+fut.get());
    	                                        } catch (InterruptedException | ExecutionException e) {
    	                                            e.printStackTrace();
    	                                        }
    	                                    }
    	                                    //shut down the executor service (thread pool) now, you must shutdown thread pool in order to terminate all the threads of the pools
    	                                    executor.shutdown();
		                                    
    	                                    //fileMetadataWithVersion fmds = new fileMetadataWithVersion();
	                                        fmds.data.add(fmd);
	                                        Config.logger.info("Put file: " + fi.filename + " (chunks level index) metadata: " + "f" + fi.guid + " with size: " + fmds.ConvertToByteArray().length);
	                                        RestConnector.PutFile(m_tkn, m_usercontainer, "f"+fi.guid, fmds.ConvertToByteArray(), m_pxy);
	                                                 

                                        }
                                        else {
	                                    	byte[] filedata = Files.readAllBytes(new File(fi.filename).toPath());
	                                        fmd.data.size();
	                                        Hashtable<String, String> ht = new Hashtable<String, String>();
	                                        
    	                                    //Get ExecutorService from Executors utility class, thread pool size is 10
    	                                    ExecutorService executor = Executors.newFixedThreadPool(l_worker);
    	                                    //create a list to hold the Future object associated with Callable
    	                                    List<Future<String>> list = new ArrayList<Future<String>>();

	                                        
	                                        for(chunk c : fmd.data)
	                                        {
	                                            if (ht.get(c.hashvalue)==null)
	                                            {
	                                               int tmpf=-1;
	                                               
	                                               // cc is current object ( hot data ) and ccc is backup object ( cold data )
	                                            	if(gcc.contains("c1"+c.hashvalue)){
	                                            		tmpf=1;
	                                            		if (Config.refcounter == 1) {
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
	                                            		}
	                                            	}
	                                            	else if (gcc.contains("c0"+c.hashvalue)){
	                                            		tmpf=0;
	                                            		if (Config.refcounter == 1){
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
	                                            		}
	                                            	}else if(gbc.contains("backup/" + "c1"+c.hashvalue)){
	                                            		tmpf=1;
	                                            		if (Config.refcounter == 1) {//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
	                                            			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c1"+c.hashvalue, m_usercontainer, "/" + "c1"+c.hashvalue, m_pxy);
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
	                                            			gcc.add("c1" + c.hashvalue);
	                                            			gbc.remove("backup/" + "c1"+c.hashvalue);
	                                            		}
	                                            	}
	                                            	else if (gbc.contains("backup/" + "c0"+c.hashvalue)){
	                                            		tmpf=0;
	                                            		if (Config.refcounter == 1){//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
	                                            			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c0"+c.hashvalue, m_usercontainer, "/" + "c0"+c.hashvalue, m_pxy);                                           			
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
	                                            			gcc.add("c0" + c.hashvalue);
	                                            			gbc.remove("backup/" + "c0"+c.hashvalue);
	                                            		}
	                                            	}
	                                            	
	                                            	if(tmpf==1)
	                                            		c.flag= c.flag | 1; //zip size is smaller than real size, c1+cityhash                                           	
	                                            	else if(tmpf==0)
	                                            		c.flag= c.flag & ~1; //real size is smaller than zip , c0+cityhash
	                                            	if(tmpf>=0)
	                                            		continue;
	                                            		
	                                            		
	                                            	byte[] tmp = new byte[(int)(c.end - c.start + 1)];                                            
	                                                System.arraycopy(filedata, (int)c.start, tmp, 0, tmp.length);
	                                                byte[] ztmp=ZipProcess.zip(tmp);
	                                                
	                                                if(ztmp.length < tmp.length)
	                                                {
	                                                	c.flag= c.flag | 1;
	                                                	
	                                                	if (fthread){
		                                                	//multithreading sub start
		        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, ztmp);
		        	                                        //submit Callable tasks to be executed by thread pool
		        	                                        Future<String> future = executor.submit(callable);
		        	                                        //add Future to the list, we can get return value using Future
		        	                                        list.add(future);
		                                                	//multithreading sub end	                                                		
	                                                	}else{
	                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
	                                                	}

	                                                	ht.put(c.hashvalue,"1");
	                                                	uploadsize+=ztmp.length;
	                                                	
	                                                }
	                                                else
	                                                {
	                                                	c.flag= c.flag & ~1;
	                                                	
	                                                	if (fthread){
		                                                	//multithreading sub start
		        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, tmp);
		        	                                        //submit Callable tasks to be executed by thread pool
		        	                                        Future<String> future = executor.submit(callable);
		        	                                        //add Future to the list, we can get return value using Future
		        	                                        list.add(future);
		                                                	//multithreading sub end	                                                		
	                                                	}else{
	                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
	                                                	}

	                                                	ht.put(c.hashvalue,"0");
	                                                	uploadsize+=tmp.length;
	                                                }
	                                                gcc.add("c"+ Integer.toString(c.flag) + c.hashvalue);
	                                                
	                                                
	                                            }
	                                            else
	                                            {
	                                            	if(Integer.parseInt(ht.get(c.hashvalue))==1)
	                                            		c.flag=c.flag|1;
	                                            	else
	                                            		c.flag= c.flag & ~1;                                            	                                            	
	                                            }
	                                            dsize = dsize + c.end - c.start + 1;
	                                        }
	                                        //Collect future list
    	                                    for(Future<String> fut : list){
    	                                        try {
    	                                            //print the return value of Future, notice the output delay in console
    	                                            // because Future.get() waits for task to get completed
    	                                            System.out.println(new Date()+ "::"+fut.get());
    	                                        } catch (InterruptedException | ExecutionException e) {
    	                                            e.printStackTrace();
    	                                        }
    	                                    }
    	                                    //shut down the executor service (thread pool) now, you must shutdown thread pool in order to terminate all the threads of the pools
    	                                    executor.shutdown();
	                                        
	                                        //fileMetadataWithVersion fmds = new fileMetadataWithVersion();
	                                        fmds.data.add(fmd);
	                                        Config.logger.info("Put file: " + fi.filename + " (chunks level index) metadata: " + "f" + fi.guid + " with size: " + fmds.ConvertToByteArray().length);
	                                        RestConnector.PutFile(m_tkn, m_usercontainer, "f"+fi.guid, fmds.ConvertToByteArray(), m_pxy);
	                                    }
	                                    Config.logger.info("Uploaded " + fi.filename);
	                                    UpdateRemoteUserMetaFile(fi);
	                                    Date dte=new Date();
	                                    Config.logger.info("Cost----Times:" + (dte.getTime() - dts.getTime()) + " MillSeconds    Traffic:" + uploadsize + "/" + dsize + "/" + fi.bytelength);
	                                    if(clsExperiment.ExperimentDump(fi.filename, (dte.getTime() - dts.getTime()), uploadsize, dsize,fi.bytelength, fmds.ConvertToByteArray().length))
	                                    {Config.logger.debug("Experiment Dump OK");}
	                                    else{Config.logger.debug("Experiment Dump Fail");}
	                                    fi.fop = FOP.NONE;
	                                }
                                }
                                catch (Exception ex)
                                {
                                    fi.fop = FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
                         case LOCAL_NEED_OVERWRITE:
                            {
                                try
                                {
                                    flgchanged = true;
                                    Date dts=new Date();
                                    byte[] filedata = RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+fi.guid, m_pxy).data;
                                    //get remote metadata
                                    fileMetadataWithVersion fmd = new fileMetadataWithVersion(filedata);
                                  
                                    Collections.sort(fmd.data);
                                    int lastversion=fmd.data.size();
                                    byte[] realdata = new byte[(int)fmd.data.get(lastversion-1).byteslength];
                                    long dsize = 0;
                                    Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
                                    
                                    //get the latest mod
                                    for(fileMetadata m : fmd.data)
                                    {
                                    	m_mod=m.mod;
                                    }  
                                    
                                    //check local file name and generate most update chunk level base on file metadata
                                    fileMetadata localfmd = fileMetadata.GetMetadata(fi.filename, m_mod,Config.divider,Config.refactor,Config.min,Config.max,Config.fixedchunksize,Config.ct);
                                    byte[] localcache = Files.readAllBytes(new File(fi.filename).toPath());
                                    int downloadsize=0;
                                    for (chunk c : localfmd.data)
                                    {
                                        if (ht.get(c.hashvalue)==null)
                                        {
                                            byte[] tmp=new byte[(int)(c.end-c.start+1)];
                                            System.arraycopy(localcache,(int)c.start,tmp,0,tmp.length);
                                            ht.put(c.hashvalue, tmp);
                                        }
                                    }
                                    for (chunk c : fmd.data.get(lastversion-1).data)
                                    {
                                    	if (ht.get(c.hashvalue)==null)
                                        {
                                            byte[] temp = RestConnector.GetContainer(m_tkn, m_usercontainer +"/c"+Integer.toString(c.flag) +c.hashvalue, m_pxy).data;
                                            downloadsize+=temp.length;
                                            if(( c.flag & 1 )== 1 )
                                            	temp=ZipProcess.unzip(temp);
                                            System.arraycopy(temp, 0, realdata, (int)c.start, temp.length);
                                            ht.put(c.hashvalue, temp);                                            
                                        }
                                        else
                                        {
                                        	System.arraycopy((byte[])ht.get(c.hashvalue), 0, realdata, (int)c.start, ((byte[])ht.get(c.hashvalue)).length);
                                        }
                                        dsize = dsize + c.end - c.start + 1;
                                    }
                                    FileOutputStream out = new FileOutputStream(fi.filename);
                                	out.write(realdata);
                                	out.close();
                                    new File(fi.filename).setLastModified(fi.dt.getTime());                                                                  
                                    Config.logger.info("OverWrited local " + fi.filename);
                                    Date dte=new Date();
                                    Config.logger.info("Cost----Times:" + (dte.getTime() - dts.getTime()) + " MillSeconds    Traffic:" + downloadsize + "/" + fi.bytelength);
                                    fi.fop = FOP.NONE;
                                }
                                catch (Exception ex)
                                {
                                    fi.fop = FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
 	                    case MOVE_FROM_REF:
		                    {
		                    	
		                        //update local metadata
		                    	flgchanged = true;
		                    	
		                        for(fileInfo f: merged.filelist)
                                {
		                        	if(f.fop==FOP.MOVE_TO_REF)
		                        	{
			                            //update remote metadata
			                            UpdateRemoteUserMetaFile(fi, f); 
		                        	}
                                }
		                    	

		                    }
		                    break;
                        case REMOTE_NEED_OVERWRITE:
                            {
                                try
                                {
                                    flgchanged = true;
                                    Date dts=new Date();
                                    byte[] remotefilemetadata = RestConnector.GetContainer(m_tkn, m_usercontainer + "/f"+fi.guid, m_pxy).data;
                                    //get metatdata on server
                                    fileMetadataWithVersion fmds = new fileMetadataWithVersion(remotefilemetadata);
                                    //get the latest mod
                                    for(fileMetadata m : fmds.data)
                                    {
                                    	m_mod=m.mod;
                                    	break;
                                    }
                                    //get local metadata but use latest mod on server
                                    fileMetadata fmd = fileMetadata.GetMetadata(fi.filename, m_mod, Config.divider,Config.refactor,Config.min,Config.max,Config.fixedchunksize,Config.ct);
                                    long uploadsize=0;
                                    long dsize = 0;
                                    if (fmd.byteslength > l_buffer){
                                        
                                        int dcount = 1;
                                        //File FilePath = new File(fi.filename).toPath();
                                        
                                        byte[] filedata = GetFileByteArray(fi.filename, dcount);
                              
                                    	Date dti=new Date();
	                                    if(clsExperiment.ExperimentDcountDump(fi.filename, (dti.getTime() - dts.getTime()), Integer.toString(dcount), 0, 0, 0, "0.00%"))
	                                    {Config.logger.debug("Experiment Dump Dcount First OK");}
	                                    else{Config.logger.debug("Experiment Dump Dcount Frist Fail");}	
	                                    
                                        Hashtable<String, String> ht = new Hashtable<String, String>();
                           
	                                    //Get ExecutorService from Executors utility class, thread pool size is 10
	                                    ExecutorService executor = Executors.newFixedThreadPool(l_worker);
	                                    //create a list to hold the Future object associated with Callable
	                                    List<Future<String>> list = new ArrayList<Future<String>>();

                                        
                                        for(chunk c : fmd.data)
                                        {	                                        	
	                                            if (ht.get(c.hashvalue)==null)
	                                            {
	                                                //tmpf = -1 is default can't find the chunk in gcc array
	                                            	int tmpf=-1;
	                                               
	                                               // cc is current object ( hot data ) and ccc is backup object ( cold data )
	                                            	if(gcc.contains("c1"+c.hashvalue)){
	                                            		tmpf=1;
	                                            		if (Config.refcounter == 1) {
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
	                                            		}
	                                            	}
	                                            	else if (gcc.contains("c0"+c.hashvalue)){
	                                            		tmpf=0;
	                                            		if (Config.refcounter == 1){
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
	                                            		}
	                                            	}else if(gbc.contains("backup/" + "c1"+c.hashvalue)){
	                                            		tmpf=1;
	                                            		if (Config.refcounter == 1) {//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
	                                            			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c1"+c.hashvalue, m_usercontainer, "/" + "c1"+c.hashvalue, m_pxy);
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
	                                            			gcc.add("c1" + c.hashvalue);
	                                            			gbc.remove("backup/" + "c1"+c.hashvalue);
	                                            		}
	                                            	}
	                                            	else if (gbc.contains("backup/" + "c0"+c.hashvalue)){
	                                            		tmpf=0;
	                                            		if (Config.refcounter == 1){//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
	                                            			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c0"+c.hashvalue, m_usercontainer, "/" + "c0"+c.hashvalue, m_pxy);                                           			
	                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
	                                            			gcc.add("c0" + c.hashvalue);
	                                            			gbc.remove("backup/" + "c0"+c.hashvalue);
	                                            		}
	                                            	}
	                                            		

	                                            	if(tmpf==1)
	                                            		c.flag= c.flag | 1; //zip size is smaller than real size, c1+cityhash                                           	
	                                            	else if(tmpf==0)
	                                            		c.flag= c.flag & ~1; //real size is smaller than zip , c0+cityhash
	                                            	
	                                            	
	                                        		int intstart = (int) (c.start - (l_buffer*(dcount-1)));
	                                        		int intend = (int) (c.end - (l_buffer*(dcount-1)));
		                                        	/**if ( (dsize + c.end - c.start + 1) > 1*1024*1024*1024*dcount ) **/
	                                        		byte[] tmp = new byte[(intend - intstart + 1)];
		                                        	if ( intend > ( l_buffer ) )
		                                        	{
		                                        		dcount = dcount + 1;
		                                        		byte[] tmpfront = new byte[(int)l_buffer - intstart];
		                                        		System.arraycopy(filedata, intstart, tmpfront, 0, tmpfront.length);
		                                        		//get next 1G buffer
		                                        		try {
		                                        			filedata = GetFileByteArray(fi.filename, dcount);
		                                        			
		                                                	Date dtj=new Date();
		                                                	
		                    		                        double dbpercentage = (double)dsize / (double)fi.bytelength;
		                    		                        DecimalFormat percentFormat= new DecimalFormat("#.##%");
		                    		                        
		            	                                    if(clsExperiment.ExperimentDcountDump(fi.filename, (dtj.getTime() - dti.getTime()), Integer.toString(dcount), uploadsize, dsize,fi.bytelength, String.valueOf(percentFormat.format(dbpercentage))))
		            	                                    {Config.logger.debug("Experiment Dump Dcount Loop OK");}
		            	                                    else{Config.logger.debug("Experiment Dump Dcount Loop Fail");}
		            	                                    
		            	                        			Config.logger.debug("Renew token from m_usernameserver when remote_need_overwrite upload");
		            	                        			if(GetToken()==false)
		            	                        				return;
		            	                        			else
		            	                        				Config.logger.debug("Got token: " + m_tkn);
		            	                                    
		                                        		} catch(IOException ex){
		                                                	System.out.println(ex.toString());
		                                                }
		                                        		byte[] tmpback = new byte[intend - (int)l_buffer+1];
		                                        		System.arraycopy(filedata, 0, tmpback, 0, tmpback.length);
		                                        		//concanate tmpfront and tmpback into tmp
		                                        		System.arraycopy(tmpfront, 0, tmp, 0, tmpfront.length);
		                                        		System.arraycopy(tmpback, 0, tmp, tmpfront.length, tmpback.length);

		                                        	}
		                                        	//else
		                                        	//{
		                                        	//	System.arraycopy(filedata, intstart, tmp, 0, tmp.length); 
		                                        	//}	                                            	
	                                            	
	                                            	
	                                            	//if tmp = 0 or 1 then means find the chunk in gcc then we can skip this loop ( chunks )
	                                            	if(tmpf>=0)
	                                            		continue;

	                                        		//int intstart = (int) (c.start - (l_buffer*(dcount-1)));
	                                        		//int intend = (int) (c.end - (l_buffer*(dcount-1)));
		                                        	/**if ( (dsize + c.end - c.start + 1) > 1*1024*1024*1024*dcount ) **/
	                                        		//byte[] tmp = new byte[(intend - intstart + 1)];
		                                        	if ( intend <= ( l_buffer ) )
		                                        	/*
		                                        	{
		                                        		dcount = dcount + 1;
		                                        		byte[] tmpfront = new byte[(int)l_buffer - intstart];
		                                        		System.arraycopy(filedata, intstart, tmpfront, 0, tmpfront.length);
		                                        		//get next 1G buffer
		                                        		try {
		                                        			filedata = GetFileByteArray(fi.filename, dcount);
		                                        		} catch(IOException ex){
		                                                	System.out.println(ex.toString());
		                                                }
		                                        		byte[] tmpback = new byte[intend - (int)l_buffer];
		                                        		System.arraycopy(filedata, 0, tmpback, 0, tmpback.length);
		                                        		//concanate tmpfront and tmpback into tmp
		                                        		System.arraycopy(tmpfront, 0, tmp, 0, tmpfront.length);
		                                        		System.arraycopy(tmpback, 0, tmp, tmpfront.length, tmpback.length);

		                                        	}
		                                        	else
		                                        	*/
		                                        	{
		                                        		System.arraycopy(filedata, intstart, tmp, 0, tmp.length); 
		                                        	}
		                                        	
		                                        	byte[] ztmp=ZipProcess.zip(tmp);
		                                        	
	                                                if(ztmp.length < tmp.length)
	                                                {
	                                                	c.flag= c.flag | 1;
	                                                	
	                                                	if (fthread){
		                                                	//multithreading sub start
		        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, ztmp);
		        	                                        //submit Callable tasks to be executed by thread pool
		        	                                        Future<String> future = executor.submit(callable);
		        	                                        //add Future to the list, we can get return value using Future
		        	                                        list.add(future);
		                                                	//multithreading sub end	                                                		
	                                                	}else{
	                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
	                                                	}
	                                                	//RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
	                                                	ht.put(c.hashvalue,"1");
	                                                	uploadsize+=ztmp.length;
	                                                	
	                                                }
	                                                else
	                                                {
	                                                	c.flag= c.flag & ~1;
	                                                	
	                                                	if (fthread){
		                                                	//multithreading sub start
		        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, tmp);
		        	                                        //submit Callable tasks to be executed by thread pool
		        	                                        Future<String> future = executor.submit(callable);
		        	                                        //add Future to the list, we can get return value using Future
		        	                                        list.add(future);
		                                                	//multithreading sub end	                                                		
	                                                	}else{
	                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
	                                                	}
	                                                	//RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
	                                                	ht.put(c.hashvalue,"0");
	                                                	uploadsize+=tmp.length;
	                                                }
	                                            	
	                                                gcc.add("c"+ Integer.toString(c.flag) + c.hashvalue);
                                            
	                                            }
	                                            else
	                                            {
	                                            	if(Integer.parseInt(ht.get(c.hashvalue))==1)
	                                            		c.flag=c.flag|1;
	                                            	else
	                                            		c.flag= c.flag & ~1;   
	                                            	
	                                        		int intend = (int) (c.end - (l_buffer*(dcount-1)));

		                                        	if ( intend > ( l_buffer ))
		                                        	{
		                                        		dcount = dcount + 1;

		                                        		try {
		                                        			filedata = GetFileByteArray(fi.filename, dcount);
		                                        			
		                                                	Date dtj=new Date();
		                                                	
		                    		                        double dbpercentage = (double)dsize / (double)fi.bytelength;
		                    		                        DecimalFormat percentFormat= new DecimalFormat("#.##%");
		                    		                        
		            	                                    if(clsExperiment.ExperimentDcountDump(fi.filename, (dtj.getTime() - dti.getTime()), Integer.toString(dcount), uploadsize, dsize,fi.bytelength, String.valueOf(percentFormat.format(dbpercentage))))
		            	                                    {Config.logger.debug("Experiment Dump Dcount Loop OK");}
		            	                                    else{Config.logger.debug("Experiment Dump Dcount Loop Fail");}	
		            	                                    
		            	                        			Config.logger.debug("Renew token from m_usernameserver when new upload");
		            	                        			if(GetToken()==false)
		            	                        				return;
		            	                        			else
		            	                        				Config.logger.debug("Got token: " + m_tkn);
		            	                                    
		                                        		} catch(IOException ex){
		                                                	System.out.println(ex.toString());
		                                                }

		                                        	}
	                                            	
	                                            }

                                            dsize = dsize + c.end - c.start + 1;
                                        }
                                        //Collect future list
	                                    for(Future<String> fut : list){
	                                        try {
	                                            //print the return value of Future, notice the output delay in console
	                                            // because Future.get() waits for task to get completed
	                                            System.out.println(new Date()+ "::"+fut.get());
	                                        } catch (InterruptedException | ExecutionException e) {
	                                            e.printStackTrace();
	                                        }
	                                    }
	                                    //shut down the executor service (thread pool) now, you must shutdown thread pool in order to terminate all the threads of the pools
	                                    executor.shutdown();
	                                    
	                                    //fileMetadataWithVersion fmds = new fileMetadataWithVersion();
                                        fmds.data.add(fmd);
                                        Config.logger.info("Put file: " + fi.filename + " (chunks level index) metadata: " + "f" + fi.guid + " with size: " + fmds.ConvertToByteArray().length);
                                        RestConnector.PutFile(m_tkn, m_usercontainer , "f" + fi.guid, fmds.ConvertToByteArray(), m_pxy);
                                    	
                                    }
                                    else {
	                                    byte[] filedata = Files.readAllBytes(new File(fi.filename).toPath());
	                                    fmd.data.size();
	                                    Hashtable<String, String> ht = new Hashtable<String, String>();

	                                    //Get ExecutorService from Executors utility class, thread pool size is 10
	                                    ExecutorService executor = Executors.newFixedThreadPool(l_worker);
	                                    //create a list to hold the Future object associated with Callable
	                                    List<Future<String>> list = new ArrayList<Future<String>>();

	                                    
	                                    for(chunk c : fmd.data)
	                                    {
	                                    	 if (ht.get(c.hashvalue)==null)
	                                         {
	                                    		int tmpf=-1;
	                                         	if(gcc.contains("c1"+c.hashvalue)){
	                                        		tmpf=1;
	                                        		if (Config.refcounter == 1) {
	                                        			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
	                                        		}
	                                        	}
	                                        	else if (gcc.contains("c0"+c.hashvalue)){
	                                        		tmpf=0;
	                                        		if (Config.refcounter == 1){
	                                        			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
	                                        		}
	                                        	}else if(gbc.contains("backup/" + "c1"+c.hashvalue)){
	                                        		tmpf=1;
	                                        		if (Config.refcounter == 1) {//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
	                                        			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c1"+c.hashvalue, m_usercontainer, "/"+"c1"+c.hashvalue, m_pxy);                                           			
	                                        			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
	                                        			gcc.add("c1" + c.hashvalue);
	                                        			gbc.remove("backup/" + "c1"+c.hashvalue);
	                                        		}
	                                        	}
	                                        	else if (gcc.contains("backup/" + "c0"+c.hashvalue)){
	                                        		tmpf=0;
	                                        		if (Config.refcounter == 1){//Retrive object back to normal: rename from /backup/ to /, and add default reference counter 90000001
	                                        			RestConnector.RenameOrMoveFile(m_tkn, m_usercontainer, "backup/" + "c0"+c.hashvalue, m_usercontainer, "/"+"c0"+c.hashvalue, m_pxy);                                           			
	                                        			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
	                                        			gcc.add("c1" + c.hashvalue);
	                                        			gbc.remove("backup/" + "c0"+c.hashvalue);
	                                        		}
	                                        	}
	
	                                         	if(tmpf==1)
	                                         		c.flag= c.flag | 1;
	                                         	else if(tmpf==0)
	                                         		c.flag= c.flag & ~1;
	                                         	if(tmpf>=0)
	                                         		continue;
	                                    		 
	                                    		 byte[] tmp = new byte[(int)(c.end - c.start + 1)];                                            
	                                             System.arraycopy(filedata, (int)c.start, tmp, 0, tmp.length);
	                                             byte[] ztmp=ZipProcess.zip(tmp);
	                                             if(ztmp.length < tmp.length)
	                                             {
	                                             	c.flag= c.flag | 1;
	                                             	
                                                	if (fthread){
	                                                	//multithreading sub start
	        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, ztmp);
	        	                                        //submit Callable tasks to be executed by thread pool
	        	                                        Future<String> future = executor.submit(callable);
	        	                                        //add Future to the list, we can get return value using Future
	        	                                        list.add(future);
	                                                	//multithreading sub end	                                                		
                                                	}else{
                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
                                                	}
	                                             	//RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
	                                             	ht.put(c.hashvalue,"1");
	                                             	uploadsize+=ztmp.length;
	                                             }
	                                             else
	                                             {
	                                             	c.flag= c.flag & ~1;
	                                             	
                                                	if (fthread){
	                                                	//multithreading sub start
	        	                                        Callable<String> callable = new ObjUploaderRunnable(m_tkn, m_usercontainer, "c"+ Integer.toString(c.flag) + c.hashvalue, tmp);
	        	                                        //submit Callable tasks to be executed by thread pool
	        	                                        Future<String> future = executor.submit(callable);
	        	                                        //add Future to the list, we can get return value using Future
	        	                                        list.add(future);
	                                                	//multithreading sub end	                                                		
                                                	}else{
                                                		RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
                                                	}
	                                             	//RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
	                                             	ht.put(c.hashvalue,"0");
	                                             	uploadsize+=tmp.length;
	                                             }
	                                             gcc.add("c"+ Integer.toString(c.flag) + c.hashvalue);
	                                         }
	                                         else
	                                         {
	                                         	if(Integer.parseInt(ht.get(c.hashvalue))==1)
	                                         		c.flag=c.flag|1;
	                                         	else
	                                         		c.flag= c.flag & ~1;                                            	                                            	
	                                         }

	                                        dsize = dsize + c.end - c.start + 1;
	                                    } 
                                        //Collect future list
	                                    for(Future<String> fut : list){
	                                        try {
	                                            //print the return value of Future, notice the output delay in console
	                                            // because Future.get() waits for task to get completed
	                                            System.out.println(new Date()+ "::"+fut.get());
	                                        } catch (InterruptedException | ExecutionException e) {
	                                            e.printStackTrace();
	                                        }
	                                    }
	                                    //shut down the executor service (thread pool) now, you must shutdown thread pool in order to terminate all the threads of the pools
	                                    executor.shutdown();
	                                    
	                                    fmds.data.add(fmd);
	                                    Config.logger.info("Put file: " + fi.filename + " (chunks level index) metadata: " + "f" + fi.guid + " with size: " + fmds.ConvertToByteArray().length);
	                                    RestConnector.PutFile(m_tkn, m_usercontainer , "f" + fi.guid,  fmds.ConvertToByteArray(), m_pxy); 
                                    }
                                    Config.logger.info("Overwrite remote " + fi.filename);
                                    UpdateRemoteUserMetaFile(fi);
                                    Date dte=new Date();
                                    Config.logger.info("Cost----Times:" + (dte.getTime() - dts.getTime()) + " MillSeconds    Traffic:" + uploadsize + "/" + dsize + "/" + fi.bytelength);
                                    if(clsExperiment.ExperimentDump(fi.filename, (dte.getTime() - dts.getTime()), uploadsize, dsize, fi.bytelength, fmds.ConvertToByteArray().length))
                                    {Config.logger.debug("Experiment Dump OK");}
                                    else{Config.logger.debug("Experiment Dump Fail");}
                                    fi.fop = FOP.NONE;
                                }
                                catch (Exception ex)
                                {
                                    fi.fop=FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                //Deletion
                for (fileInfo fi : merged.filelist)
                {
                    SyncStatus.SetStatus("Syncing (deletion)"+ fi.filename);
                    if (fi.type == 1 || fi.type == 2)//folder
                    {
                        if (fi.fop == FOP.LOCAL_HAS_DELETED || fi.fop == FOP.REMOTE_HAS_DELETED)
                        {
                            new File(fi.filename).delete();
                            flgchanged = true;
                            Config.logger.info("Delete folder " + fi.filename);
                            if(fi.fop==FOP.LOCAL_HAS_DELETED)
                            	UpdateRemoteUserMetaFile( fi);  
                        }
                        continue;
                    }
                    switch (fi.fop) //Deletion : LOCAL_HAS_DELETED or REMOTE_HAS_DELETED
                    {
	                    case LOCAL_NEED_TOBE_DELETED:
                        {
                            try
                            {
                            	//local as delete, so don't need to delete local, just need to udpate the remove metadata
                                flgchanged = true;
                                if (new File(fi.filename).exists())
                                    new File(fi.filename).delete();
                                
                                Config.logger.info("Delete file " + fi.filename);
                                
                                //reduce ref counter
                        		ReduceRefCounterAndPurgeFile(fi);
                        			//RestConnector.ReduceObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
                                
                                //update remote metadata
                                UpdateRemoteUserMetaFile(fi); 
                                
                            }
                            catch (Exception ex)
                            {
                                fi.fop = FOP.FAIL;
                                Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                            }
                        }
                        break; 
	                    case REMOTE_NEED_TOBE_DELETED:
                        {
                            try
                            {
                                UpdateRemoteUserMetaFile(fi); 
                                
                            }
                            catch (Exception ex)
                            {
                                fi.fop = FOP.FAIL;
                                Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                            }
                        }
                        break;  
                    	case LOCAL_HAS_DELETED:
                            {
                                try
                                {
                                	//local as delete, so don't need to delete local, just need to udpate the remove metadata
                                    flgchanged = true;
                                    if (new File(fi.filename).exists())
                                        new File(fi.filename).delete();
                                    
                                    Config.logger.info("Delete file " + fi.filename);
                                    
                                    //reduce ref counter
                            		ReduceRefCounterAndPurgeFile(fi);
                            			//RestConnector.ReduceObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
                                    
                                    //update remote metadata
                                    UpdateRemoteUserMetaFile(fi); 
                                    
                                }
                                catch (Exception ex)
                                {
                                    fi.fop = FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
                        case REMOTE_HAS_DELETED:
                            {
                                try
                                {
                                	//remote has deleted, local have to delete
                                    flgchanged = true;

                                    if (new File(fi.filename).exists())
                                        new File(fi.filename).delete();
                                    Config.logger.info("Delete file " + fi.filename);

                                    //since remote has been updated, don't need to do anything, just clean local
                                }
                                catch (Exception ex)
                                {
                                    fi.fop = FOP.FAIL;
                                    Config.logger.error("Error to process "+fi.filename+".Error:"+ex.getMessage());
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                merged.user = m_username;

                if (flgchanged){             
                    merged.WriteToDisk(m_metafile); 
                    //fxController.MainController.readMetaDataintoTable();
                }
                    
               Config.logger.info("Finish sync process");  
               SyncStatus.SetStatus("All to update");   
            }
            catch (Exception e)
            {
            	Config.logger.fatal("Error to sync:"+e.getMessage());
                SyncStatus.SetStatus("Cannot sync with server");
            }
            //Sync Interval is milliseconds = 1/1000 seconds which means 5000 milliseconds = 5 seconds
            //Thread.sleep(5000);
            System.gc();
            Thread.sleep(m_synctime);
        }
		
	}

	//private File File(String filename) {
		// TODO Auto-generated method stub
	//	return null;
	//}

	@Override
	public void run() {
		try
		{
			StartSync();
		}
		catch(Exception e)
		{
			Config.logger.fatal("Cannot sync."+e.getMessage());
		}	
		
	}
}
