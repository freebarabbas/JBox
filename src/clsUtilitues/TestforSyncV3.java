package clsUtilitues;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import clsTypes.Config;
import clsTypes.chunkType;
import clsTypes.clsProperties;

public class TestforSyncV3 {

	public static void main(String[] args) {
		if(!Config.InitLogger())
		{
			System.out.println("Cannot start program");
			return;
		}
		
		try{
			
			clsProperties properties = new clsProperties();
			properties.getPropValues();
			
			System.out.println(Config.serverlogin+"\n"+Config.swiftusr+"\n"+Config.swiftpwd+"\n"+Config.ct+"\n"+Config.divider+"\n"+Config.refactor+"\n"+Config.refcounter);

			
			if((args != null) && (args.length != 0) && (args.length <= 8)){
				Config.logger.info("Start the program");
				Config.logger.info("Initialize the paramters");
				Config.InitConfig(args);
				
				
				//for (String s: args){
				//	System.out.println(s)
				//}
				
				Config.setserverlogin(args[0].toString());
				Config.setswiftusr(args[1].toString());
				Config.setswiftpwd(args[2].toString());
				
				switch (args[1].toString()){
				case "q":
					System.out.println("run: query " + ", level:" + args[4].toString() + ", name or version:" + args[5].toString());
					break;
				case "r":
					System.out.println("run: retrive " + ", level:" + args[4].toString() + ", name or version:" + args[5].toString());
					break;					
				default: //s
					System.out.println("run: sync (upload/download) " + ", dedup-alg:" + args[4].toString() + ", divider:" + args[5].toString() + ", refactor:" + args[6].toString() + ", client count:" + args[7].toString());
					
                    //mod = 64
					//32KB ~ 128KB
					if (args[4].toString().equals("var")) {Config.ct = chunkType.VAR;}
					else if (args[4].toString().equals("fix")) {Config.ct = chunkType.FIX;}
					else if (args[4].toString().equals("no")) {Config.ct = chunkType.NO;}
					else {Config.ct = chunkType.VAR;}
					//String strUserName = "10846130789747:JBOX@hp.com";
					//String strPassWord = "Wang_634917";			
					
					//Config.setswiftusr("johnnywa");
					//Config.setswiftpwd("Chianing2345");
					
					Config.setswiftdiv(Integer.parseInt(args[5].toString()));
					Config.setswiftrefactor(Integer.parseInt(args[6].toString()));
					
					//if (args.length > 7){
					//	if (args[6] != null && !args[6].toString().isEmpty()){
							Config.setswiftrefcounter(Integer.parseInt(args[7].toString()));
					//	}
					//}
					
					Config.logger.debug(Config.ConvertToHTML());
								
					Runnable r=new SyncV3(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj,0);
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
			}
			else
			{
				System.out.println("try ... #java -jar JBox.jar <authurl> <username> <password> <q:query, r:retrive, s:sync> <var || fix || no> <divider= 16 || 32 || 64 || 128 ... > <refactor= 0(no refactor) ... || 3 || 4 || 5 ...> <refcounter off(0) or on(1)> <client count>");
				System.out.println("if try ... #java -jar JBox.jar <authurl> <username> <password> <q:query, r:retrive only> <level: f: file, c: chunk> <name or version: if f, then filename, if c, then version>");
				
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal(e.getMessage());
			e.printStackTrace();
		}

	}

}
