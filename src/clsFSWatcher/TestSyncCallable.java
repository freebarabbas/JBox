package clsFSWatcher;

	
import java.util.concurrent.Callable;

public class TestSyncCallable implements Callable<Boolean> {
	private boolean bolSyncReturn = false;
	
	private  Boolean StartSync() throws Exception{
		// sleep for 10 seconds
		Thread.sleep(5 * 1000);
		System.out.println("running thread now");
		Thread.sleep(5 * 1000);
		return true;
	}
	
	public Boolean call() {
	    try {
	        bolSyncReturn = StartSync();
	    } catch(final InterruptedException ex) {
	        ex.printStackTrace();
	        bolSyncReturn = false;
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return bolSyncReturn;
	
	}
}



