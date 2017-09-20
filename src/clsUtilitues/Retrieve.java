package clsUtilitues;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import clsTypes.*;
import clsCompExtract.ZipProcess;
//import clsCompExtract.ZipProcess;
import clsRESTConnector.*;

public class Retrieve {
	//private List<String> m_syncfolders;
	//private String m_metafile;
	private String m_url;
	private String m_username;
	private String m_pwd;
	private ebProxy m_pxy;
	private String m_tkn;
	private String m_storageurl;
	private String m_usercontainer;
	private String m_level;
	private String m_guid;
	private String m_name;
	private int m_version=0;
	private String m_containername;
	private static long l_buffer=1*1024*1024*1024;
	
	/*
	public Retrieve(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_guid)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_guid=p_guid;
	}
	*/
	
	public Retrieve(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_guid, String p_name, String p_containername)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_guid=p_guid;
		m_name=p_name;
		m_containername=p_containername;
	}
	
	/*
	public Retrieve(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_guid, int p_version)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_guid=p_guid;
		m_version=p_version;
	}
	*/
	
	public Retrieve(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_guid, int p_version, String p_name, String p_containername)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_guid=p_guid;
		m_name=p_name;
		m_version=p_version;
		m_containername=p_containername;
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
	
	@SuppressWarnings("resource")
	public  void StartRetrieve() throws Exception
	{
		
		SyncStatus.SetStatus("Connecting to server");
		RestResult rr=null;
		if(GetToken()==true)
        {
			Config.logger.debug("Receiving token from server");
			int dotIndex=m_username.lastIndexOf(':');
	        if(dotIndex>=0)
	        	m_usercontainer=m_storageurl+"/"+m_username.substring(dotIndex+1);
	        else
	        	if(m_containername.isEmpty() && m_containername == null){m_usercontainer=m_storageurl+"/"+m_username;}
	        	else{m_usercontainer=m_storageurl+"/"+m_containername;}
			
			
        	if (m_level.equalsIgnoreCase("c")){
        		int intCol=0;
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
	                
	                
	                Iterator<fileInfo> itcol = tmpumd.filelist.iterator();
	                while(itcol.hasNext())
	                {
	                	fileInfo tmpcol=itcol.next();
	                	if (tmpcol.type==0 && tmpcol.guid.equalsIgnoreCase(m_guid)) {
		                	if (tmpcol.filename.length() > intCol){intCol=tmpcol.filename.length();}
	                	}
	                }
	                
	                String strDash="-";
	                for(int i=0; i<(intCol+124+6); i++)
	                {strDash = strDash + "-";}

	                System.out.println(strDash);
	                System.out.println(String.format("|%-"+intCol+"s|%-32s|%-32s|%-20s|%-20s|%-20s|" , "File Name", "File GUID", "File Content Hash", "Create Time", "Update Time", "(Byte)" ));
	                
	                
	                Iterator<fileInfo> it = tmpumd.filelist.iterator();
	                while(it.hasNext())
	                {
	                	fileInfo tmp=it.next();
	                	if (tmp.type==0 && tmp.guid.equalsIgnoreCase(m_guid)) {
		                	String CreateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.dt);
		                	String UpdateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.lastaction);
		                	String ByteLength = String.valueOf(tmp.bytelength);
		                	System.out.println(strDash);
			                System.out.println(String.format("|%-"+intCol+"s|%-32s|%-32s|%-20s|%-20s|%-20s|" , tmp.filename.toString(), tmp.guid.toString(), tmp.filehash.toString(), CreateTime, UpdateTime, ByteLength));
	                	}
	                }
	                System.out.println(strDash);
	            }
        		if (!m_guid.equalsIgnoreCase("")&&(intCol!=0)){
        			long downloadsize=0;
        			rr=null;
        			System.out.print("Getting chunk level metadata ... ");
                	rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+m_guid, m_pxy);
                	byte[] filedata = rr.data;
                    fileMetadataWithVersion fmd = new fileMetadataWithVersion(filedata);
                    Collections.sort(fmd.data);
                    boolean bolFileExistOverwrite = false;
                    File fdownload = new File(m_name);
                    if (fdownload.exists()){
                    	System.out.println(m_name + " exist, Would you like to overwrite it <true/false> ?");
                		//@SuppressWarnings("resource")
						Scanner scanner = new Scanner(System.in);
                		if(scanner.nextBoolean()==true) {
                			bolFileExistOverwrite = true;
                		    System.out.println("Will overwrite it");
                		} else {
                		    System.out.println("Skip Download");
                		    return;
                		}
                    }

                    int lastversion=0;
                    if (m_version==0){lastversion=fmd.data.size();}
                    else{lastversion=m_version;}
                    
                    if ( fmd.data.get(lastversion-1).byteslength > l_buffer ) {   
                        long dsize = 0;
                    	System.out.println("Will download file size:" + fmd.data.get(lastversion-1).byteslength);
                    	System.out.print("\n");
                    	FileOutputStream out = new FileOutputStream(m_name+".tmp", true);
                    	try{
		                    for (chunk c : fmd.data.get(lastversion-1).data)
		                    {
		                        long lngtemp = 0;

	                            byte[]  temp=RestConnector.GetContainer(m_tkn, m_usercontainer+"/c"+ Integer.toString(c.flag) +c.hashvalue, m_pxy).data;
	                        	lngtemp = temp.length;
	                            downloadsize +=temp.length;
	                            if( (c.flag & 1) == 1) //compressed chunk
	                            {
	                            	temp=ZipProcess.unzip(temp);
	                            }
	                            
	                            out.write(temp);

		                        dsize =dsize + c.end - c.start + 1;
		                        double dbpercentage = (double)dsize / (double)fmd.data.get(lastversion-1).byteslength;
		                        DecimalFormat percentFormat= new DecimalFormat("#.##%");
		                        //put the cursor back to the beginning of line, if output line over screen then won't work
		                        String strinfo = "\rDownloaded:" + percentFormat.format(dbpercentage) + " -- Unzip(Chunk):"+ lngtemp + "/" + (c.end - c.start + 1) + "; Total(File): " + dsize + "/" + fmd.data.get(lastversion-1).byteslength + "      ";
		                        System.out.print(strinfo);
		                    }
		                    System.out.print("\n");    
                         }finally {
                            out.close();
                            File ftmp = new File(m_name+".tmp");
                        	if (bolFileExistOverwrite){
                            	if(ftmp.renameTo(fdownload)){
        		                	Config.logger.info("Downloaded at: " + m_name + " with Original Size:"+ dsize +" Bytes, Download Size:" + downloadsize + " Bytes");
        		                	System.out.println("Downloaded at: " + m_name + " with Original Size:"+ dsize +" Bytes, Download Size:" + downloadsize + " Bytes");
                            	}else{
        		                	Config.logger.info("file " + m_name + ".tmp rename to "+ m_name + "Fail !");
        		                	System.out.println("file " + m_name + ".tmp rename to "+ m_name +" Fila !");
                            	}
                            }else{
    		                	Config.logger.info("file " + m_name + " exist ! Download Fail !");
    		                	System.out.println("file " + m_name + " exist ! Download Fila !");
                            }
                         }
                    }
                    else
                    {
                    	//if file size less than l_buffer which is 1G, 
                    	//we can put into byte array ( memory ) and write it out.
                        byte[] realdata = new byte[(int) fmd.data.get(lastversion-1).byteslength];
                        fmd.data.get(lastversion-1).data.size();
                       
                        long dsize = 0;
                        Hashtable<String, byte[]> ht = new Hashtable<String, byte[]>();
                        System.out.print("\n");
                    
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
	                        double dbpercentage = (double)dsize / (double)fmd.data.get(lastversion-1).byteslength;
	                        DecimalFormat percentFormat= new DecimalFormat("#.##%");
	                        System.out.print("\rDownload%:" + percentFormat.format(dbpercentage) + " to the memory (ByteArray) ");
	                    }
	                    
	                    ht.clear();
	                    if (!m_name.equalsIgnoreCase("")){
		                    FileOutputStream out = new FileOutputStream(m_name+".tmp", true);
		                	out.write(realdata);
		                	out.close();
		                	System.out.println("and Write out file: " + m_name + " from memory (ByteArray) !" );
                            File ftmp = new File(m_name+".tmp");
                        	if (bolFileExistOverwrite){
                            	if(ftmp.renameTo(fdownload)){
        		                	Config.logger.info("Downloaded at: " + m_name + " with Original Size:"+ dsize +" Bytes, Download Size:" + downloadsize + " Bytes");
        		                	System.out.println("Downloaded at: " + m_name + " with Original Size:"+ dsize +" Bytes, Download Size:" + downloadsize + " Bytes");
                            	}else{
        		                	Config.logger.info("file " + m_name + ".tmp rename to "+ m_name + "Fail !");
        		                	System.out.println("file " + m_name + ".tmp rename to "+ m_name +" Fila !");
                            	}
                            }else{
    		                	Config.logger.info("file " + m_name + " exist ! Download Fail !");
    		                	System.out.println("file " + m_name + " exist ! Download Fila !");
                            }
	                    }
	                    else{
		                    FileOutputStream out = new FileOutputStream(strFileName);
		                	out.write(realdata);
		                	out.close();
	                        File ftmp = new File(m_name+".tmp");
                        	if (bolFileExistOverwrite){
                            	if(ftmp.renameTo(fdownload)){
        		                	Config.logger.info("Downloaded at: " + m_name + " with Original Size:"+ dsize +" Bytes, Download Size:" + downloadsize + " Bytes");
        		                	System.out.println("Downloaded at: " + m_name + " with Original Size:"+ dsize +" Bytes, Download Size:" + downloadsize + " Bytes");
                            	}else{
        		                	Config.logger.info("file " + m_name + ".tmp rename to "+ m_name + "Fail !");
        		                	System.out.println("file " + m_name + ".tmp rename to "+ m_name +" Fila !");
                            	}
                            }else{
    		                	Config.logger.info("file " + m_name + " exist ! Download Fail !");
    		                	System.out.println("file " + m_name + " exist ! Download Fila !");
                            }
	                    }

                    }
                    
        		}else{
        			System.out.println("missing file guid !!!");
        			return;
        		}
       
            }else{
            	System.out.println("missing chunk level !!! < c: chunk level >");
            	return;
            }
        }	
        else{
        	System.out.println("can't get the tokent");
        	return;
        }
		
	}

}

