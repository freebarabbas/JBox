package clsUtilitues;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import clsTypes.*;
import clsRESTConnector.*;

public class sync implements Runnable {
	
	private List<String> m_syncfolders;
	private String m_metafile;
	private String m_url;
	private String m_username;
	private String m_pwd;
	private ebProxy m_pxy;
	private int m_mod;
	
	
	public sync(List<String> p_syncfolders,String p_metafile, String p_url,String p_username,String p_pwd,ebProxy p_pxy)
	{
		m_syncfolders=p_syncfolders;
		m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_mod=0;
	}
	public sync(List<String> p_syncfolders,String p_metafile, String p_url,String p_username,String p_pwd,ebProxy p_pxy,int p_mod)
	{
		m_syncfolders=p_syncfolders;
		m_metafile=p_metafile;
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_mod=p_mod;
	}

	private  boolean UpdateRemoteUserMetaFile(String tkn,String storageurl,String usermetafile,String useraccount,ebProxy pxy,fileInfo fi)
    {
        try
        {
        	Config.logger.debug("Start to update user meta file for " + fi.filename);
            RestResult rr= RestConnector.GetContainer(tkn, storageurl + "/USERMETAFILE/"+ usermetafile+"-L", pxy);
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
                rr= RestConnector.GetContainer(tkn, storageurl + "/USERMETAFILE/"+ usermetafile+"-L", pxy);
                lockbin=rr.data;

            }
            Config.logger.debug("Add lock  to update user meta file for " + fi.filename); 
            RestConnector.PutFile(tkn, storageurl + "/USERMETAFILE", usermetafile+"-L", SmallFunctions.Date2String(new Date()).getBytes(), pxy);
            rr = RestConnector.GetContainer(tkn, storageurl + "/USERMETAFILE/"+ usermetafile, pxy);
            byte[] remotebin=rr.data;
            if (remotebin == null)
            {
                userMetaData umd = new userMetaData();
                umd.user = useraccount;
                umd.filelist = new ArrayList<fileInfo>(); 
                fi.fop = FOP.NONE;
                umd.filelist.add(fi);
                RestConnector.PutFile(tkn,  storageurl + "/USERMETAFILE", usermetafile, umd.ConvertToByteArray(), pxy);
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
                				RestConnector.PutFile(tkn,  storageurl + "/USERMETAFILE", usermetafile, umd.ConvertToByteArray(), pxy);               						
                			}
                			found=true;
                			break;
                		}
                	}
                	if(found==false)
                	{
                		fi.fop=FOP.NONE;
                		umd.filelist.add(fi);
                		RestConnector.PutFile(tkn,  storageurl + "/USERMETAFILE", usermetafile, umd.ConvertToByteArray(), pxy);
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
                				RestConnector.PutFile(tkn,  storageurl + "/USERMETAFILE", usermetafile, umd.ConvertToByteArray(), pxy);               						
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
                			RestConnector.PutFile(tkn,  storageurl + "/USERMETAFILE", usermetafile, umd.ConvertToByteArray(), pxy);               						
                			break;
                		}
                	}
                }
            }
            RestConnector.DeleteFile(tkn, storageurl + "/USERMETAFILE", usermetafile+"-L", pxy);
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
	
	private  void StartSync(List<String> syncfolders,String metafile, String url,String username,String pwd,ebProxy pxy) throws Exception
    {       
        //try to get token
		Config.logger.info("Starting sync process");
        String tkn="";
        RestResult rr;
        String storageurl="";
        String usermetafile=new File(metafile).getName();
        int dotIndex=usermetafile.lastIndexOf('.');
        if(dotIndex>=0) { // to prevent exception if there is no dot
        	usermetafile=usermetafile.substring(0,dotIndex);
        }
        
        SyncStatus.SetStatus("Connecting to server");
        while (tkn == "")
        {
            rr = RestConnector.GetToken(url, username, pwd, pxy);
            if (rr.token=="")
            {
            	Config.logger.error("Cannot get token."+rr.msg);
                Thread.sleep(5000);
            }
            else{
            	Config.logger.debug("Get token:"+rr.token);
            	tkn=rr.token;
            	storageurl=rr.storageurl;
            }
        }
        SyncStatus.SetStatus("Identitying the changes");
        while (true)
        {
            try
            {
            	userMetaData local = null;
                File localmetafile=new File(metafile);
                if (localmetafile.exists())
                {
                    local = new userMetaData(metafile);
                    Config.logger.debug(local.ConvertToHTML("Last snapshot"));
                }


                userMetaData lastlocal = new userMetaData();              
                lastlocal.GenerateFilesStructure(syncfolders);
                Config.logger.debug(lastlocal.ConvertToHTML("Current snapshot"));
                if (local != null)
                {
                    lastlocal.MergeWithLocal(local);
                    Config.logger.debug(lastlocal.ConvertToHTML("Merged with last snapshot"));
                	/*Iterator<fileInfo> it1 = lastlocal.filelist.iterator();
                	while(it1.hasNext()){
                		fileInfo tmp=it1.next();
                		Iterator<fileInfo> it2 = local.filelist.iterator();
                		boolean found=false;
                		while(it2.hasNext())
                		{
                			fileInfo tmp2=it2.next();
                			if(tmp.filename.compareToIgnoreCase(tmp2.filename)==0 && tmp.type==tmp2.type)
                			{
                				found=true;
                				if(tmp.type== 0)
                				{
                					int sameguid=0;
                					Iterator<fileInfo> it3 = local.filelist.iterator();
                					while(it3.hasNext())
                					{
                						fileInfo tmp3=it3.next();
                						if(tmp3.guid.compareToIgnoreCase(tmp2.guid)==0)
                							sameguid++;
                					}
                					if(sameguid==1)
                					{
                						if(tmp.dt.compareTo(tmp2.dt)==0)
                						{
                							tmp.filehash=tmp2.filehash;
                							tmp.fop=FOP.ALREADYUPLOAD;
                						}
                						else
                						{
                							tmp.filehash=HashCalc.GetFileMd5Hash(tmp.filename);
                							tmp.fop=FOP.REMOTE_NEED_OVERWRITE;
                						}
                						tmp.status=tmp2.status;
                                        tmp.guid=tmp2.guid;
                                        tmp.parentguid=tmp2.parentguid;
                					}
                					else
                					{
                						tmp.filehash=HashCalc.GetFileMd5Hash(tmp.filename);
                						Iterator<fileInfo> it4 = local.filelist.iterator();
                						int samehash=0;
                						fileInfo tmp4=null;
                						while(it4.hasNext())
                    					{
                							tmp4=it4.next();
                    						if(tmp4.filehash.compareToIgnoreCase(tmp.filehash)==0)
                    							samehash++;
                    					}
                                        if (samehash > 0 )
                                        {
                                            tmp.status=tmp4.status;
                                            tmp.guid=tmp4.guid;
                                            tmp.parentguid=tmp4.parentguid;
                                        }
                                        else//multi file shared a same object, need branch
                                        {
                                        	tmp.guid = SmallFunctions.GetDummyGUID();
                                        	tmp.fop = FOP.BRANCH;
                                        }
                					}
                				}
                				else
                				{
                					 tmp.status=tmp2.status;
                                     tmp.guid=tmp2.guid;
                                     tmp.parentguid=tmp2.parentguid;
                				}
                				break;
                			}                			
                		}
                		if(found==false)
            			{
            				if (tmp.type == 0)
                            {
                                tmp.filehash = HashCalc.GetFileMd5Hash(tmp.filename);
                                Iterator<fileInfo> it4 = local.filelist.iterator();
        						int samehash=0;
        						fileInfo tmp4=null;
        						while(it4.hasNext())
            					{
            						tmp4=it4.next();
            						if(tmp4.filehash.compareToIgnoreCase(tmp.filehash)==0)
            							samehash++;
            					}
                                if (samehash > 0)
                                {
                                	tmp.status=tmp4.status;
                                    tmp.guid=tmp4.guid;
                                    tmp.parentguid=tmp4.parentguid;
                                    tmp.fop = FOP.COPY;
                                }
                                else
                                	tmp.fop = FOP.NEW;
                            }
                            else
                            {                                    
                            	tmp.fop = FOP.NEW;
                            }
            			}
                	}
                	
                	Iterator<fileInfo> it4 = local.filelist.iterator();
                	while(it4.hasNext()){
                		fileInfo tmp=it4.next();
                		Iterator<fileInfo> it2 = lastlocal.filelist.iterator();
                		boolean found=false;
                		while(it2.hasNext())
                		{
                			fileInfo tmp2=it2.next();
                			if(tmp.filename.compareToIgnoreCase(tmp2.filename)==0 && tmp.type==tmp2.type)
                			{
                				found=true;
                				break;
                			}
                		}
                		if(found==false)
                		{
                			fileInfo tmpfi=tmp.copy();
                			tmpfi.fop=FOP.LOCAL_HAS_DELETED;
                			
                			lastlocal.filelist.add(tmpfi);
                		}
                	}*/
                	
                	
                }

                boolean flgchanged = false;

                userMetaData merged;

                SyncStatus.SetStatus("Getting user information from server");
                rr=RestConnector.GetContainer(tkn, storageurl + "/USERMETAFILE/"+ usermetafile, pxy);
                byte[] remotebin=rr.data;
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
                	if (fi.type == 1 || fi.type == 2)//folder
                    {
                        if(fi.fop == FOP.NEW)
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
                            UpdateRemoteUserMetaFile(tkn, storageurl, usermetafile, username, pxy, fi);  
                        }
                        fi.fop=FOP.NONE;
                        continue;
                    }
                    switch (fi.fop)
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
                                        //not share the same object for multi versions purpose. just copy the object in server side.

                                        String desguid = SmallFunctions.GenerateGUID();
                                        String srcguid = fi.guid;
                                        fi.guid = desguid;

                                        long dsize = 0;
                                        RestConnector.PutContainer(tkn, storageurl + "/" + fi.guid,pxy);
                                        rr=RestConnector.GetContainer(tkn, storageurl+"/"+srcguid+"/metadata", pxy);
                                        byte[] filedata = rr.data;
                                        fileMetadataWithVersion fmds = new fileMetadataWithVersion(filedata);
                                        Collections.sort(fmds.data);
                                        RestConnector.PutFile(tkn, storageurl+"/"+fi.guid, "metadata", fmds.data.get(0).ConvertToByteArray(), pxy);
                                        
                                        for (int k=0;k<fmds.data.get(0).data.size();k++)
                                        {
                                            chunk c=fmds.data.get(0).data.get(k);
                                            RestConnector.CopyFile(tkn, "/" + srcguid + "/" + c.hashvalue, storageurl+"/"+fi.guid+"/"+c.hashvalue, pxy);
                                            dsize = dsize + c.end - c.start + 1;
                                        }
                                        Config.logger.info("File:COPY----" + fi.filename);;
                                    }
                                    
                                    UpdateRemoteUserMetaFile(tkn, storageurl, usermetafile, username, pxy, fi);   
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
                                        
                                    	rr=RestConnector.GetContainer(tkn, storageurl+"/"+fi.guid+"/metadata", pxy);
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
                                                byte[] temp = RestConnector.GetContainer(tkn, storageurl+"/"+fi.guid+"/"+c.hashvalue, pxy).data;
                                                ht.put(c.hashvalue, temp.clone());
                                                System.arraycopy(temp, 0, realdata, (int)c.start, temp.length);
                                                downloadsize +=temp.length;
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
                                        RestConnector.PutContainer(tkn, storageurl + "/" + fi.guid, pxy);
                                        fileMetadata fmd = fileMetadata.GetMetadata(fi.filename,m_mod,Config.fixedchunksize,Config.ct);
                                        fileMetadataWithVersion fmds = new fileMetadataWithVersion();
                                        fmds.data.add(fmd);
                                        RestConnector.PutFile(tkn, storageurl + "/" + fi.guid, "metadata", fmds.ConvertToByteArray(), pxy);
                                        byte[] filedata = Files.readAllBytes(new File(fi.filename).toPath());
                                        fmd.data.size();
                                        long dsize = 0;
                                        Hashtable<String, String> ht = new Hashtable<String, String>();
                                        for(chunk c : fmd.data)
                                        {
                                            if (ht.get(c.hashvalue)==null)
                                            {
                                                byte[] tmp = new byte[(int)(c.end - c.start + 1)];
                                                System.arraycopy(filedata, (int)c.start, tmp, 0, tmp.length);
                                                RestConnector.PutFile(tkn, storageurl + "/" + fi.guid, c.hashvalue,tmp ,pxy);
                                                ht.put(c.hashvalue,"1");
                                                uploadsize+=tmp.length;
                                            }
                                            dsize = dsize + c.end - c.start + 1;
                                        }
                                    }
                                    
                                    Config.logger.info("Uploaded " + fi.filename);
                                    UpdateRemoteUserMetaFile(tkn, storageurl, usermetafile, username, pxy, fi);
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
                                    byte[] filedata = RestConnector.GetContainer(tkn, storageurl+"/"+fi.guid+"/metadata", pxy).data;
                                    fileMetadataWithVersion fmd = new fileMetadataWithVersion(filedata);
                                    Collections.sort(fmd.data);
                                    int lastversion=fmd.data.size();
                                    byte[] realdata = new byte[(int)fmd.data.get(lastversion-1).byteslength];
                                    long dsize = 0;
                                    Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
                                    fileMetadata localfmd = fileMetadata.GetMetadata(fi.filename, m_mod,Config.fixedchunksize,Config.ct);
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
                                            byte[] temp = RestConnector.GetContainer(tkn, storageurl +"/"+fi.guid+"/"+c.hashvalue, pxy).data;
                                            System.arraycopy(temp, 0, realdata, (int)c.start, temp.length);
                                            ht.put(c.hashvalue, temp);
                                            downloadsize+=temp.length;
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
                                    byte[] remotefilemetadata = RestConnector.GetContainer(tkn, storageurl + "/"+fi.guid+"/metadata", pxy).data;
                                    fileMetadataWithVersion fmds = new fileMetadataWithVersion(remotefilemetadata);
                                    fileMetadata fmd = fileMetadata.GetMetadata(fi.filename,m_mod, Config.fixedchunksize,Config.ct);
                                    fmds.data.add(fmd);
                                    RestConnector.PutFile(tkn, storageurl + "/" + fi.guid, "metadata",  fmds.ConvertToByteArray(), pxy);                                   
                                    byte[] filedata = Files.readAllBytes(new File(fi.filename).toPath());
                                    fmd.data.size();
                                    long dsize = 0;
                                    Hashtable<String, String> ht = new Hashtable<String, String>();
                                    int uploadsize=0;
                                    for(chunk c : fmd.data)
                                    {
                                        if (ht.get(c.hashvalue)==null)
                                        {
                                            byte[] tmp = new byte[(int)(c.end - c.start + 1)];
                                            System.arraycopy(filedata, (int)c.start, tmp, 0, tmp.length);
                                            RestConnector.PutFile(tkn, storageurl + "/" + fi.guid, c.hashvalue,tmp ,pxy);
                                            ht.put(c.hashvalue,"1");
                                            uploadsize+=tmp.length;
                                        }
                                        dsize = dsize + c.end - c.start + 1;
                                    }                                   
                                    Config.logger.info("Overwrite remote " + fi.filename);
                                    UpdateRemoteUserMetaFile(tkn, storageurl, usermetafile, username, pxy, fi);
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
                            	UpdateRemoteUserMetaFile(tkn, storageurl, usermetafile, username, pxy, fi);  
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
                                    UpdateRemoteUserMetaFile(tkn, storageurl, usermetafile, username, pxy, fi);  
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
                merged.user = username;

                if (flgchanged){             
                    merged.WriteToDisk(metafile); 
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
			StartSync(m_syncfolders, m_metafile, m_url, m_username, m_pwd, m_pxy);
		}
		catch(Exception e)
		{
			Config.logger.fatal("Cannot sync."+e.getMessage());
		}		
	}
    
}
