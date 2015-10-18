/*
 * 
 */
package clsTypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The Class fileMetadata.
 */
public class fileMetadata implements Metadata,Comparable<fileMetadata> {
	
	/** The dt. */
	public Date dt;
    
    /** The bytes length. */
    public long byteslength;
    
    /** The file module. */
    public int mod;
    
    /** The hash code. */
    public String hashcode;
    
    /** The data. */
    public List<chunk> data = null;
    
    /**
     * Constructor.
     *
     * @param file Create a fileMetadata object from the file content
     * @throws Exception If any error occur
     */
   
    public fileMetadata(String file) throws Exception
    {
    	
    	try
        {
        	
    		BufferedReader br = new BufferedReader(new FileReader(file));
    		String line;
    		dt=SmallFunctions.String2Date(br.readLine());
    		String[] fileinfo=br.readLine().trim().split("\\s+");
    		byteslength=Long.parseLong(fileinfo[2]);
    		mod=Integer.parseInt(fileinfo[3]);
    		hashcode=fileinfo[4];
    		data=new ArrayList<chunk>();
    		int i=1;
    		while ((line = br.readLine()) != null) {  			
    			if (line.startsWith("----"))
                    break;
    			String[] tmp=line.trim().split("\\s+");
    			if(tmp.length==4)
    				data.add(new chunk(i - 1, Long.parseLong(tmp[1]), Long.parseLong(tmp[2]), tmp[3]));
    			else
    				data.add(new chunk(i - 1, Integer.parseInt(tmp[3]), Long.parseLong(tmp[1]), Long.parseLong(tmp[2]), tmp[4]));
    		}
    		br.close();
    		
    		Collections.sort(data);
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    /**
     * Instantiates a new filemetadata.
     */
    public fileMetadata()
    {
        dt = new Date();
        byteslength = 0;
        hashcode = "";
        mod =0;							//initial mod is always 0
        data = new ArrayList<chunk>();
    }
    
    /**
     * Instantiates a new file metadata.
     *
     * @param d the input 
     */
    public fileMetadata(byte[] d)
    {
        try
        {
            String tmp = new String(d);
            String[] lines = tmp.split("\r\n|\n");
            dt = SmallFunctions.String2Date(lines[0]);
            String[] fileinfo = lines[1].trim().split("\\s+");
            byteslength = Long.parseLong(fileinfo[2]);
            mod=Integer.parseInt(fileinfo[3]);
            hashcode = fileinfo[4];
            data = new ArrayList<chunk>();
            for (int i = 2; i < lines.length; i++)
            {
                if (lines[i].startsWith("----"))
                    break;
                String[] tmp2 = lines[i].trim().split("\\s+");
                if(tmp2.length==4)
                	data.add(new chunk(i - 1, Long.parseLong(tmp2[1]), Long.parseLong(tmp2[2]), tmp2[3]));
                else
                	data.add(new chunk(i - 1, Integer.parseInt(tmp2[3]) ,Long.parseLong(tmp2[1]), Long.parseLong(tmp2[2]), tmp2[4]));
            }
            Collections.sort(data);
        }
        catch (Exception e)
        {
        }
    }
    

	/**
	 * Write to disk.
	 *
	 * @param filename the filename
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean WriteToDisk(String filename) throws Exception
    {
        try
        {
        	FileOutputStream out = new FileOutputStream(filename);
        	out.write(ConvertToByteArray());
        	out.close();
            return true;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /**
     * Convert to byte array.
     *
     * @return the byte[]
     * @throws Exception the exception
     */
    public byte[] ConvertToByteArray() throws Exception
    {
        return ConvertToString().getBytes(Charset.forName("UTF-8"));
    }

    /**
     * Convert to string.
     *
     * @return the string
     * @throws Exception the exception
     */
    public String ConvertToString() throws Exception
    {
        try
        {
            StringBuilder s = new StringBuilder();
            s.append(SmallFunctions.Date2String(dt));
            s.append(System.getProperty("line.separator"));
            s.append("file 0 ").append(Long.toString(byteslength)).append(" ").append(Integer.toString(mod)).append(" ").append(hashcode); //add mod # in file level 
            s.append(System.getProperty("line.separator"));
            for(chunk c : data)
            {
            	s.append("chunk ").append(Long.toString(c.start)).append(" ").append(Long.toString(c.end)).append(" ").append(Integer.toString(c.flag)).append(" ").append(c.hashvalue);
            	s.append(System.getProperty("line.separator"));
            }
            return s.toString();
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    
    @Override
	public int compareTo(fileMetadata fmd) {
		boolean tmp=this.dt.after(fmd.dt);
		if(tmp)
			return 1;
		else
			return -1;
	}
    
	public static fileMetadata GetMetadata(String filename,int mod, int chunksize, chunkType ct) throws Exception
    {
        try
        {
            fileMetadata fmd = new fileMetadata();
            File file=new File(filename);
            Path subfd=  FileSystems.getDefault().getPath(file.getAbsolutePath());
            BasicFileAttributes subattrs = Files.readAttributes(subfd,BasicFileAttributes.class);
        	FileTime sublw=subattrs.lastModifiedTime();
            fmd.dt = new Date(sublw.to(TimeUnit.SECONDS)*1000);
            fmd.byteslength = file.length();
            fmd.data = ChunkProcess.GetChunk(filename, mod, chunksize, ct);
            chunk tmp=null;
            for(chunk c :  fmd.data)
            {
            	if(c.index == 0) //c.index = 0 , present the first line which means file level record
            	{
            		tmp=c;
            		break;
            	}
            }            
            fmd.hashcode = tmp.hashvalue;
            if (mod==0){
            	fmd.mod=tmp.flag;				//if mod == 0 means never process, then use native anchor
            }else
            {
            	fmd.mod=mod;					//if mod != 0 means process already, always use the latest mod as moduler as anchor
            }
            fmd.data.remove(tmp);

            return fmd;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

	public static fileMetadata GetMetadata(String filename, int chunksize, chunkType ct) throws Exception
    {
        return GetMetadata(filename,0,chunksize,ct);
    }

}
