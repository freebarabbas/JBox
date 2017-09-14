package clsFSWatcher;

import static java.lang.Thread.currentThread;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestFSWatcher {

	public static void main(String[] args) throws IOException, InterruptedException {

		if ((args.length==0)||(args[0].isEmpty())){
			System.out.println("Please input a watch direcgtory");
		}
		else{
			String strDirectory="";
			for (String s: args){
				strDirectory = s;
				System.out.println("watching direcgory: "+s);
			}
			ExecutorService executorService = Executors.newFixedThreadPool(2);
			final Path dir = Paths.get(strDirectory);
			System.out.println(dir.toString());
		    ArrayList<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
		    tasks.add(
		            new Callable<Boolean>()
		            {
		                @Override
		                public Boolean call() throws Exception
		                {
		                	Boolean bolreturn = new FSWatcher(dir).processEvents();
		                	return bolreturn;
		                }
		            });
		    
		    tasks.add(
		            new Callable<Boolean>()
		            {
		                @Override
		                public Boolean call() throws Exception
		                {
		                    while(true){
			                    //FSWatcher.getfsDump();
			                    Map<String, String> mapReturn = FSWatcher.getfsfinalDump();
			                    if (!mapReturn.isEmpty()){
				                    for (Entry<String, String> entry : mapReturn.entrySet()) {
				                    	//List<String> ls= entry.getValue();
				                    	System.out.println(entry.getKey()+"\t"+entry.getValue());
				                    }
				                    //TestSync s =new TestSync("input test");
			                    	//Thread t = new Thread(s);
			                    	//t.start();
				                    TestSyncRunnable[] randomNumberTasks = new TestSyncRunnable[1];

				                    //for (int i = 0; i < 5; i++)
				                    //{
				                        randomNumberTasks[0] = new TestSyncRunnable();
				                        Thread t = new Thread(randomNumberTasks[0]);
				                        t.start();
				                    //}

				                    //for (int i = 0; i < 5; i++)
				                        System.out.println(randomNumberTasks[0].get());
			                    }
			                    Thread.sleep(5000);
		                    }
		                }
		            });
		    executorService.invokeAll(tasks);
		}

    }
}