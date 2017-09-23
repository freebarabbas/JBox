package clsFSWatcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FSWatcher {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final static Map<String, Tuple<Long, Integer>> fsfinal = new HashMap<String, Tuple<Long, Integer>>();
    
    
	@SuppressWarnings("hiding")
	public class Tuple<Long, Integer> { 
    	  public final Long lngTimeStamp; 
    	  public final Integer intDirectory; 
    	  public Tuple(Long lngTimeStamp, Integer intDirectory) { 
    	    this.lngTimeStamp =lngTimeStamp; 
    	    this.intDirectory =intDirectory; 
    	  }
    } 
    
    
    /**
     * Creates a WatchService and registers the given directory
     */
    public FSWatcher(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
 
        walkAndRegisterDirectories(dir);
    }
    
    /**
     * Register the given directory with the WatchService; This function will be called by FileVisitor
     */
    private void registerDirectory(Path dir) throws IOException 
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }
    
    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     */  
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Process all events for keys queued to the watcher
     * @return 
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public
	Boolean processEvents() throws IOException {
        for (;;) {
 
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return false;
            }
 
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
 
            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();
 
                // Context for directory entry event is the file name of entry
                Path name = ((WatchEvent<Path>)event).context();
                Path child = dir.resolve(name);
 
                if ((CheckValidFileName(child, event.kind().name())) ) {
                	if ((kind == ENTRY_MODIFY) || (kind == ENTRY_DELETE)) {
                		fsfinal.put(kind+child.toString(), new Tuple<Long, Integer>(new Long(System.currentTimeMillis()), new Integer(FolderOrFileIndentifier(child.toString()))));
                	}
                }
                // if directory is created, and watching recursively, then register it and its sub-directories
                if ((kind == ENTRY_CREATE) || (kind == ENTRY_DELETE)) {
                    try {
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                        }
                    } catch (IOException x) {
                    	System.out.println(x.toString());
                    }
                }
                
            }
 
            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
 
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
		return null;
    }
        
    @SuppressWarnings("finally")
	public static Map<String, String> getfsfinalDump(){
    	Map<String, String> fsfinalDump = new HashMap<String, String>();
    	try {
			if (!fsfinal.isEmpty()){
	    		for (Iterator<Map.Entry<String,Tuple<Long, Integer>>> it = fsfinal.entrySet().iterator(); it.hasNext();) {
	    			  Map.Entry<String, Tuple<Long, Integer>> entry = it.next();
	    			  String key = entry.getKey();
	    			  Tuple<Long, Integer> lsfinal= entry.getValue();
    			      long intervalmilliseconds = System.currentTimeMillis() - lsfinal.lngTimeStamp;
    			      if (intervalmilliseconds > 5000){
    			    	  //System.out.println(key +"\t"+ getTimeStamp(lsfinal.lngTimeStamp) + "\t" +intervalmilliseconds + "\t" + lsfinal.intDirectory);
    			    	  //List<String> ls = new ArrayList<String>();
    			    	  //ls.add(key.substring(0, 12));
    			    	  //ls.add(Integer.toString(lsfinal.intDirectory));
    			    	  if (!fsfinalDump.keySet().contains(key.substring(12, key.length()))){
	    			    	  fsfinalDump.put(key.substring(12, key.length()), key.substring(0, 12));
    			    	  }
    			    	  it.remove();
	    			  }
	    		}
    		}
    	}catch(Exception e){
    		System.out.println(e.toString());
    	}
    	finally{
    		return fsfinalDump;
    	}
    }
    
    private Integer FolderOrFileIndentifier(String dir) throws IOException {
    	File f = new File(dir.toString());
    	if (f.isDirectory()){
    		return 1;
    	}else{
    		return 0;
    	}
    }
    
    private boolean CheckValidFileName(Path dir, String strEvent) throws IOException {
    	File f = new File(dir.toString());
    	String s = ".";
    	if (strEvent !="ENTRY_DELETE"){
	    	if (f.exists()){
	    		if (f.isHidden()){
		    		return false;
		    	}
		    	else if (f.isFile()){
			    	if (f.getName().toString().charAt(0) == s.charAt(0)){
			    		return false;
			    	}else{
			    		return true;
			    	}
		    	}
		    	else{
		    		return false;
		    	}
	    	}else{
	    		return false;
	    	}
    	}else{
	    	if (f.getName().toString().charAt(0) == s.charAt(0)){
	    		return false;
	    	}else{
	    		return true;
	    	}
    	}
    }
    
    /*
    private static String getTimeStamp(Long lngDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate = sdf.format(lngDate);
        return formattedDate;
    }
    */
    public static boolean TestDump(String strKey, String strFile, String strTimeStamp){
    	String strDumpFile = "test";
    	try {
    		FileWriter fw = new FileWriter("/tmp/"+strDumpFile+".log", true);
    		fw.write(strKey +"\t"+ strFile + "\t" + strTimeStamp + System.getProperty("line.separator"));
    		fw.close();
    		return true;
    	}catch(Exception e){System.out.println(e);return false;}
    }
}