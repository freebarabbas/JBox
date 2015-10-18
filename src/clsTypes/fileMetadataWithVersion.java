package clsTypes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class fileMetadataWithVersion {
	public List<fileMetadata> data = null;
    public fileMetadataWithVersion()
    {
        data = new ArrayList<fileMetadata>();
    }
    public fileMetadataWithVersion(byte[] initstring) throws Exception
    {
        try
        {
        	String tmps = new String(initstring);
        	String[] lines = tmps.split("\r\n|\n|\r");
            data= new ArrayList<fileMetadata>();
            for(int i=0;i<lines.length;i++)
            {
                if (lines[i].startsWith("----"))
                    continue;
                fileMetadata fmd = new fileMetadata();
                fmd.dt = SmallFunctions.String2Date(lines[i++]);
                String[] fileinfo = lines[i++].trim().split("\\s+");
                fmd.byteslength = Long.parseLong(fileinfo[2]);
                fmd.mod = Integer.parseInt(fileinfo[3]);
                fmd.hashcode = fileinfo[4];
                fmd.data = new ArrayList<chunk>();
                int j = 1;
                while (i<lines.length)
                {
                    if (lines[i].startsWith("----"))//next version will be start
                        break;
                    String[] tmp = lines[i++].trim().split("\\s+");
                    if(tmp.length==4)
                    	fmd.data.add(new chunk(j++, Long.parseLong(tmp[1]),Long.parseLong(tmp[2]), tmp[3]));
                    else
                    	fmd.data.add(new chunk(j++, Integer.parseInt(tmp[3]),Long.parseLong(tmp[1]),Long.parseLong(tmp[2]), tmp[4]));
                    	
                }
                data.add(fmd);
            }
            
            Collections.sort(data);

        }
        catch (Exception e)
        {
            throw e;
        }
    }
    public fileMetadataWithVersion(String file) throws Exception
    {
        try
        {

            	BufferedReader br = new BufferedReader(new FileReader(file));
            	String line;
            	data= new ArrayList<fileMetadata>();
            	while ((line = br.readLine()) != null)
                {
            		fileMetadata fmd = new fileMetadata();                       
                    fmd.dt =SmallFunctions.String2Date(br.readLine());
                    String[] fileinfo =br.readLine().trim().split("\\s+");
                    fmd.byteslength = Long.parseLong(fileinfo[2]);
                    fmd.mod = Integer.parseInt(fileinfo[3]);
                    fmd.hashcode = fileinfo[4];
                    fmd.data = new ArrayList<chunk>();
                    int i = 1;
                    while ((line = br.readLine()) != null)
                    {
                        if (line.startsWith("----"))//next version will be start
                            break;
                        String[] tmp = line.trim().split("\\s+");
                        if(tmp.length==4)
                        	fmd.data.add(new chunk(i++, Long.parseLong(tmp[1]), Long.parseLong(tmp[2]), tmp[3]));
                        else
                        	fmd.data.add(new chunk(i++, Integer.parseInt(tmp[3]), Long.parseLong(tmp[1]), Long.parseLong(tmp[2]), tmp[4]));
                    }
                    data.add(fmd);
                }
            	br.close();

            Collections.sort(data);

        }
        catch (Exception e)
        {
            throw e;
        }
    }
    public String ConvertToString() throws Exception
    {
        try
        {
            StringBuilder s = new StringBuilder();
            Collections.sort(data);
            int i = 0;
            for (int j=0; j<data.size();j++)
            {
                s.append(data.get(j).ConvertToString());
                s.append("---------------------------------------------").append(System.getProperty("line.separator"));;
                i++;
                if (i == 5)
                    break;
            }
            return s.toString();
        }
        catch (Exception e)
        {
            throw e;
        }
    }
    public byte[] ConvertToByteArray() throws Exception
    {
    	return ConvertToString().getBytes(Charset.forName("UTF-8"));
    }

}
