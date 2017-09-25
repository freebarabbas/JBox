package clsUtilitues;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import clsFSWatcher.FSWatcher;
import clsTypes.Config;
import clsTypes.clsProperties;

public class JBox {

	private static Boolean StartCallableSyncThread(){
		final ExecutorService executorSyncCallableService;
		final Future<Boolean>  futureSyncCallabletask;
		Boolean bolReturn = false;
		executorSyncCallableService = Executors.newFixedThreadPool(1);        
		futureSyncCallabletask = executorSyncCallableService.submit(new SyncCallable(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd,Config.proxyobj,Config.power,Config.synctime,Config.containername));
	
	    try {
			// waits the 10 seconds for the Callable.call to finish.
			bolReturn = futureSyncCallabletask.get(); // this raises ExecutionException if thread dies
			if (bolReturn) {
				System.out.println("Thread killed and finished");
			}else{
				System.out.println("Something Wrong !");
			}
		} 
	    catch(final InterruptedException ex) {
		    ex.printStackTrace();
		} 
	    catch(final ExecutionException ex) {
		    ex.printStackTrace();
		}
		executorSyncCallableService.shutdownNow();
	    return bolReturn;
	}
	
	
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
							Query q = new Query(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(),Config.containername);
							q.StartQuery();
						}else if (args[1].toString().equalsIgnoreCase("c")  && args.length == 3){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString());
							Query q = new Query(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString(),Config.containername);
							q.StartQuery();						
						}else if (args[1].toString().equalsIgnoreCase("f") && args.length == 3){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString());
							Query q = new Query(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString(),Config.containername);
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
							Retrieve r = new Retrieve(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString(), Integer.parseInt(args[3].toString()), args[4].toString(),Config.containername);
							r.StartRetrieve();						
						}else if (args[1].toString().equalsIgnoreCase("c") && args.length == 4){
							System.out.println("run: " + args[0].toString() + ", username:" + Config.swiftusr + ", password:" + Config.swiftpwd + ", level:" + args[1].toString() + ", file guid:" + args[2].toString() + ", output file name:" + args[3].toString());
							Retrieve r = new Retrieve(Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, args[1].toString(), args[2].toString(), args[3].toString(),Config.containername);
							r.StartRetrieve();
						}else{
							if (args.length > 5){System.out.println("Too Many Input Arguments");}
							else if (args.length <4){System.out.println("Miss Some Input Arguments");}
							Helper m = new Helper("r");
							m.GetMenu();
						}
						break;		
	                    //mod = 64, 32KB ~ 128KB
					case "w":
						if (args.length==1)
						{
							Config.logger.debug(Config.ConvertToHTML());
							ExecutorService executorFSWatcherService = Executors.newFixedThreadPool(2);
							final Path dir = Paths.get(Config.syncfolders.get(0).toString());
							System.out.println("<Push> watching direcgory: "+dir.toString());
						    ArrayList<Callable<Boolean>> tasksFSWatcher = new ArrayList<Callable<Boolean>>();
						    tasksFSWatcher.add(
						            new Callable<Boolean>()
						            {
						            	@Override
						                public Boolean call() throws Exception
						                {
						                	Boolean bolreturn = new FSWatcher(dir).processEvents();
						                	return bolreturn;
						                }
						            });
						    
						    tasksFSWatcher.add(
						            new Callable<Boolean>()
						            {
						                @Override
						                public Boolean call() throws Exception
						                {
											//when start then always initial
						                	if (StartCallableSyncThread()){System.out.println("SyncThread Done!");
											}else{ System.out.println("Sync Thread Error !");}
											
						                    while(true){
							                    //FSWatcher.getfsDump();
							                    Map<String, String> mapReturn = FSWatcher.getfsfinalDump();
							                    if (!mapReturn.isEmpty()){
							                    	
							                    	String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
													//System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
													String strStatus = "";
													if( SyncStatus.GetMessage().equals("") ) {strStatus = "Start";} else {strStatus=SyncStatus.GetMessage();}
													System.out.println(timeStamp+": "+ strStatus);
							                    	
								                    for (Entry<String, String> entry : mapReturn.entrySet()) {
								                    	//List<String> ls= entry.getValue();
								                    	System.out.println(entry.getKey()+"\t"+entry.getValue());
								                    }
								                    
													if (StartCallableSyncThread()){System.out.println("SyncThread Done!");
													}else{ System.out.println("Sync Thread Error !");}
													
													System.gc(); //garbage collection
													System.out.println();
							                    }
							                    Thread.sleep(5000);
						                    }
						                }
						            });
						    executorFSWatcherService.invokeAll(tasksFSWatcher);
						}else{							
							Helper m = new Helper("w");
							m.GetMenu();
						}
						break;
					case "s":
						if (args.length==1)
						{
							Config.logger.debug(Config.ConvertToHTML());
							while(true)
							{
								String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
								//System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
								String strStatus = "";
								if( SyncStatus.GetMessage().equals("") ) {strStatus = "Start";} else {strStatus=SyncStatus.GetMessage();}
								System.out.println(timeStamp+": "+ strStatus);
								
								if (StartCallableSyncThread()){System.out.println("SyncThread Done!");
								}else{ System.out.println("Sync Thread Error !");}
	
								System.gc(); //garbage collection
								Thread.sleep(Config.synctime);
							}
						}
						else{
							Helper m = new Helper("s");
							m.GetMenu();							
						}
						break;
					case "p":
						if (args.length==1)
						{
							Config.logger.debug(Config.ConvertToHTML());
							String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
							//System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
							String strStatus = "";
							if( SyncStatus.GetMessage().equals("") ) {strStatus = "Start";} else {strStatus=SyncStatus.GetMessage();}
							System.out.println(timeStamp+": "+ strStatus);
							
							if (StartCallableSyncThread()){System.out.println("SyncThread Done!");
							}else{ System.out.println("Sync Thread Error !");}

							System.gc(); //garbage collection
							System.out.println("Push Once Done !");
						}
						else
						{
							Helper m = new Helper("p");
							m.GetMenu();							
						}
						break;
					case "t":
						if (args.length==1)
						{
							Config.logger.debug(Config.ConvertToHTML());
							
							Runnable r=new Sync(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd,Config.proxyobj,Config.power,Config.synctime,Config.containername);
							new Thread(r).start();
							while(true)
							{

								String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
								//System.out.println(SyncStatus.GetTimeStamp().toString()+" "+ SyncStatus.GetMessage());
								String strStatus = "";
								if( SyncStatus.GetMessage().equals("") ) {strStatus = "Start";} else {strStatus=SyncStatus.GetMessage();}
								System.out.println(timeStamp+": "+ strStatus);
								System.gc(); //garbage collection
								//Thread.interrupted();
								Thread.sleep(1000);
							}
						}
						break;
					default:
						Helper m = new Helper("q");
						m.GetMenu();
						break;
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
