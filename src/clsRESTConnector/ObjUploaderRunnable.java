package clsRESTConnector;

import java.util.concurrent.Callable;

public class ObjUploaderRunnable implements Callable<String>{
	String strTkn;
	String strUserContainer;
	String strObj;
	
//public class ObjUploaderRunnable implements Runnable{
	public ObjUploaderRunnable(String str_tkn, String str_usercontainer, String str_obj, byte[] data) {
        this.strTkn = str_tkn;
        this.strUserContainer = str_usercontainer;
		this.strObj = str_obj;
		ebProxy pxy=new ebProxy();
        //System.out.println("input string: " + this.strObj);
        try{
        	RestConnector.PutFile(this.strTkn, this.strUserContainer, this.strObj, data , pxy);
        	//System.out.println("upload " + this.strObj + " To " + this.strUserContainer);
   		}catch(Exception e ){
   			System.out.println(e.getMessage());
   		}
    }
	
    public String call() throws Exception {
        //Thread.sleep(1000);
        //return the thread name executing this callable task
        return Thread.currentThread().getName() + " upload " + this.strObj + " To " + this.strUserContainer;
    }
	/*
	private final long countUntil;
	//private String objName;
	ObjUploaderRunnable(long countUntil) {
		this.countUntil = countUntil;
    }
    @Override
    public void run() {
        System.out.printf("I'm running in thread for uploading %s \n", Thread.currentThread().getName());
        System.out.println(countUntil);
    }
    
    */
}
