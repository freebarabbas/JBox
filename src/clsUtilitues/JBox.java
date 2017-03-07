package clsUtilitues;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import clsTypes.Config;
import clsTypes.clsProperties;

public class JBox {

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
				
					//System.out.println(Config.serverlogin+"\n"+Config.swiftusr+"\n"+Config.swiftpwd+"\n"+Config.ct+"\n"+Config.divider+"\n"+Config.refactor+"\n"+Config.refcounter);
				
					switch (args[0].toString()){
					case "q":
						if ( args.length < 2){
							Helper m = new Helper("q");
							m.GetMenu();
						}
						else if (args[1].toString().equalsIgnoreCase("f") && args.length == 2){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString());
							Query q = new Query(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString());
							q.StartQuery();
						}else if (args[1].toString().equalsIgnoreCase("c")  && args.length == 3){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString());
							Query q = new Query(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString());
							q.StartQuery();						
						}else if (args[1].toString().equalsIgnoreCase("f") && args.length == 3){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString());
							Query q = new Query(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString());
							q.StartQuery();
						}else{
							Helper m = new Helper("q");
							m.GetMenu();
						}
						//System.out.println("Query Done !");
						break;
					case "r":
						if (args.length < 4){
							Helper m = new Helper("r");
							m.GetMenu();
						}
						else if (args[1].toString().equalsIgnoreCase("c") && args.length == 5){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString() + ", version:" + args[3].toString()+ ", output file name:" + args[4].toString());
							Retrieve r = new Retrieve(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString(), Integer.parseInt(args[3].toString()), args[4].toString());
							r.StartRetrieve();						
						}else if (args[1].toString().equalsIgnoreCase("c") && args.length == 4){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString() + ", output file name:" + args[3].toString());
							Retrieve r = new Retrieve(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString(), args[3].toString());
							r.StartRetrieve();
						}else{
							if (args.length > 5){System.out.println("Too Many Input Arguments");}
							else if (args.length <4){System.out.println("Miss Some Input Arguments");}
							Helper m = new Helper("r");
							m.GetMenu();
						}
						break;		
	                    //mod = 64, 32KB ~ 128KB
					default:	
						Config.logger.debug(Config.ConvertToHTML());
									
						Runnable r=new Sync(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd,Config.proxyobj,Config.power,Config.min,Config.max,Config.synctime);
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
