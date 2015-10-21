package clsUtilitues;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import clsTypes.Config;


public class TestforSyncV2 {

	public static void main(String[] args) {
		if(!Config.InitLogger())
		{
			System.out.println("Cannot start program");
			return;
		}
		
		try{
			Config.logger.info("Start the program");
			Config.logger.info("Initialize the paramters");
			Config.InitConfig(args);
			//Config.setswiftusr("10846130789747:JavaTestUser");
			//Config.setswiftpwd("!qaz2wsx");
			
			//String strUserName = "10846130789747:JBOX@hp.com";
			//String strPassWord = "Wang_634917";			
			Config.setswiftusr("johnnywa");
			Config.setswiftpwd("Chianing2345");			
			Config.logger.debug(Config.ConvertToHTML());
						
			Runnable r=new SyncV2(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj,0);
			new Thread(r).start();
			while(true)
			{
				String timeStamp = new SimpleDateFormat("yyyy/MMdd/HHmm-ss").format(Calendar.getInstance().getTime());
				//System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
				String strStatus = "";
				if( SyncStatus.GetMessage().equals("") ) {strStatus = "Start";} else {strStatus=SyncStatus.GetMessage();}
				System.out.println(timeStamp+": "+ strStatus);
				Thread.sleep(1000);
			}
			
		}
		catch(Exception e)
		{
			Config.logger.fatal(e.getMessage());
			e.printStackTrace();
		}

	}

}
