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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FSWatcher {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    
    /**
     * Creates a WatchService and registers the given directory
     */
    
    FSWatcher(Path dir) throws IOException {
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
     * @throws IOException 
     */
    
    void processEvents() throws IOException {
        for (;;) {
 
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
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
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>)event).context();
                Path child = dir.resolve(name);
 
                //boolean bolReturn = false;                	
                //bolReturn = FolderOrFileIndentifier(child);

                //System.out.println(formattedDate); // 12/01/2011 4:48:16 PM
                if (kind != ENTRY_DELETE){
	                if (FolderOrFileIndentifier(child)){
	                	// print out event
	                	System.out.format("%s: %s in time: %s\n", event.kind().name(), child, getTimeStamp());
	                	TesttDump(event.kind().name(),child.toString(),getTimeStamp());
	                }
                }
                else
                {
                	if (CheckFileNameStartWPeriod(child)){
                		System.out.format("%s: %s in time: %s\n", event.kind().name(), child, getTimeStamp());
                		TesttDump(event.kind().name(),child.toString(),getTimeStamp());
                	}
                }
            
                // if directory is created, and watching recursively, then register it and its sub-directories
                if ((kind == ENTRY_CREATE) || (kind == ENTRY_DELETE)) {
                    try {
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                        }
                    } catch (IOException x) {
                        // do something useful
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
    }
    
    private boolean CheckFileNameStartWPeriod(Path dir) throws IOException {
    	File f = new File(dir.toString());
    	String s = ".";
    	if (f.getName().toString().charAt(0) == s.charAt(0)){
    		return false;
    	}else{
    		return true;
    	}
    }
    
    private boolean FolderOrFileIndentifier(Path dir) throws IOException {
    	File f = new File(dir.toString());
    	String s = ".";
    	if (f.exists());{
    		if (f.isDirectory()){
	    		return false;
	    	}
	    	else if (f.isHidden()){
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
    	}
    }
    
    private static String getTimeStamp(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    
    public static boolean TesttDump(String strKey, String strFile, String strTimeStamp){
    	String strDumpFile = "test";
    	try {
    		FileWriter fw = new FileWriter("/tmp/"+strDumpFile+".log", true);
    		fw.write(strKey +"\t"+ strFile + "\t" + strTimeStamp + System.getProperty("line.separator"));
    		fw.close();
    		return true;
    	}catch(Exception e){System.out.println(e);return false;}
    	//System.out.println("Experiment Dump Success!");
    	//return true;
    }
}
