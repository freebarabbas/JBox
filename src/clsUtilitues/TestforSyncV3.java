package clsUtilitues;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import clsTypes.Config;
import clsTypes.clsProperties;

public class TestforSyncV3 {

	public static void main(String[] args) {
		if(!Config.InitLogger())
		{
			System.out.println("Cannot start program");
			return;
		}
		
		try{
			
			if((args != null) && (args.length != 0) && (args.length <= 8)){
				Config.logger.info("Start the program");
				Config.logger.info("Initialize the paramters");
				Config.InitConfig(args);
				
				
				clsProperties properties = new clsProperties();
				if (properties.getPropValues()) {
				
					System.out.println(Config.serverlogin+"\n"+Config.swiftusr+"\n"+Config.swiftpwd+"\n"+Config.ct+"\n"+Config.divider+"\n"+Config.refactor+"\n"+Config.refcounter);
				
					switch (args[0].toString()){
					case "q":
						System.out.println("run: query " + ", level:" + args[1].toString() + ", name or version:" + args[2].toString());
						break;
					case "r":
						System.out.println("run: retrive " + ", level:" + args[1].toString() + ", name or version:" + args[2].toString());
						break;					
					default: //s
						System.out.println("run: sync (upload/download) " + ", dedup-alg:" + Config.ct + ", divider:" + Config.divider + ", refactor:" + Config.refactor + ", client count:" + Config.refcounter);
						
	                    //mod = 64, 32KB ~ 128KB
						
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
				else{
					System.out.println("Error at loading JBoxconfig.properties file at ./ ");
				}
			}
			else
			{
				System.out.println("try ... #java -jar JBox.jar <s:sync> ");
				System.out.println("if try ... #java -jar JBox.jar <q:query or r:retrive> <level: f: file, c: chunk> <name or version: if f, then filename, if c, then version>");
				
			}
		}
		catch(Exception e)
		{
			Config.logger.fatal(e.getMessage());
			e.printStackTrace();
		}

	}

}
