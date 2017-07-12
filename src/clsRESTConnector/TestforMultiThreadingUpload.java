package clsRESTConnector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/*
OpenStack Swift RESTFul API supports Parallel MultiParts of File Upload in this way: 
1. Send a MultipartUploadRequest Parallel to Swift.
2. Get a response containing a unique id for this upload operation.
3. For i in ${partCount}           
        3.1. Calculate size and offset of split-i in whole file.
        3.2 . Build a UploadPartRequest with file offset, size of current split and unique upload id. 
                   
     ***3.3. Give this request to a thread and starts upload by running thread.***                  
                3.3.1. Send associated  UploadPartRequest to Swift.                  
                3.3.2. Get response after successful upload and save ETag property of response.
***4. Wait all threads to terminate***
***5. Get ETags (ETag is an identifier for successfully completed uploads) of all terminated threads.***
Don't need ~ 6. Send a CompleteMultipartUploadRequest to Swift with unique upload id and all ETags. So Swift joins all file parts as target objects.
 */

public class TestforMultiThreadingUpload {
	/*
    //private static final Logger logger = LogUtil.getLogger();
    
    public static final long DEFAULT_FILE_PART_SIZE = 5 * 1024 * 1024; // 5MB
    public static long FILE_PART_SIZE = DEFAULT_FILE_PART_SIZE;

    //private static AmazonS3 s3Client;
    //private static TransferManager transferManager;
    
    /*
    static {
            init();
    }

    private AmazonS3Util() {

    }

    
    private static void init() {
            // ...

    //        s3Client = new AmazonS3Client(AmazonUtil.getAwsCredentials());
    //        transferManager = new TransferManager(AmazonUtil.getAwsCredentials());        
    }

    
    
    public static void putObjAsMultiPart(String bucketName, File file) {
        putObjAsMultiPart(bucketName, file, FILE_PART_SIZE);
    }

	public static void putObjAsMultiPart(String bucketName, File file, long partSize) {  
	        List<PartETag> partETags = new ArrayList<PartETag>();  
	        List<MultiPartFileUploader> uploaders = new ArrayList<MultiPartFileUploader>();  
	           
	        // Step 1: Initialize.  
	        //InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, file.getName());  
	        //InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);  
	        long contentLength = file.length();  
	           
	        try {  
	                // Step 2: Upload parts.  
	                long filePosition = 0;  
	                for (int i = 1; filePosition < contentLength; i++) {  
	                        // Last part can be less than part size. Adjust part size.  
	                        partSize = Math.min(partSize, (contentLength - filePosition));  
	
	                        // Create request to upload a part.  
	                        UploadPartRequest uploadRequest =   
	                                new UploadPartRequest().  
	                                        withBucketName(bucketName).withKey(file.getName()).  
	                                        withUploadId(initResponse.getUploadId()).withPartNumber(i).  
	                                        withFileOffset(filePosition).  
	                                        withFile(file).  
	                                        withPartSize(partSize);  
	
	                        uploadRequest.setProgressListener(new UploadProgressListener(file, i, partSize));  
	               
	                        // Upload part and add response to our list.  
	                        MultiPartFileUploader uploader = new MultiPartFileUploader(uploadRequest);  
	                        uploaders.add(uploader);  
	                        uploader.upload();   
	
	                        filePosition += partSize;  
	                }  
	             
	                for (MultiPartFileUploader uploader : uploaders) {  
	                        uploader.join();  
	                        partETags.add(uploader.getPartETag());  
	                }  
	
	                // Step 3: complete.  
	                CompleteMultipartUploadRequest compRequest = 
	                        new CompleteMultipartUploadRequest(bucketName,   
	                                                           file.getName(),   
	                                                           initResponse.getUploadId(),   
	                                                           partETags);  
	
	                s3Client.completeMultipartUpload(compRequest);  
	        }   
	        catch (Throwable t) {
	        	System.out.println(t.getMessage().toString());
	                //logger.error("Unable to put object as multipart to Swift for file " + file.getName(), t);  
	                //s3Client.abortMultipartUpload(  
	                //        new AbortMultipartUploadRequest(  
	                //                bucketName, file.getName(), initResponse.getUploadId()));  
	        }  
	}  
	
	// ......///
	
	private static class UploadProgressListener implements ProgressListener {
	
	        File file;
	        int partNo;
	        long partLength;
	
	        UploadProgressListener(File file) {
	                this.file = file;
	        }
	
	        @SuppressWarnings("unused")
	        UploadProgressListener(File file, int partNo) {
	                this(file, partNo, 0);
	        }
	
	        UploadProgressListener(File file, int partNo, long partLength) {
	                this.file = file;
	                this.partNo = partNo;
	                this.partLength = partLength;
	        }
	
	        /*
	        @Override
	        public void progressChanged(ProgressEvent progressEvent) {
	                switch (((Object) progressEvent).getEventCode()) {
	                        case ProgressEvent.STARTED_EVENT_CODE:
	                                //logger.info("Upload started for file " + "\"" + file.getName() + "\"");
	                                break;
	                        case ProgressEvent.COMPLETED_EVENT_CODE:
	                                //logger.info("Upload completed for file " + "\"" + file.getName() + "\"" + 
	                                                ", " + file.length() + " bytes data has been transferred");
	                                break;
	                        case ProgressEvent.FAILED_EVENT_CODE:
	                                //logger.info("Upload failed for file " + "\"" + file.getName() + "\"" + 
	                                                ", " + progressEvent.getBytesTransfered() + " bytes data has been transferred");
	                                break;
	                        case ProgressEvent.CANCELED_EVENT_CODE:
	                                //logger.info("Upload cancelled for file " + "\"" + file.getName() + "\"" + 
	                                                ", " + progressEvent.getBytesTransfered() + " bytes data has been transferred");
	                                break;
	                        case ProgressEvent.PART_STARTED_EVENT_CODE:
	                                //logger.info("Upload started at " + partNo + ". part for file " + "\"" + file.getName() + "\"");
	                                break;
	                        case ProgressEvent.PART_COMPLETED_EVENT_CODE:
	                                //logger.info("Upload completed at " + partNo + ". part for file " + "\"" + file.getName() + "\"" + 
	                                                ", " + (partLength > 0 ? partLength : progressEvent.getBytesTransfered())  + 
	                                                " bytes data has been transferred");
	                                break;
	                        case ProgressEvent.PART_FAILED_EVENT_CODE:
	                                //logger.info("Upload failed at " + partNo + ". part for file " + "\"" + file.getName() + "\"" +
	                                //                ", " + progressEvent.getBytesTransfered() + " bytes data has been transferred");
	                                break;
	                }
	        }
	        
	
	}
	
	private static class MultiPartFileUploader extends Thread {  
	           
	        //private UploadPartRequest uploadRequest;  
	        //private PartETag partETag;  
	           
	        MultiPartFileUploader(UploadPartRequest uploadRequest) { 
	                //this.s3Client = s3Client;
	                //this.uploadRequest = uploadRequest;  
	        }  
	           
	        //@Override  
	        //public void run() {  
	    	//	cityhash = HashCalc.GetFileCityHash(path);
	    	//	System.out.println(HashCalc.GetFileCityHash(path)); 
	                //partETag = s3Client.uploadPart(uploadRequest).getPartETag();  
	        //}  
	           
	        private GetFileCityHash(path) {  
	                return HashCalc.GetFileCityHash(path);  
	        }  
	           
	        private void upload() {  
	                start();  
	        }  
	           
	}  
	*/
	/*
	public class MyFirstRunnable implements Runnable{
	    @Override
	    public void run() {
	        System.out.println("In a thread");
	    }
	}
	
	
	public class MySecondRunnable implements Runnable{
	    @Override
	    public void run() {
	        System.out.printf("I'm running in thread %s \n", Thread.currentThread().getName());
	    }
	}
	
	
	public static void main(String[] args) throws Exception {
		//Thread thread = new Thread(new MyFirstRunnable());
		//thread.start();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		//Runnable runnable = new ObjUploaderRunnable();

		for (int i =0; i <= 25; i++){
			//Thread thread = new Thread(runnable);
		    //thread.setName("Object " + i);
		    //thread.start();
		    //thread.join(); //The join method allows one thread to wait for the completion of another.
			
			Runnable worker = new ObjUploaderRunnable(10000000L + i);
			executor.execute(worker);
		}
		
		// This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        executor.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("Finished all threads");
	}
	*/
	
