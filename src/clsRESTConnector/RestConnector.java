package clsRESTConnector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

import javax.xml.bind.DatatypeConverter;

import clsTypes.Config;
import clsTypes.SmallFunctions;

public class RestConnector {
	private static HttpURLConnection GetConnection(String url,ebProxy pxy) throws Exception
	{
		URL server = new URL(url);
		HttpURLConnection conn=null;
		if(pxy.flag==1)
		{
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(pxy.address, pxy.port));
			conn = (HttpURLConnection)server.openConnection(proxy);
			if(pxy.username!="")
			{
				String encoded = new String(DatatypeConverter.printBase64Binary(new String(pxy.username+":"+pxy.password).getBytes()));
				conn.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
			}
		}
		else
		{
			conn=(HttpURLConnection)server.openConnection(Proxy.NO_PROXY);
		}
		return conn;
	}
		
	public static RestResult GetToken(String url, String username, String pwd,ebProxy pxy) throws Exception
	{
		HttpURLConnection conn=GetConnection(url,pxy);
		conn.setRequestMethod("GET");
		 
		conn.setRequestProperty("X-Auth-User", username);
		conn.setRequestProperty("X-Auth-Key", pwd);
		conn.setRequestProperty("ST_AUTH", url);
		//conn.setRequestProperty("ST_USER", username);
		//conn.setRequestProperty("ST_KEY", pwd);		
		
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK)
        {
            String storageurl=conn.getHeaderField("X-Storage-Url");
            String token=conn.getHeaderField("X-Auth-Token");
            
            //String storageurl=conn.getHeaderField("OS_STORAGE_URL");
            //String token=conn.getHeaderField("OS_AUTH_TOKEN");
            
            return new RestResult(responseCode,true,"",storageurl,token);
        }
		else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED)
        {
            return new RestResult(responseCode,false,"Invalid username or password","","");
        }
		
        else
        {
            return new RestResult(responseCode,false,Integer.toString(responseCode),"","");
        }
		
	}
	
	public static RestResult GetETag(String curtoken, String container, ebProxy pxy) throws Exception
    {
        
		HttpURLConnection conn=GetConnection(container,pxy);
		conn.setRequestMethod("GET");

		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Accept", "*/*");
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK)
        {
			String msg=conn.getHeaderField("ETag");
			return new RestResult(responseCode,true,msg,"","");
        }
		else if(responseCode == HttpURLConnection.HTTP_NO_CONTENT)
		{
			return new RestResult(responseCode,true,"can not find ETag","","");
		}
        else
        {
            return new RestResult(responseCode,false,Integer.toString(responseCode),"","");
        }			
    }

	public static RestResult PutContainer(String curtoken, String container, ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container,pxy);
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);
		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_CREATED)
        {
            return new RestResult(responseCode,true,"","","");
        }
		else if (responseCode == HttpURLConnection.HTTP_ACCEPTED)
        {
            return new RestResult(responseCode,true,"Already created","","");
        }
        else
        {
            return new RestResult(responseCode,false,"","","");
        }		
		
    }
	
	public static RestResult GetContainer(String curtoken, String container, ebProxy pxy) throws Exception
    {
        
		HttpURLConnection conn=GetConnection(container,pxy);
		conn.setRequestMethod("GET");

		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Accept", "*/*");
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK)
        {
            int l=conn.getContentLength();
            byte[] retval=new byte[l];
            InputStream in=conn.getInputStream();
            int rl=0;
            while(rl<l)
            {
            	rl+=in.read(retval,rl,l-rl);
            }
            in.close();
			RestResult rr= new RestResult(responseCode,true,"","","");
			rr.data=retval;
			return rr;
        }
		else if(responseCode == HttpURLConnection.HTTP_NO_CONTENT)
		{
			return new RestResult(responseCode,true,"no any content","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }			
    }

	public static RestResult GetObjectContent(String curtoken, String container, String object, String byterange, ebProxy pxy) throws Exception
    {
        
		//HttpURLConnection conn=GetConnection(container,pxy);
		HttpURLConnection conn=GetConnection(container+"/"+object,pxy);
		conn.setRequestMethod("GET");

		conn.setRequestProperty("X-Auth-Token", curtoken);
		//conn.setRequestProperty("Range", "bytes="+byterange); //Range: bytes=7-15,46-49
		conn.setRequestProperty("Range", "bytes=1-3650");
		conn.setRequestProperty("Accept", "*/*");
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_PARTIAL)
        {
            int l=conn.getContentLength();
            byte[] retval=new byte[l];
            InputStream in=conn.getInputStream();
            int rl=0;
            while(rl<l)
            {
            	rl+=in.read(retval,rl,l-rl);
            }
            in.close();
			RestResult rr= new RestResult(responseCode,true,"","","");
			rr.data=retval;
			return rr;
        }
		else if(responseCode == HttpURLConnection.HTTP_NO_CONTENT)
		{
			return new RestResult(responseCode,true,"no any content","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }			
    }
	
	public static RestResult DeleteContainer(String curtoken, String container, ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container,pxy);
		conn.setRequestMethod("DELETE");

		conn.setRequestProperty("X-Auth-Token", curtoken);
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NO_CONTENT)
        {        
			return new RestResult(responseCode,true,"","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }
    }
	
	public static RestResult UpdateObjectRefCount(String curtoken, String container, String object, String  objcount,ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container+"/"+object,pxy);
		conn.setRequestMethod("POST");
		
		conn.setRequestProperty("X-Auth-Token", curtoken);
		//conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		//conn.setRequestProperty("Content-Type", "application/octet-stream");
		
		if (object.substring(0,1).equals("c") || object.substring(0,8).equals("backup/c") || (object.substring(0,1).equals("f") && !objcount.substring(0,1).equals("9"))){
			conn.setRequestProperty("X-Delete-At", objcount); //
		}
		
		conn.setDoOutput(true);
		
		//OutputStream out=conn.getOutputStream();
		//out.write(data,0,data.length);
		
		int responseCode = conn.getResponseCode();
		//out.close();
		if (responseCode == HttpURLConnection.HTTP_ACCEPTED)
        {        
			return new RestResult(responseCode,true,"","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }
						
    }
	
	public static RestResult AddObjectRefCount(String curtoken, String container, String object, ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container+"/"+object,pxy);
		//HttpURLConnection conn=GetConnection(object,pxy);
		conn.setRequestMethod("GET");

		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Accept", "*/*");
		conn.setDoOutput(true);
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK)
        {
			try{
				String strRefCount=conn.getHeaderField("X-Delete-At");
				String objcount="";
				long l=0;
				if (strRefCount==null){
					objcount = "9000000001";
				}else if(!strRefCount.substring(0,1).equals("9")) {
					objcount = "9000000001";
				}
				else{
					l = Long.parseLong(strRefCount);
					l = l + 1;
					objcount = String.valueOf(l);
				}
				
				try {
					UpdateObjectRefCount(curtoken, container,object,objcount,pxy);
				}catch (Exception e){
					System.err.println(e.getMessage());
				}
			} catch (Exception e){
			    System.err.println("Caught IOException: " + e.getMessage());
			}
            return new RestResult(responseCode,true,"","","");
        }
		else if(responseCode == HttpURLConnection.HTTP_NO_CONTENT)
		{
			return new RestResult(responseCode,true,"no X-Delete-At","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }			
    }
	
	public static RestResult ReduceObjectRefCount(String curtoken, String container, String object, ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container+"/"+object,pxy);
		//HttpURLConnection conn=GetConnection(object,pxy);
		conn.setRequestMethod("GET");

		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Accept", "*/*");
		conn.setDoOutput(true);
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK)
        {
			try{
				String strRefCount=conn.getHeaderField("X-Delete-At");
				long l = Long.parseLong(strRefCount);
				String objcount;
				
				if (strRefCount.equals("9000000001") || l <=9000000001L || !strRefCount.substring(0,1).equals("9")){
					//if ref count back to zero, then purge the object, set X-Delete-At = unix time now
					
					//String strContainer=container.substring(container.indexOf("/AUTH_")+ 6 + (container.length() - (container.indexOf("/AUTH_")+ 6) -1)/2, container.length());
					
					RestConnector.RenameOrMoveFile(curtoken, container, object, container, "/backup/"+object, pxy);
					//Object names might contain a slash character (“/”), so pseudo-nested directories are possible.
					//RestConnector.CopyFile(curtoken, strContainer + "/" + object, container + "/backup/" +object + "", pxy);
					//RestConnector.DeleteFile(curtoken,container,object,pxy);
					
					//objcount = String.valueOf((System.currentTimeMillis() / 1000L) + Config.objectpurgetime*1000);
					//objcount = SmallFunctions.GetXDeleteAt(Config.objectpurgesecond);
					Config.logger.debug("move object "+object+" to /backup/");
					
					try {
						UpdateObjectRefCount(curtoken, container, "backup/" + object ,SmallFunctions.GetXDeleteAt(Config.objectpurgesecond),pxy);
						Config.logger.debug("sum X-Delete-At for backup/"+object+" for deletion in the future");
					}catch (Exception e){
						System.err.println(e.getMessage());
					}
				}
				else
				{
					l = l - 1;
					objcount = String.valueOf(l);
					
					try {
						UpdateObjectRefCount(curtoken, container, object ,objcount,pxy);
					}catch (Exception e){
						System.err.println(e.getMessage());
					}
				}
				

				
			} catch (Exception e){
			    System.err.println("Caught IOException: " + e.getMessage());
			}
            return new RestResult(responseCode,true,"","","");
        }
		else if(responseCode == HttpURLConnection.HTTP_NO_CONTENT)
		{
			return new RestResult(responseCode,true,"no X-Delete-At","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }			
    }
	
	public static RestResult PutFile(String curtoken, String container, String filename, byte[] data,ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container+"/"+filename,pxy);
		conn.setRequestMethod("PUT");
		
		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		conn.setRequestProperty("Content-Type", "application/octet-stream");
		
		if (filename.substring(0,1).equals("c")){
			conn.setRequestProperty("X-Delete-At", "9000000001"); //1451871006	9000000001
		}
		
		conn.setDoOutput(true);
		
		OutputStream out=conn.getOutputStream();
		out.write(data,0,data.length);
		
		int responseCode = conn.getResponseCode();
		out.close();
		if (responseCode == HttpURLConnection.HTTP_CREATED)
        {        
			return new RestResult(responseCode,true,"","","");
		}
        else
        {
            return new RestResult(responseCode,false,"","","");
        }
				
    }
	
	public static RestResult DeleteFile(String curtoken, String container, String filename,ebProxy pxy) throws Exception
    {
		return DeleteContainer(curtoken, container+"/"+filename,pxy);				
    }
	
	public static RestResult CopyFile(String curtoken, String src, String des,ebProxy pxy) throws Exception
    {
        
		try {
			HttpURLConnection conn=GetConnection(des,pxy);
			conn.setRequestMethod("PUT");
			conn.setDoOutput(true);
			conn.setRequestProperty("X-Auth-Token", curtoken);
			conn.setRequestProperty("X-Copy-From", src);
			//conn.setRequestProperty("Content-Length", "0");
			conn.setFixedLengthStreamingMode(0);
	
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_CREATED)
	        {        
				return new RestResult(responseCode,true,"","","");
			}
	        else
	        {
	            return new RestResult(responseCode,false,"","","");
	        }
		}catch (Exception e){
			System.err.println(e.getMessage());
			return new RestResult(400,false,"","","");
		}
		
    }
	
	public static RestResult RenameOrMoveFile(String curtoken, String oldContainer, String oldObject, String newContainer, String newObject, ebProxy pxy) throws Exception
    {
		try {
			if (oldContainer.equals(newContainer)){ // same container but just rename
				String strContainer=oldContainer.substring(oldContainer.indexOf("/AUTH_")+ 5 + (oldContainer.length() - (oldContainer.indexOf("/AUTH_")+ 6) -1)/2, oldContainer.length());
				RestConnector.CopyFile(curtoken, "/"+ strContainer + "/" + oldObject, oldContainer + newObject, pxy);
			}else{ // diff container, rename and remove
				String strOldContainer=oldContainer.substring(oldContainer.indexOf("/AUTH_")+ 5 + (oldContainer.length() - (oldContainer.indexOf("/AUTH_")+ 6) -1)/2, oldContainer.length());
				//String strNewContainer=newContainer.substring(newContainer.indexOf("/AUTH_")+ 6 + (newContainer.length() - (newContainer.indexOf("/AUTH_")+ 6) -1)/2, newContainer.length());
				RestConnector.CopyFile(curtoken, "/" + strOldContainer + "/" + oldObject, newContainer + newObject, pxy);
			}
			RestConnector.DeleteFile(curtoken,oldContainer,oldObject,pxy);
			return new RestResult(200,true,"","","");
		}catch (Exception e){
			System.err.println(e.getMessage());
			return new RestResult(400,false,"","","");
		}
    }
	
}