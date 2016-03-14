package pkgTypes;

//import java.io.DataInputStream;
import java.io.File;
//import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import pkgCityHashCalc.clsJavaVariableChunk;

/**
 * The Class include functions to calculate file's chunk information.
 */
public class ChunkProcess {

	/**
	 * Gets the variable chunk info.
	 *
	 * @param filename the filename
	 * @param mod module
	 * @return the chunk list
	 */
	public static List<chunk> GetVarChunk(String filename, int mod, int divide, int refactor)
    {
		 List<chunk> ret=new ArrayList<chunk>();
		 try
		 {
			 //2 = var
			 String varresult=clsJavaVariableChunk.GetVariableChunks(filename,mod,2, divide, refactor);
			 String[] lines=varresult.split("\r\n|\n|\r");
			 for(int i=0;i<lines.length;i++)
			 {
				 String[] tmp=lines[i].split("\\s+");
				 //if (i==0 && tmp[0].equals("file")){
				//	 mod = Integer.parseInt(tmp[3]);
				 //}
				 int s=Integer.parseInt(tmp[1]);
				 int l=Integer.parseInt(tmp[2]);
				 int e=s+l-1;
				 //if i = 0 means file level record, then end = length
				 if(i==0)
					 e=l;
				 ret.add(new chunk(i,Integer.parseInt(tmp[3]),s,e,tmp[4]));
			 }
		 }
		 catch(Exception e)
		 {
			 return null;
		 }
        return ret;           
    }
	/*
	public static List<chunk> GetFixChunk2(string filename, long chunksize)
    {
        List<chunk> ret = new List<chunk>();
        System.IO.FileInfo fi=new System.IO.FileInfo(filename);
        long filelength=fi.Length;
        ret.Add(new chunk(0, 0, filelength, HashCalc.GetFileMd5Hash(filename)));
        using (FileStream inputStream = new FileStream(filename, FileMode.Open))
        {
            for (long i = 0; i < filelength; i += chunksize)
            {
                long s = i;
                long e = ((s + chunksize) < filelength ? (s + chunksize) : filelength) - 1;
                int l =Convert.ToInt32(e - s + 1);
                byte[] buffer = new byte[l];
                inputStream.Read(buffer, 0, l);
                ret.Add(new chunk(Convert.ToInt32(i / chunksize)+1, s, e, HashCalc.GetMD5Hash(buffer)));
            }
        }
        return ret;
    }
    
    public static List<chunk> GetNoChunk2(string filename)
    {
        List<chunk> ret = new List<chunk>();
        System.IO.FileInfo fi = new System.IO.FileInfo(filename);
        long filelength = fi.Length;
        string hash=HashCalc.GetFileMd5Hash(filename);
        ret.add(new chunk(0, 0, filelength, hash ));
        ret.add(new chunk(1, 0, filelength-1, hash));
        return ret;
    }
    */
	
    /**
	 * Gets the chunk info by fixed chunk size.
	 *
	 * @param filename the filename
	 * @param chunksize the fixed chunk size
	 * @return the chunk list
	 * @throws Exception the exception
	 */
	public static List<chunk> GetFixChunk(String filename, int mod, int divide, int refactor)// int chunksize) throws Exception
    {
		/*
        List<chunk> ret = new ArrayList<chunk>();
        File file=new File(filename);
        long filelength = file.length();
        ret.add(new chunk(0, 0, filelength, HashCalc.GetFileCityHash(filename)));
        FileInputStream fis=new FileInputStream(file);
		DataInputStream dis = new DataInputStream(fis);		
		for (int i = 0; i < filelength; i += chunksize)
        {
            int s = i;
            int e = (int) (((s + chunksize) < filelength ? (s + chunksize) : filelength) - 1);
            int l = e - s + 1;
            byte[] buffer = new byte[l];
            dis.read(buffer, 0, l);
            ret.add(new chunk(i / chunksize + 1, s, e, HashCalc.GetCityHash(buffer)));
        }
		if(dis!=null)
			dis.close();
		if(fis!=null)
			fis.close();
        return ret;
        */
		
		 List<chunk> ret=new ArrayList<chunk>();
		 try
		 {
			 String varresult=clsJavaVariableChunk.GetVariableChunks(filename,mod,1,divide,refactor);
			 String[] lines=varresult.split("\r\n|\n|\r");
			 for(int i=0;i<lines.length;i++)
			 {
				 String[] tmp=lines[i].split("\\s+");
				 //if (i==0 && tmp[0].equals("file")){
				//	 mod = Integer.parseInt(tmp[3]);
				 //}
				 int s=Integer.parseInt(tmp[1]);
				 int l=Integer.parseInt(tmp[2]);
				 int e=s+l-1;
				 //if i = 0 means file level record, then end = length
				 if(i==0)
					 e=l;
				 ret.add(new chunk(i,Integer.parseInt(tmp[3]),s,e,tmp[4]));
			 }
		 }
		 catch(Exception e)
		 {
			 return null;
		 }
       return ret;           
    }

   
    /**
     * Gets the file information without any chunk.
     *
     * @param filename the filename
     * @return the chunk list
     * @throws Exception the exception
     */
    public static List<chunk> GetNoChunk(String filename) throws Exception
    {
        List<chunk> ret = new ArrayList<chunk>();
        File file=new File(filename);
        long filelength = file.length();
        String hash = HashCalc.GetFileCityHash(filename);
        ret.add(new chunk(0, 0, filelength, hash));
        ret.add(new chunk(1, 0, filelength-1, hash));
        return ret;
    }

    /**
     * Gets the chunk.
     *
     * @param filename the filename
     * @param mod module
     * @param chunksize the fixed chunk size only when the chunk type is fix
     * @param ct the chunk type
     * @return the chunk list
     * @throws Exception the exception
     */
    public static List<chunk> GetChunk(String filename,int mod, int divide,int refactor,int chunksize, chunkType ct) throws Exception
    {
        switch (ct)
        {
            case VAR:
                return GetVarChunk(filename,mod,divide,refactor);
            case FIX:
                return GetFixChunk(filename,mod,divide,refactor);
            case NO:
                return GetNoChunk(filename);
            default:
                return null;
        }
    }
    
}
