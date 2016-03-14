package pkgTypes;

public interface Metadata {
	byte[] ConvertToByteArray()  throws Exception;
	String ConvertToString()  throws Exception;
	boolean WriteToDisk(String filename)  throws Exception;
}
