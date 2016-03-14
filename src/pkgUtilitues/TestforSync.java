package pkgUtilitues;


import pkgTypes.Config;

public class TestforSync {


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
			Config.logger.debug(Config.ConvertToHTML());
						
			Runnable r=new sync(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj);
			new Thread(r).start();
			
			while(true)
			{
				System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
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
