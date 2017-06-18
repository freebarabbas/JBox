package clsRESTConnector;

public class RestResult {
	public int httpcode;
	public boolean result;
	public String msg;
	public String storageurl;
	public String token;
	public byte[] data;
	public RestResult(int c,boolean r,String m,String storage,String tkn)
	{
		httpcode=c;
		result=r;
		msg=m;
		storageurl=storage;
		token=tkn;
		data=null;
	}
}