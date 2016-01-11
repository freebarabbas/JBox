package clsTypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class userMetaData  implements Metadata {
	public Date dt;
    public String user;
    public String version;
    public List<fileInfo> filelist;
    //file leve metadata - base on user
    public userMetaData(String file) throws Exception
    {
    	try
        {
        	
    		BufferedReader br = new BufferedReader(new FileReader(file));
    		String line;
    		dt=SmallFunctions.String2Date(br.readLine());
    		user=br.readLine();
    		version=br.readLine();
    		filelist=new ArrayList<fileInfo>();   	
    		while ((line = br.readLine()) != null) {  			
    			String pgid = line.substring(0, 40).trim();
    			String gid = line.substring(40, 80).trim();
                int type = Integer.parseInt(line.substring(80, 84));
                int ss = Integer.parseInt(line.substring(84, 88));						//use as file level moduler
                Date fdt = SmallFunctions.String2Date(line.substring(88, 118).trim());
                Date fla = SmallFunctions.String2Date(line.substring(118, 148).trim());
                String fhash = line.substring(148, 188).trim();
                long blength = Long.parseLong(line.substring(188, 200).trim());
                String fname = line.substring(200).trim();
                fileInfo tmp=new fileInfo(fname, gid, pgid, ss, type, fdt, fla);
                tmp.bytelength=blength;
                tmp.filehash=fhash;
                filelist.add(tmp);
    		}
    		br.close();
    	
        }
        catch (Exception e)
        {
            throw e;
        }
    	
    }
    //file level metadata - base on user
    public userMetaData(byte[] initstring) throws Exception
    {
    	try
        {
            String tmp = new String(initstring);
            String[] lines = tmp.split("\r\n|\n|\r");
            dt = SmallFunctions.String2Date(lines[0]);
            user = lines[1];
            version = lines[2];
            filelist = new ArrayList<fileInfo>();
            for(int i=3;i<lines.length;i++)
            {
            	String pgid = lines[i].substring(0, 40).trim();
    			String gid = lines[i].substring(40, 80).trim();
                int type = Integer.parseInt(lines[i].substring(80, 84));
                int ss = Integer.parseInt(lines[i].substring(84, 88));						//use as file level moduler
                Date fdt = SmallFunctions.String2Date(lines[i].substring(88, 118).trim());
                Date fla = SmallFunctions.String2Date(lines[i].substring(118, 148).trim());
                String fhash = lines[i].substring(148, 188).trim();
                long blength = Long.parseLong(lines[i].substring(188, 200).trim());
                String fname = lines[i].substring(200).trim();
                fileInfo fi=new fileInfo(fname, gid, pgid, ss, type, fdt, fla);
                fi.bytelength=blength;
                fi.filehash=fhash;
                filelist.add(fi);
            }
            
           
        }
        catch (Exception e)
        {
        	throw e;
        }
    	
    	
    }
    public userMetaData()
    {
        dt = new Date();
        user = "";
        version = "2.0";
        filelist = null;
    }
    public void SetVersionFlag(int v)
    {
    	for(fileInfo fi:filelist)
    		fi.versionflag=v;
    }
    //chunk level metadata - base on file
    public boolean GenerateFilesStructure(List<String> syncfolders) throws Exception
    {
        try
        {
            filelist = new ArrayList<fileInfo>();
            for (String folder : syncfolders)
            {
                File file=new File(folder);
                if(file.exists())
                {
                	String baseguid=SmallFunctions.GenerateGUID();
                	Path rootfd=  FileSystems.getDefault().getPath(folder);
                	BasicFileAttributes attrs = Files.readAttributes(rootfd,BasicFileAttributes.class);
                	FileTime rootla=attrs.lastAccessTime();
                	FileTime rootlw=attrs.lastModifiedTime();
                		
                	
                	fileInfo rootfi=new fileInfo(folder, baseguid, SmallFunctions.GetDummyGUID(), 0, 2, new Date(rootlw.to(TimeUnit.SECONDS)*1000), new Date(rootla.to(TimeUnit.SECONDS)*1000));
                	filelist.add(rootfi);
                	LinkedList<File> list = new LinkedList<File>();  
                	File[] files = file.listFiles();  
                    for (File file2 : files) {  
                    	String subguid=SmallFunctions.GenerateGUID();
                    	Path subfd=  FileSystems.getDefault().getPath(file2.getAbsolutePath());
                    	BasicFileAttributes subattrs = Files.readAttributes(subfd,BasicFileAttributes.class);
                    	FileTime subla=subattrs.lastAccessTime();
                    	FileTime sublw=subattrs.lastModifiedTime();
                    	if (file2.isDirectory()) {                        	
                        	fileInfo subfi=new fileInfo(file2.getAbsolutePath(), subguid, baseguid, 0, 1, new Date(sublw.to(TimeUnit.SECONDS)*1000), new Date(subla.to(TimeUnit.SECONDS)*1000));
                        	filelist.add(subfi);
                            list.add(file2);  
                        } else {  
                        	fileInfo subfi=new fileInfo(file2.getAbsolutePath(), subguid, baseguid, 0, 0, new Date(sublw.to(TimeUnit.SECONDS)*1000), new Date(subla.to(TimeUnit.SECONDS)*1000));
                        	subfi.bytelength=file2.length();
                        	filelist.add(subfi);
                        }  
                    }
                    File temp_file;  
                    while (!list.isEmpty()) {  
                        temp_file = list.removeFirst();  
                        files = temp_file.listFiles();  
                        for (File file2 : files) { 
                        	String subguid=SmallFunctions.GenerateGUID();
                        	Path subfd=  FileSystems.getDefault().getPath(file2.getAbsolutePath());
                        	BasicFileAttributes subattrs = Files.readAttributes(subfd,BasicFileAttributes.class);
                        	FileTime subla=subattrs.lastAccessTime();
                        	FileTime sublw=subattrs.lastModifiedTime();
                            if (file2.isDirectory()) {  
                            	fileInfo subfi=new fileInfo(file2.getAbsolutePath(), subguid, baseguid, 0, 1, new Date(sublw.to(TimeUnit.SECONDS)*1000), new Date(subla.to(TimeUnit.SECONDS)*1000));
                            	filelist.add(subfi);
                                list.add(file2); 
                            } else {  
                            	fileInfo subfi=new fileInfo(file2.getAbsolutePath(), subguid, baseguid, 0, 0, new Date(sublw.to(TimeUnit.SECONDS)*1000), new Date(subla.to(TimeUnit.SECONDS)*1000));
                            	subfi.bytelength=file2.length();
                            	filelist.add(subfi);  
                            }  
                        }  
                    }
                }                              
            }

            return true;
        }
        catch (Exception e)
        {
            throw e;
        }
    }    
    public String ConvertToString() throws Exception
    {
        try
        {
            StringBuilder s = new StringBuilder();
            s.append(SmallFunctions.Date2String(dt));
            s.append(System.getProperty("line.separator"));
            s.append(user).append(System.getProperty("line.separator"));
            s.append(version).append(System.getProperty("line.separator"));
            for(fileInfo fi : filelist)
            {
                if (fi.fop != FOP.LOCAL_HAS_DELETED && fi.fop != FOP.REMOTE_HAS_DELETED && fi.fop != FOP.FAIL)
                {
                    String tmp =  String.format("%-40s", fi.parentguid);
                    tmp += String.format("%-40s", fi.guid);
                    tmp += String.format("%04d", fi.type);
                    tmp += String.format("%04d", fi.status);
                    tmp += String.format("%-30s",SmallFunctions.Date2String(fi.dt));
                    tmp += String.format("%-30s",SmallFunctions.Date2String(fi.lastaction));
                    tmp += String.format("%-40s", fi.filehash);
                    tmp += String.format("%012d", fi.bytelength);
                    tmp += fi.filename;
                    s.append(tmp).append(System.getProperty("line.separator"));
                }
            }
            return s.toString();
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public byte[] ConvertToByteArray() throws Exception
    {
    	return ConvertToString().getBytes(Charset.forName("UTF-8"));
    }

    public boolean WriteToDisk(String filename) throws Exception
    {
    	try
        {
    		StringBuilder s = new StringBuilder();
            s.append(SmallFunctions.Date2String(dt));
            s.append(System.getProperty("line.separator"));
            s.append(user).append(System.getProperty("line.separator"));
            s.append(version).append(System.getProperty("line.separator"));
            for(fileInfo fi : filelist)
            {
                if (fi.fop== FOP.NONE)
                {
                    String tmp =  String.format("%-40s", fi.parentguid);
                    tmp += String.format("%-40s", fi.guid);
                    tmp += String.format("%04d", fi.type);
                    tmp += String.format("%04d", fi.status);
                    tmp += String.format("%-30s",SmallFunctions.Date2String(fi.dt));
                    tmp += String.format("%-30s",SmallFunctions.Date2String(fi.lastaction));
                    tmp += String.format("%-40s", fi.filehash);
                    tmp += String.format("%012d", fi.bytelength);
                    tmp += fi.filename;
                    s.append(tmp).append(System.getProperty("line.separator"));
                }
            }
            String ret= s.toString();
    		
    		
    		FileOutputStream out = new FileOutputStream(filename);
        	out.write(ret.getBytes(Charset.forName("UTF-8")));
        	out.close();
            return true;
        }
        catch (Exception e)
        {
            throw e;
        }
    	
    	
    }
    
    
    public void MergeWithLocal(userMetaData loc) throws Exception
    {
    	try
    	{
    		this.SetVersionFlag(1);
    		loc.SetVersionFlag(0);
        	filelist.addAll(loc.filelist);
        	Collections.sort(filelist);      	
        	Iterator<fileInfo> it = filelist.iterator();
        	fileInfo pre=null;
        	while (it.hasNext()) 
            {
        		fileInfo fi=it.next();
        		if(fi.versionflag==1) //0 is local metadata (prelocal), 1 is current fs snapshot(curlocal), 2. merge local, 3. remote, 4 merge ( final )
        		{
        			if(pre==null) // if pre is null and fi version is 1, then means new file show up in local
        			{
        				fi.fop=FOP.NEW;
        				if(fi.type==0)
        				{
        					fi.filehash=HashCalc.GetFileCityHash(fi.filename);
        					Iterator<fileInfo> it4 = loc.filelist.iterator();
    						int samehash=0;
    						fileInfo tmp4=null;
    						while(it4.hasNext())
        					{
        						tmp4=it4.next();
        						if(tmp4.filehash.compareToIgnoreCase(fi.filehash)==0)
        						{
        							samehash++;
        							break;
        						}
        					}
                            if (samehash > 0)
                            {
                            	fi.status=tmp4.status;
                            	fi.guid=tmp4.guid;
                            	fi.parentguid=tmp4.parentguid;
                            	fi.fop = FOP.COPY;
                            }
        				}
        			}
        			else // got a pair then compare the file name and type match
        			{
        				if(pre.filename.compareToIgnoreCase(fi.filename)!=0||pre.type!=fi.type)
        				{   
        					if(pre.filename.compareToIgnoreCase(fi.filename)!=0){
        						pre.versionflag=-1;
        					}
        					fi.fop=FOP.NEW;  
        					if(fi.type==0)
            					fi.filehash=HashCalc.GetFileCityHash(fi.filename);
        				}
        				else //file name match and type match, then check type 0 is object, other than that is folder =1 or root folder =2
        				{      					
        					if(fi.type==0) //object
        					{
	        					if(pre.dt.compareTo(fi.dt)==0) //check date is the same or not
	        					{
	        						fi.parentguid=pre.parentguid;
	        						fi.guid=pre.guid;
	        						fi.status=pre.status;
	        						fi.fop=FOP.ALREADYUPLOAD;
	        						fi.filehash=pre.filehash;	        						
	        					}
	        					else if(pre.dt.compareTo(fi.dt)<0)//date diff, if <0, curlocal is newer
	        					{	        						
	        						fi.filehash=HashCalc.GetFileCityHash(fi.filename);
	        						Iterator<fileInfo> it3 = loc.filelist.iterator();
	        						int sameguid=0;
	        						fileInfo tmp3=null;
	        						while(it3.hasNext())
                					{
            							tmp3=it3.next();
                						if(tmp3.guid.compareToIgnoreCase(pre.guid)==0)
                							sameguid++;
                					}
	        						if(sameguid==1){ // as long as at least one object diff 						
	        							fi.fop=FOP.REMOTE_NEED_OVERWRITE;
	        							fi.guid=pre.guid;
	        							fi.status=pre.status;
	        							fi.parentguid=pre.parentguid;
	        						}
	        						else
	        							fi.fop=FOP.BRANCH;
	        					}
	        					else if(pre.dt.compareTo(fi.dt)>0)//date diff, if <0, prelocal is newer which is impossible
	        					{	        						
	        						fi.filehash=HashCalc.GetFileCityHash(fi.filename);
	        						Iterator<fileInfo> it3 = loc.filelist.iterator();
	        						int sameguid=0;
	        						fileInfo tmp3=null;
	        						while(it3.hasNext())
                					{
            							tmp3=it3.next();
                						if(tmp3.guid.compareToIgnoreCase(pre.guid)==0)
                							sameguid++;
                					}
	        						if(sameguid==1){ // as long as at least one object diff 	    						
	        							fi.fop=FOP.LOCAL_NEED_OVERWRITE;
	        							fi.guid=pre.guid;
	        							fi.status=pre.status;
	        							fi.parentguid=pre.parentguid;
	        						}
	        						else
	        							fi.fop=FOP.BRANCH;
	        					}
        					}
        					else // folder, then keep folder
        					{
        						fi.parentguid=pre.parentguid;
        						fi.guid=pre.guid;
        						fi.status=pre.status;
        					}
            				pre.versionflag=-1; //-1 means will be removed from the list
            				pre=null;
        				}
        			}
        		}
        		else //for pre version = 0, then it should be pre local, then be pre
        		{
        			if(pre!=null) //if pre version =0, fi version =0, means pre delete, move fi to pre
        				pre.fop=FOP.LOCAL_HAS_DELETED;
        			pre=fi;
        		}       		
            }
        	if(pre!=null)
				pre.fop=FOP.LOCAL_HAS_DELETED;
        	
        	//clean out the duplicated one for comparison before
        	it = filelist.iterator();
        	while (it.hasNext()) 
            {
        		fileInfo fi=it.next();
        		if(fi.versionflag==-1)
        			it.remove();
            }
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
    }
    
    public void Merge(userMetaData snd) throws Exception
    {
        try
        {      	
        	this.SetVersionFlag(2);
        	snd.SetVersionFlag(3);
        	filelist.addAll(snd.filelist);
        	Collections.sort(filelist);      	
        	Iterator<fileInfo> it = filelist.iterator();
        	fileInfo pre=null;
        	while (it.hasNext()) 
            {
                fileInfo fi=it.next();
                if(fi.versionflag==3)
            	{
            		if(pre==null||pre.filename.compareToIgnoreCase(fi.filename)!=0||pre.type!=fi.type||pre.versionflag!=2)
            		{
            			if(pre==null)
            				fi.fop=FOP.DOWNLOAD;
            			else
            			{
            				switch(pre.fop)
                    		{
    	                		case NEW:
    	                		case BRANCH:
    	                			pre.fop=FOP.UPLOAD;
    	                			break;
    	                		case ALREADYUPLOAD:
    	                			pre.fop=FOP.REMOTE_HAS_DELETED;
    	                			break;
    	                		case REMOTE_NEED_OVERWRITE:	
    	                			if(snd.dt.compareTo(this.dt)>0)
    	                				pre.fop=FOP.REMOTE_HAS_DELETED;
    	                			else
    	                			{
    	                				pre.guid=SmallFunctions.GenerateGUID();
    	                				pre.fop=FOP.UPLOAD;
    	                			}
    	                			break;
    	                		default:
    	                			break;
                    		}
            				pre=null;
            			}
            		}
            		else
            		{
            			if(fi.type==0)
            			{
            				if(fi.dt.compareTo(pre.dt)>0)
            				{
            					
            					if(pre.fop!=FOP.LOCAL_HAS_DELETED)
            					{
            						fi.copyTo(pre);
            						pre.fop=FOP.LOCAL_NEED_OVERWRITE;            						          						
            					}
            					else
            					{
            						fi.copyTo(pre);
            						pre.fop=FOP.DOWNLOAD;
            					}
 
            				}
            				else if(fi.dt.compareTo(pre.dt)==0)
            				{
            					if(pre.fop!=FOP.LOCAL_HAS_DELETED&&pre.fop!=FOP.COPY)
            						pre.fop=FOP.NONE;
            				}
            				else
            				{
            					if(pre.fop==FOP.BRANCH)
            						pre.fop=FOP.UPLOAD;
            					else if(pre.fop!=FOP.LOCAL_HAS_DELETED)
            						pre.fop=FOP.REMOTE_NEED_OVERWRITE;            						
            				}
            			}
            			it.remove();
            			pre=null;
            		}
            		
            	}
                else
                {
                	if(pre!=null)
                	{
                		switch(pre.fop)
                		{
	                		case NEW:
	                		case BRANCH:
	                			pre.fop=FOP.UPLOAD;
	                			break;
	                		case ALREADYUPLOAD:
	                			pre.fop=FOP.REMOTE_HAS_DELETED;
	                			break;
	                		case REMOTE_NEED_OVERWRITE:	
	                			if(snd.dt.compareTo(this.dt)>0)
	                				pre.fop=FOP.REMOTE_HAS_DELETED;
	                			else
	                			{
	                				pre.guid=SmallFunctions.GenerateGUID();
	                				pre.fop=FOP.UPLOAD;
	                			}
	                			break;
	                		default:
	                			break;
                		}
                	}
                	pre=fi;
                }                          	
            }
        	if(pre!=null)
        	{
        		switch(pre.fop)
        		{
            		case NEW:
            		case BRANCH:
            			pre.fop=FOP.UPLOAD;
            			break;
            		case ALREADYUPLOAD:
            			pre.fop=FOP.REMOTE_HAS_DELETED;
            			break;
            		case REMOTE_NEED_OVERWRITE:	
            			if(snd.dt.compareTo(this.dt)>0)
            				pre.fop=FOP.REMOTE_HAS_DELETED;
            			else
            			{
            				pre.guid=SmallFunctions.GenerateGUID();
            				pre.fop=FOP.UPLOAD;
            			}
            			break;
            		default:
            			break;
        		}
        	}
        	this.SetVersionFlag(4);
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    public static userMetaData GenerateLatestFilesStructure(List<String> syncfolders) throws Exception
    {
        try
        {
        	userMetaData tmp = new userMetaData();
            tmp.GenerateFilesStructure(syncfolders);
            return tmp;
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    public String ConvertToHTML(String title)
    {
    	StringBuilder sb=new StringBuilder();
    	sb.append(title+":").append(System.getProperty("line.separator")).append("<br>");
    	for(fileInfo fi:filelist)
    	{
    		//if(fi.fop!=FOP.NONE)
    			sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-60s</b>", fi.filename).replace(' ', '-')).append(fi.fop.toString()).append(System.getProperty("line.separator")).append("<br>");
    	}   	
    	return sb.toString();
    }
}
