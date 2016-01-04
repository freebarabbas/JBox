package clsUtilitues;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
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
import clsRESTConnector.*;

public class SyncV2 implements Runnable {
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
	
	
	public SyncV2(List<String> p_syncfolders,String p_metafile, String p_url,String p_username,String p_pwd,ebProxy p_pxy,int p_mod)
	{
		m_syncfolders=p_syncfolders;
		m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_mod =p_mod;
		m_initialtype=1;
	}
	
	public SyncV2(List<String> p_syncfolders,String p_metafile,String p_url, String p_storageurl,String p_username,String p_tkn,ebProxy p_pxy,int p_mod)
	{
		m_syncfolders=p_syncfolders;
		m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_storageurl=p_storageurl;
		m_tkn=p_tkn;
		m_pxy=p_pxy;
		int dotIndex=p_username.lastIndexOf(':');
        if(dotIndex>=0)
        	m_usercontainer=m_storageurl+"/"+p_username.substring(dotIndex+1);
        else
        	m_usercontainer=m_storageurl+"/"+p_username;
		m_initialtype=2;
		m_mod=p_mod;
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
					if(lines[i].startsWith("c") && !lines[i].endsWith("_d"))
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
            	userMetaData umd = new userMetaData(remotebin);
                umd.dt = new Date();
                if (fi.fop == FOP.UPLOAD || fi.fop==FOP.COPY)
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	boolean found=false;
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next();
                		if(tmp.filename.compareToIgnoreCase(fi.filename)==0 )
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
                	if(found==false)
                	{
                		fi.fop=FOP.NONE;
                		umd.filelist.add(fi);
                		RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);
                	}
                }
                else if (fi.fop == FOP.REMOTE_NEED_OVERWRITE)
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next();
                		if(tmp.filename.compareToIgnoreCase(fi.filename)==0 )
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
                else if(fi.fop == FOP.LOCAL_HAS_DELETED)
                {
                	Iterator<fileInfo> it1 = umd.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next();
                		if(tmp.filename.compareToIgnoreCase(fi.filename)==0 )
                		{
                			umd.filelist.remove(tmp);
                			RestConnector.PutFile(m_tkn,  m_usercontainer, "USERMETAFILE", umd.ConvertToByteArray(), m_pxy);              						
                			break;
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
	
	private  void StartSync() throws Exception
	{
		Config.logger.info("Starting sync process");
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
		SyncStatus.SetStatus("Identitying the changes");
		RestResult rr=null;
		Set<String> cc=GetCurrentChunk(); //get object list under user container from swift
        while (true)
        {
            try
            {
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

                boolean flgchanged = false;

                userMetaData merged;

                SyncStatus.SetStatus("Getting user information from server");
                rr=RestConnector.GetContainer(m_tkn, m_usercontainer + "/USERMETAFILE", m_pxy);
                byte[] remotebin=null;
                if(rr.httpcode==HttpURLConnection.HTTP_NOT_FOUND)
                {
                	RestConnector.PutContainer(m_tkn, m_usercontainer, m_pxy);
                }
                else
                	remotebin=rr.data;
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
                    Config.logger.debug(tmpumd.ConvertToHTML("Remote snapshot"));
                    lastlocal.Merge(tmpumd);
                    Config.logger.debug(lastlocal.ConvertToHTML("Merged with remote metafile"));
                }
                merged = lastlocal;
                               
                Iterator<fileInfo> it = merged.filelist.iterator();
                while(it.hasNext())
                {
                    fileInfo fi=it.next();
                    Config.logger.debug("Start to process:"+fi.ConvertToHTML());
                    SyncStatus.SetStatus(fi.filename,"start to sync","Syncing "+ fi.filename);
                	if (fi.type == 1 || fi.type == 2)//2 root folder, 1 sub folder
                    {
                        if(fi.fop == FOP.NEW)//fi.fop NEW, DOWNLOAD, UPLOAD and NONE
                        	flgchanged=true;
                		if (fi.fop == FOP.DOWNLOAD)
                        {
                        	File dir = new File(fi.filename);
                        	dir.mkdir();
                            flgchanged = true;
                            Config.logger.info("Create local Folder:"+fi.filename);
                        }
                        if (fi.fop == FOP.UPLOAD)
                        {
                            flgchanged = true;
                            UpdateRemoteUserMetaFile(fi);  
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
                                    	f=it1.next();
                                    	if(f.filehash.compareToIgnoreCase( fi.filehash)==0 && (f.fop==FOP.LOCAL_HAS_DELETED || f.fop==FOP.REMOTE_HAS_DELETED ))
                                    	{
                                    		found=true;
                                    		break;
                                    	}
                                    }
                                    
                                    if (found == true)//it is a move action
                                    {
                                        fi.guid = f.guid;
                                        fi.parentguid = f.parentguid;
                                        fi.status = f.status;
                                        Config.logger.info("File:Move/Rename----" + fi.filename +" from "+ f.filename);
                                    }
                                    else
                                    {
                                        //not share the same object for multi-versions purpose. just copy the object in server side.

                                        String desguid = SmallFunctions.GenerateGUID();
                                        String srcguid = fi.guid;
                                        fi.guid = desguid;

                                        rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+srcguid, m_pxy);
                                        byte[] filedata = rr.data;
                                        fileMetadataWithVersion fmds = new fileMetadataWithVersion(filedata);
                                        Collections.sort(fmds.data);
                                        RestConnector.PutFile(m_tkn, m_usercontainer, "f"+fi.guid, fmds.data.get(0).ConvertToByteArray(), m_pxy);
                                        
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
                                    int uploadsize=0;
                                    if (needupload)
                                    {
                                        fileMetadata fmd = fileMetadata.GetMetadata(fi.filename, m_mod,Config.divider,Config.refactor,Config.fixedchunksize,Config.ct);                                                                              
                                        byte[] filedata = Files.readAllBytes(new File(fi.filename).toPath());
                                        fmd.data.size();
                                        long dsize = 0;
                                        Hashtable<String, String> ht = new Hashtable<String, String>();
                                        for(chunk c : fmd.data)
                                        {
                                            if (ht.get(c.hashvalue)==null)
                                            {
                                               int tmpf=-1;
                                               
                                            	if(cc.contains("c1"+c.hashvalue)){
                                            		tmpf=1;
                                            		if (Config.refcounter == 1) {
                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c1"+c.hashvalue, m_pxy);
                                            		}
                                            	}
                                            	else if (cc.contains("c0"+c.hashvalue)){
                                            		tmpf=0;
                                            		if (Config.refcounter == 1){
                                            			RestConnector.AddObjectRefCount(m_tkn, m_usercontainer, "c0"+c.hashvalue, m_pxy);
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
                                                	RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
                                                	ht.put(c.hashvalue,"1");
                                                	uploadsize+=ztmp.length;
                                                	
                                                }
                                                else
                                                {
                                                	c.flag= c.flag & ~1;
                                                	RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
                                                	ht.put(c.hashvalue,"0");
                                                	uploadsize+=tmp.length;
                                                }
                                                cc.add("c"+ Integer.toString(c.flag) + c.hashvalue);
                                                
                                                
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
                                        fileMetadataWithVersion fmds = new fileMetadataWithVersion();
                                        fmds.data.add(fmd);
                                        RestConnector.PutFile(m_tkn, m_usercontainer, "f"+fi.guid, fmds.ConvertToByteArray(), m_pxy);
                                    }
                                    
                                    Config.logger.info("Uploaded " + fi.filename);
                                    UpdateRemoteUserMetaFile( fi);
                                    Date dte=new Date();
                                    Config.logger.info("Cost----Times:" + (dte.getTime() - dts.getTime()) + " MillSeconds    Traffic:" + uploadsize + "/" + fi.bytelength);
                                     
                                    fi.fop = FOP.NONE;

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
                                    fileMetadata localfmd = fileMetadata.GetMetadata(fi.filename, m_mod,Config.divider,Config.refactor,Config.fixedchunksize,Config.ct);
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
                                    }
                                    //get local metadata but use lasttest mod on server
                                    fileMetadata fmd = fileMetadata.GetMetadata(fi.filename, m_mod, Config.divider,Config.refactor,Config.fixedchunksize,Config.ct);
                                                                     
                                    byte[] filedata = Files.readAllBytes(new File(fi.filename).toPath());
                                    fmd.data.size();
                                    long dsize = 0;
                                    Hashtable<String, String> ht = new Hashtable<String, String>();
                                    int uploadsize=0;
                                    for(chunk c : fmd.data)
                                    {
                                    	 if (ht.get(c.hashvalue)==null)
                                         {
                                    		 int tmpf=-1;
                                         	if(cc.contains("c1"+c.hashvalue))
                                         		tmpf=1;
                                         	else if (cc.contains("c0"+c.hashvalue))
                                         		tmpf=0;
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
                                             	RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,ztmp ,m_pxy);
                                             	ht.put(c.hashvalue,"1");
                                             	uploadsize+=ztmp.length;
                                             }
                                             else
                                             {
                                             	c.flag= c.flag & ~1;
                                             	RestConnector.PutFile(m_tkn, m_usercontainer ,"c"+ Integer.toString(c.flag) + c.hashvalue,tmp ,m_pxy);
                                             	ht.put(c.hashvalue,"0");
                                             	uploadsize+=tmp.length;
                                             }
                                             cc.add("c"+ Integer.toString(c.flag) + c.hashvalue);
                                         }
                                         else
                                         {
                                         	if(Integer.parseInt(ht.get(c.hashvalue))==1)
                                         		c.flag=c.flag|1;
                                         	else
                                         		c.flag= c.flag & ~1;                                            	                                            	
                                         }
                                    	/*if (ht.get(c.hashvalue)==null)
                                        {
                                            byte[] tmp = new byte[(int)(c.end - c.start + 1)];
                                            System.arraycopy(filedata, (int)c.start, tmp, 0, tmp.length);
                                            RestConnector.PutFile(m_tkn, m_usercontainer, "c"+c.hashvalue,tmp ,m_pxy);
                                            ht.put(c.hashvalue,"1");
                                            uploadsize+=tmp.length;
                                        }*/
                                        dsize = dsize + c.end - c.start + 1;
                                    } 
                                    fmds.data.add(fmd);
                                    RestConnector.PutFile(m_tkn, m_usercontainer , "f" + fi.guid,  fmds.ConvertToByteArray(), m_pxy);  
                                    
                                    Config.logger.info("Overwrite remote " + fi.filename);
                                    UpdateRemoteUserMetaFile(fi);
                                    Date dte=new Date();
                                    Config.logger.info("Cost----Times:" + (dte.getTime() - dts.getTime()) + " MillSeconds    Traffic:" + uploadsize + "/" + fi.bytelength);
                                      
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
                for (fileInfo fi : merged.filelist)
                {
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
                    switch (fi.fop)
                    {
                        case LOCAL_HAS_DELETED:
                            {
                                try
                                {
                                    flgchanged = true;
                                    if (new File(fi.filename).exists())
                                        new File(fi.filename).delete();
                                    Config.logger.info("Delete file " + fi.filename);
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

                                    flgchanged = true;

                                    if (new File(fi.filename).exists())
                                        new File(fi.filename).delete();

                                    Config.logger.info("Delete file " + fi.filename);

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

            Thread.sleep(5000);
        }
		
	}

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
