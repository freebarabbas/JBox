package pkgAmazonS3Connector;

import java.io.IOException;
import java.util.UUID;

public class clsTestAmazonS3Connector {
	public static void main(String[] args) throws Exception {
	
        String bucketName = "my-first-s3-bucket-" + UUID.randomUUID();
        String objkey = "TestObjectKey";
        String testfilename = "TestFileName";
        String testdata = "test\n test again \n test again and again \n";
        byte[] b = testdata.getBytes();
        
        String testprefix = "Test";

        System.out.println("===========================================");
        System.out.println("Getting Started Test with Amazon S3");
        System.out.println("===========================================\n");

        clsAmazonS3Connector s3 = new clsAmazonS3Connector();
		s3.GetCredentials();
		s3.CreateBucket(bucketName);
		s3.ListBucket(bucketName);
		
		s3.PutObject(bucketName, objkey, testfilename, b);
		s3.GetObject(bucketName, objkey);
		s3.ListObjects(bucketName, objkey, testprefix);
		
		s3.DeleteObjects(bucketName, objkey);
		s3.DeleteBucket(bucketName);
		
        System.out.println("===========================================");
        System.out.println("Getting Finished Test with Amazon S3");
        System.out.println("===========================================\n");
		
	}
}
