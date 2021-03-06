package clsUtilitues;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;

import clsTypes.*;
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

	private String m_usercontainer;
	private String m_level;
	private String m_guid;
	private String m_containername;
	
	public Query(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_containername)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_containername=p_containername;
	}
	
	public Query(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_containername)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_containername=p_containername;
		
	}
	
	public Query(String p_url,String p_username,String p_pwd,ebProxy p_pxy, String p_level, String p_guid, String p_containername)
	{
		m_url=p_url;
		m_username=p_username;
		m_pwd=p_pwd;
		m_pxy=p_pxy;
		m_level=p_level;
		m_guid=p_guid;
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
	
	public  void StartQuery() throws Exception
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
	                
	                int intCol=0;
	                Iterator<fileInfo> itcol = tmpumd.filelist.iterator();
	                
	                while(itcol.hasNext())
	                {
	                	fileInfo tmpcol=itcol.next();
	                	if (tmpcol.type==0) {
		                	if (tmpcol.filename.length() > intCol){intCol=tmpcol.filename.length();}
	                	}
	                }
		                
		            if (intCol != 0){
		                
		                String strDash="-";
		                for(int i=0; i<(intCol+124+6); i++)
		                {strDash = strDash + "-";}
	
		                System.out.println(strDash);
		                System.out.println(String.format("|%-"+intCol+"s|%-32s|%-32s|%-20s|%-20s|%-20s|" , "File Directory Name", "File GUID", "File Content Hash", "Create Time", "Update Time", "Size(Byte)" ));
		                Iterator<fileInfo> it = tmpumd.filelist.iterator();
		                while(it.hasNext())
		                {
		                	fileInfo tmp=it.next();
		                	if (tmp.type==0) {
			                	String CreateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.dt);
			                	String UpdateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(tmp.lastaction);
			                	String ByteLength = String.valueOf(tmp.bytelength);
			                	System.out.println(strDash);
				                System.out.println(String.format("|%-"+intCol+"s|%-32s|%-32s|%-20s|%-20s|%-20s|" , tmp.filename.toString(), tmp.guid.toString(), tmp.filehash.toString(), CreateTime, UpdateTime, ByteLength));
		                	}
		                }
		                System.out.println(strDash);
		                //Config.logger.debug(lastlocal.ConvertToHTML("Merged with remote metafile"));
		                
		                if (m_guid != null){
		                	if (!m_guid.equalsIgnoreCase("") && !m_guid.isEmpty() ){
		                	    System.out.print("Display f"+m_guid+ " all the contnet");
		                	    rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+m_guid, m_pxy);
		                	    System.out.print(new String(rr.data, "UTF-8"));
		                	}
		                }
	                }
	                else{
	                	System.out.println("File Level Metadata - USERMETAFILE got no record !");
	                }
	                
	            }
        	}else if (m_level.equalsIgnoreCase("c")){

        		int intCol=0;
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
	                
	                //int intCol=0;
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
	                if (intCol==0){
	                	System.out.println("Find Nothing about this "+ m_guid +" !");
	                }
	                else{
						System.out.println(String.format("|%-"+intCol+"s|%-32s|%-32s|%-20s|%-20s|%-20s|" , "File Directory Name", "File GUID", "File Content Hash", "Create Time", "Update Time", "Size(Byte)" ));
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
	            }
        		
        		if (!m_guid.equalsIgnoreCase("")&&(intCol!=0)){
        						
                	rr=RestConnector.GetContainer(m_tkn, m_usercontainer+"/f"+m_guid, m_pxy);
                	byte[] filedata = rr.data;
                    fileMetadataWithVersion fmd = new fileMetadataWithVersion(filedata);
                    Collections.sort(fmd.data);
                    
                    int lastversion=fmd.data.size();
                    
	                String strDash="-";
	                for(int i=0; i<(102+2); i++)
	                {strDash = strDash + "-";}
	                
	                System.out.print("\n");
	                System.out.println(strDash);
	                System.out.println(String.format("|%-10s|%-20s|%-30s|%-40s|" , "Version", "TimeStamp", "Size", "File Hash" ));
                    
                    String TimeStamp="";
                    long lngVersion=0;
                    for (int i=0; i<lastversion; i++){
                    	TimeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(fmd.data.get(i).dt.getTime());
                    	lngVersion=i+1;
	                	System.out.println(strDash);
		                System.out.println(String.format("|%-10s|%-20s|%-30s|%-40s|" , lngVersion, TimeStamp, fmd.data.get(i).byteslength, fmd.data.get(i).hashcode));
                    }
                    System.out.println(strDash);

        		}else{
        			System.out.println("missing file guid !!!");
        			return;
        		}
        		
            }else{
            	System.out.println("missing level !!! < f: file level or c: chunk level >");
            	return;
            }
        	
        }else{
        	System.out.println("can't get the tokent");
        	return;
        }
		
	}

}

