package clsFSWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
			                    for (Map.Entry<String,String> entry : mapReturn.entrySet()) {
			                    	System.out.println(entry.getKey()+"\t"+entry.getValue());
			                    }
			                    Thread.sleep(5000);
		                    }
		                }
		            });
		    executorService.invokeAll(tasks);
		}

    }
}