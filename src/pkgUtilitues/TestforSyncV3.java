package pkgUtilitues;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import pkgTypes.Config;
import pkgTypes.chunkType;


public class TestforSyncV3 {

	public static void main(String[] args) {
		if(!Config.InitLogger())
		{
			System.out.println("Cannot start program");
			return;
		}
		
		try{
			
			if((args != null) && (args.length != 0) && (args.length <= 7)){
				Config.logger.info("Start the program");
				Config.logger.info("Initialize the paramters");
				Config.InitConfig(args);
				
				
				//for (String s: args){
				//	System.out.println(s);
				//}
				
				switch (args[0].toString()){
				case "q":
					System.out.println("run: " + args[0].toString() + "username:" + args[1].toString() + ", password:" + args[2].toString() + ", level:" + args[3].toString() + ", name or version:" + args[4].toString());
					break;
				case "r":
					System.out.println("run: " + args[0].toString() + "username:" + args[1].toString() + ", password:" + args[2].toString() + ", level:" + args[3].toString() + ", name or version:" + args[4].toString());
					break;					
				default: //s
					System.out.println("run: " + args[0].toString() + "username:" + args[1].toString() + ", password:" + args[2].toString() + ", dedup-alg:" + args[3].toString() + ", divider:" + args[4].toString() + ", refactor:" + args[5].toString() + ", client count:" + args[6].toString());
					
					
					Config.setswiftusr(args[1].toString());
					Config.setswiftpwd(args[2].toString());
					
					if (args[3].toString().equals("var")) {Config.ct = chunkType.VAR;}
					else if (args[3].toString().equals("fix")) {Config.ct = chunkType.FIX;}
					else if (args[3].toString().equals("no")) {Config.ct = chunkType.NO;}
					else {Config.ct = chunkType.VAR;}
					//String strUserName = "10846130789747:JBOX@hp.com";
					//String strPassWord = "Wang_634917";			
					
					//Config.setswiftusr("johnnywa");
					//Config.setswiftpwd("Chianing2345");
					
					Config.setswiftdiv(Integer.parseInt(args[4].toString()));
					Config.setswiftrefactor(Integer.parseInt(args[5].toString()));
					
					//if (args.length > 7){
					//	if (args[6] != null && !args[6].toString().isEmpty()){
							Config.setswiftrefcounter(Integer.parseInt(args[6].toString()));
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
				System.out.println("try ... #java -jar JBox.jar <q:query, r:retrive, s:sync> <username> <password> <var || fix || no> <divider= 16 || 32 || 64 || 128 ... > <refactor= 0(no refactor) ... || 3 || 4 || 5 ...> <refcounter off(0) or on(1)> <client count>");
				System.out.println("if try ... #java -jar JBox.jar <q:query, r:retrive only> <username> <password> <level: f: file, c: chunk> <name or version: if f, then filename, if c, then version>");
				
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal(e.getMessage());
			e.printStackTrace();
		}

	}

}
