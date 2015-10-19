package clsTypes;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import clsRESTConnector.ebProxy;

public class Config {
	
	//public static String syncfoler="c:\\kenstuff\\jboxsync1";
	public static String syncfoler="//home//ubuntu//JBox";
	
	public static String usermetafile="//home//ubuntu//JBoxLog//10846130789747_JBOX_hp_com";
	public static String appname="JBox";
	public static List<String> syncfolders = new ArrayList<String>();

	//public static String swiftusr = "10846130789747:JavaTestUser";
    //public static String swiftpwd = "!qaz2wsx";    
    
    public static String serverlogin = "https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/";
    public static String storageurl = "";//https://region-a.geo-1.objects.hpcloudsvc.com/v1/10846130789747";
    
    //public static String restproxy = "web-proxy.corp.hp.com";
    public static String restproxy = "";
    public static int restproxyport = 8080;
    public static String restproxyuser = "";
    public static String restproxypwd = "";
    public static ebProxy proxyobj=null;
    public static String metafileversion = "2.0";
    public static int fixedchunksize = 4 * 1024 * 1024;
    public static String apppath= Paths.get("").toAbsolutePath().toString();    
    public static String dbpath=String.format("%s//userdata.db",apppath);
    //public static String loggerfile=String.format("%s\\run.html", apppath);
    public static String loggerfile=String.format("//home//ubuntu//JBoxLog//run.html", apppath);    
    public static String userkey = "ABCHPHPHPCLOUDXYZ";
    public static chunkType ct = chunkType.VAR; //FIX for fix chunk and VAR for variable chunk
    public static Logger logger=null;
    public static Level loglevel=Level.DEBUG; //DEBUG show all info or INFO or ERROR
    
    
	public static String swiftusr;//"10846130789747:JBOX@hp.com";
    public static String swiftpwd;//"Wang_634917";
    
    
    public static void setswiftusr(String strswiftusr){ swiftusr = strswiftusr; }
    public static void setswiftpwd(String strswiftpwd){ swiftpwd = strswiftpwd; }    
 
    public static String token="";
    public static void settoken(String strtoken){ token = strtoken; }  
	//public String getswiftusr(){ return swiftusr.get();}
	//public void setswiftusr(String vswiftusr) { swiftusr.set(vswiftusr);}
    
    //private static String sourcedbpath=String.format("%s\\schema.db",apppath);
	
