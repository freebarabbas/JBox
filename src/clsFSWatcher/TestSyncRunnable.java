package clsFSWatcher;

public class TestSyncRunnable implements Runnable {
    private Object result = null;
    
	private void StartSync()
	{
		System.out.println("start sync now");
	}

    public void run()
    {
        Integer randomNumber = 1;

        // As run cannot throw any Exception
        try
        {
			StartSync();
			Thread.sleep(randomNumber * 1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            System.out.println("Stop Thread!");
        }
        catch (Exception e){
        	e.printStackTrace();
        }

        // Store the return value in result when done
        result = randomNumber;

        // Wake up threads blocked on the get() method
        synchronized(this)
        {
            notifyAll();
        }
    }

    public synchronized Object get() throws InterruptedException
    {
        while (result == null)
            wait();

        return result;
    }
    /*
     *  try
		{
			StartSync();
		}
		catch(InterruptedException ex){
			Config.logger.debug(ex.getMessage());
		}
		catch(Exception e)
		{
			Config.logger.fatal("Cannot sync."+e.getMessage());
		}
     */
}