	/*
	public static class CalculationThread implements Callable<Integer> {
	    int input;
	    int count;
	    public CalculationThread(int input) {
	        this.input = input;
	        System.out.println("input number: " + input);
	    }

	    @Override
	    public Integer call() throws Exception {
	    	this.count = count + 1;
	        return count;
	    }
	}

	public static void main(String[] args) throws InterruptedException {
	    ExecutorService executorService = Executors.newFixedThreadPool(10);
	    
	    for (int i =0; i <= 25; i++){
	    	Future<Integer> result = executorService.submit(new CalculationThread(i));
		    
		    try {
		        Integer integer = result.get(10, TimeUnit.MILLISECONDS);
		        System.out.println("result: " + integer);
		    } catch (Exception e) {
		        // interrupts if there is any possible error
		        result.cancel(true);
		    }
	    }
	    executorService.shutdown();
	    executorService.awaitTermination(1, TimeUnit.SECONDS);
	}
	*/
	//public class MyCallable implements Callable<String> {

	    
    public static void main(String args[]){
        //Get ExecutorService from Executors utility class, thread pool size is 10
        ExecutorService executor = Executors.newFixedThreadPool(10);
        //create a list to hold the Future object associated with Callable
        List<Future<String>> list = new ArrayList<Future<String>>();

        for(int i=0; i< 25; i++){
            //Create ObjUploaderRunnable instance
        	byte[] data = null;
            Callable<String> callable = new ObjUploaderRunnable("token", "usercontainer", "obj"+i, data);
            //submit Callable tasks to be executed by thread pool
            Future<String> future = executor.submit(callable);
            //add Future to the list, we can get return value using Future
            list.add(future);
        }
        for(Future<String> fut : list){
            try {
                //print the return value of Future, notice the output delay in console
                // because Future.get() waits for task to get completed
                System.out.println(new Date()+ "::"+fut.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        //shut down the executor service (thread pool) now, you must shutdown thread pool in order to terminate all the threads of the pools
        executor.shutdown();
    }

}