	/*
	public static String syncfoler="c:\\kenstuff\\jboxsync1";
	public static String usermetafile="c:\\JavaTestUser.txt";
	public static String appname="JBox";
	public static List<String> syncfolders = new ArrayList<String>();
	public static String swiftusr = "10846130789747:JavaTestUser";
    public static String swiftpwd = "!qaz2wsx";
    public static String serverlogin = "https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/";
    public static String storageurl = "";//https://region-a.geo-1.objects.hpcloudsvc.com/v1/10846130789747";
    public static String token="";
    public static String restproxy = "web-proxy.corp.hp.com";
    public static int restproxyport = 8080;
    public static String restproxyuser = "";
    public static String restproxypwd = "";
    public static ebProxy proxyobj=null;
    public static String metafileversion = "2.0";
    public static int fixedchunksize = 4 * 1024 * 1024;
    public static String apppath= Paths.get("").toAbsolutePath().toString();    
    public static String dbpath=String.format("%s\\userdata.db",apppath);
    public static String loggerfile=String.format("%s\\run.html", apppath);
    public static String userkey = "ABCHPHPHPCLOUDXYZ";
    public static chunkType ct = chunkType.VAR;
    public static Logger logger=null;
    public static Level loglevel=Level.DEBUG;
    
    private static String sourcedbpath=String.format("%s\\schema.db",apppath);
    */
    public static boolean InitLogger()
    {
    	try
    	{
    		logger= Logger.getLogger(appname);
    		//SimpleLayout layout = new SimpleLayout();
            //HTMLLayout  layout = new HTMLLayout();
    		//PatternLayout layout=new PatternLayout("[%d{MMM dd yyyy HH:mm:ss}] %-5p (%F:%L) - %m%n");
    		JBoxHtmlLayout  layout = new JBoxHtmlLayout();
    		RollingFileAppender appender = null;
            appender =new RollingFileAppender(layout,loggerfile,false);
            appender.setMaximumFileSize(5*1024*1024);
            logger.addAppender(appender);
            logger.setLevel(loglevel);           
    		return true;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
    }
    
    public static boolean InitConfig(String[] args) throws Exception
    {
        try
        {         
        	
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals("-chunksize"))
                {
                    i++;
                    fixedchunksize = Integer.parseInt(args[i]);
                }
                if (args[i].equals("-serverurl"))
                {
                    i++;
                    serverlogin = args[i];
                }
                if (args[i].equals("-syncfolder"))
                {
                    i++;
                    syncfoler = args[i];
                }
                if (args[i].equals("-user"))
                {
                    i++;
                    swiftusr = args[i];
                }
                if (args[i].equals("-password"))
                {
                    i++;
                    swiftpwd = args[i];
                }
                if (args[i].equals("-proxy"))
                {
                    i++;
                    restproxy = args[i];
                }
                if (args[i].equals("-proxyport"))
                {
                    i++;
                    restproxyport = Integer.parseInt(args[i]);
                }
                if (args[i].equals("-proxyusr"))
                {
                    i++;
                    restproxyuser = args[i];
                }
                if (args[i].equals("-proxypwd"))
                {
                    i++;
                    restproxypwd = args[i];
                }
                if (args[i].equals("-chunktype"))
                {
                    i++;
                    if (args[i].equals("var"))
                        ct = chunkType.VAR;
                    else if (args[i].equals("fix"))
                        ct = chunkType.FIX;
                    else if (args[i].equals("no"))
                        ct = chunkType.NO;
                }
            }
            
            proxyobj=new ebProxy(restproxy,restproxyport,restproxyuser,restproxypwd);
            if(restproxy.equals(""))
            	proxyobj.flag=0;
            
            syncfolders.add(syncfoler);
            
            /*
            dbpath=String.format("%s\\%s.db",apppath,swiftusr.replace(':', ' '));
            File f=new File(dbpath);
            if(!f.exists())
            	Files.copy(Paths.get(sourcedbpath), Paths.get(dbpath));
            dbop.InitConnection(dbpath);*/
            
            return true;
        }
        catch (Exception e)
        {
            throw e;
        }

    }
    
    
    public static String ConvertToHTML()
    {
    	StringBuilder sb=new StringBuilder();
    	sb.append("Config paramters list:").append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "usermetafile").replace(' ', '-')).append(usermetafile).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "syncfoler").replace(' ', '-')).append(syncfoler).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "swiftusr").replace(' ', '-')).append(swiftusr).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "swiftpwd").replace(' ', '-')).append("********").append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "serverlogin").replace(' ', '-')).append(serverlogin).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "storageurl").replace(' ', '-')).append(storageurl).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "token").replace(' ', '-')).append(token).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "restproxy").replace(' ', '-')).append(restproxy).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "restproxyport").replace(' ', '-')).append(Integer.toString(restproxyport)).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "restproxyuser").replace(' ', '-')).append(restproxyuser).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "restproxypwd").replace(' ', '-')).append("********").append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "metafileversion").replace(' ', '-')).append(metafileversion).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "fixedchunksize").replace(' ', '-')).append(Integer.toString(fixedchunksize)).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "dbpath").replace(' ', '-')).append(dbpath).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "loggerfile").replace(' ', '-')).append(loggerfile).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "chunktype").replace(' ', '-')).append(ct.toString()).append(System.getProperty("line.separator")).append("<br>");
    	sb.append(String.format("&nbsp;&nbsp;&nbsp;&nbsp;<b>%-25s</b>", "loglevel").replace(' ', '-')).append(loglevel.toString()).append(System.getProperty("line.separator")).append("<br>");
    	
    	return sb.toString();
    }

}
