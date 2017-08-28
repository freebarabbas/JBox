package clsFSWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestFSWatcher {

	public static void main(String[] args) throws IOException, InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(2);
		final Path dir = Paths.get("/tmp/test");
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
		                    FSWatcher.getfsDump();
		                    Thread.sleep(5000);
	                    }
	                }
	            });

	    executorService.invokeAll(tasks);

    }
}