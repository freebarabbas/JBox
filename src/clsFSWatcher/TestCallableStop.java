package clsFSWatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


class Foo implements Callable<String> {
    public String call() {
        try {
            // sleep for 10 seconds
        	System.out.println("Thread Started and wait 5 seconds then kill this thread !");
            Thread.sleep(5 * 1000);
        } catch(final InterruptedException ex) {
            ex.printStackTrace();
        }

        return ("Hello, World!");
    }
}


public class TestCallableStop {

    public static void main(final String[] argv) {
        final ExecutorService service;
        final Future<String>  task;

        service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new Foo());

        try {
            final String str;

            // waits the 10 seconds for the Callable.call to finish.
            str = task.get(); // this raises ExecutionException if thread dies
            System.out.println(str);
        } catch(final InterruptedException ex) {
            ex.printStackTrace();
        } catch(final ExecutionException ex) {
            ex.printStackTrace();
        }

        service.shutdownNow();
    }
}