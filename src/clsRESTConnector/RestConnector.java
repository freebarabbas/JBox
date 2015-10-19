package clsRESTConnector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import javax.xml.bind.DatatypeConverter;

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
	
	public static RestResult PutFile(String curtoken, String container, String filename, byte[] data,ebProxy pxy) throws Exception
    {
		HttpURLConnection conn=GetConnection(container+"/"+filename,pxy);
		conn.setRequestMethod("PUT");
		
		conn.setRequestProperty("X-Auth-Token", curtoken);
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		conn.setRequestProperty("Content-Type", "application/octet-stream");
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
		
    }

}
