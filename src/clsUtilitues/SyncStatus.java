package clsUtilitues;

import java.util.Date;

public class SyncStatus {
	
	private static String msg="";
	private static String filename="";
	private static String status="";
	private static Date setdt=null;
	
	public static  String GetMessage()
	{
		return msg;
	}
	public static String GetFileName()
	{
		return filename;
	}
	public static String GetStatus()
	{
		return status;
	}
	public static Date GetTimeStamp()
	{
		return setdt;
	}
	
	public static void SetStatus(String f,String s,String m)
	{
		setdt=new Date();
		filename=f;
		status=s;
		msg=m;
	}
	
	public static void SetStatus(String m)
	{
		setdt=new Date();
		filename="";
		status="";
		msg=m;
	}

}
