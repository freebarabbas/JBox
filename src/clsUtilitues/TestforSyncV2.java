package clsUtilitues;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import clsTypes.Config;
import clsTypes.chunkType;


public class TestforSyncV2 {

	public static void main(String[] args) {
		if(!Config.InitLogger())
		{
			System.out.println("Cannot start program");
			return;
		}
		
		try{
			
			if((args != null) && (args.length != 0) && (args.length <= 6)){
				Config.logger.info("Start the program");
				Config.logger.info("Initialize the paramters");
				Config.InitConfig(args);
				
				
				//for (String s: args){
				//	System.out.println(s);
				//}
				
				
				System.out.println("username:" + args[0].toString() + ", password:" + args[1].toString() + ", dedup-alg:" + args[2].toString() + ", divider:" + args[3].toString() + ", refactor:" + args[4].toString());
				
				
				Config.setswiftusr(args[0].toString());
				Config.setswiftpwd(args[1].toString());
				
				if (args[2].toString().equals("var")) {Config.ct = chunkType.VAR;}
				else if (args[2].toString().equals("fix")) {Config.ct = chunkType.FIX;}
				else if (args[2].toString().equals("no")) {Config.ct = chunkType.NO;}
				else {Config.ct = chunkType.VAR;}
				//String strUserName = "10846130789747:JBOX@hp.com";
				//String strPassWord = "Wang_634917";			
				
				//Config.setswiftusr("johnnywa");
				//Config.setswiftpwd("Chianing2345");
				
				Config.setswiftdiv(Integer.parseInt(args[3].toString()));
				Config.setswiftrefactor(Integer.parseInt(args[4].toString()));
				
				if (args.length > 5){
					if (args[5] != null && !args[5].toString().isEmpty()){
						Config.setswiftrefcounter(Integer.parseInt(args[5].toString()));
					}
				}
				
				Config.logger.debug(Config.ConvertToHTML());
							
				Runnable r=new SyncV2(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj,0);
				new Thread(r).start();
				while(true)
				{
					String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
					//System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
					String strStatus = "";
					if( SyncStatus.GetMessage().equals("") ) {strStatus = "Start";} else {strStatus=SyncStatus.GetMessage();}
					System.out.println(timeStamp+": "+ strStatus);
					Thread.sleep(1000);
				}
			}
			else
			{
				System.out.println("try ... #java -jar JBox.jar <username> <password> <var || fix || no> <divider= 16 || 32 || 64 || 128 ... > <refactor= 0(no refactor) ... || 3 || 4 || 5 ...> <refcounter off(0) or on(1)>");
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal(e.getMessage());
			e.printStackTrace();
		}

	}

}
