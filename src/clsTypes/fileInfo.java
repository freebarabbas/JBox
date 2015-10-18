package clsTypes;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class fileInfo.
 */
public class fileInfo  implements Comparable<fileInfo> {
	
	/** the id in sqlite db */
	public int dbid;
    
    /** The filename. */
    public String filename;
    
    /** The object guid. */
    public String guid;
    
    /** The object parent guid. - obsoleted */
    public String parentguid;
    
    /** Status. <br>
     * <b>0</b>: Not shared   <br>
     * <b>1</b>: Shared
     * */
    public int status;
    
    /** Shared Status. <br>
     * <b>0</b>: file   <br>
     * <b>1</b>: folder <br>
     * <b>2</b>: base folder
     * */
    public int type;      
    
    /** The file time stamp. */
    public Date dt;
    
    /** The last action time stamp. */
    public Date lastaction;
    
    /** The file required operation.
     * @see FOP
     */
    public FOP fop;
    
    /** The file hash. */
    public String filehash;
    
    /** The file owner. */
    public String owner;
    
    /** The file byte length. */
    public long bytelength;
    
    /** The version flag.  <br>
     * <b>0</b>: prelocal   <br>
     * <b>1</b>: curlocal   <br>
     * <b>2</b>: mergedlocal <br>
     * <b>3</b>: remote <br>
     * <b>4</b>: merged
     * */
    public int versionflag;
    
    public int mod;
    
    public fileInfo(String f, String gid, String pgid, int ss, int t, Date d, Date la)
    {
        filename = f;
        guid = gid;
        parentguid = pgid;
        status = ss;
        type = t;
        dt = d;
        lastaction = la;
        fop = FOP.NONE;
        owner = "";
        filehash = "";
        bytelength = 0;
        dbid = -1;
        versionflag=1;
        //mod = m;
    }
    public fileInfo(int id,String f, String gid, String pgid, int ss, int t, Date d, Date la,String fileowner)
    {
        dbid = id;
        filename = f;
        guid = gid;
        parentguid = pgid;
        status = ss;
        type = t;
        dt = d;
        lastaction = la;
        fop = FOP.NONE;
        owner = fileowner;
        filehash = "";
        bytelength = 0;
        versionflag=1;
        //mod = m;
    }
    public fileInfo copy()
    {
    	fileInfo ret = new fileInfo(dbid,filename, guid, parentguid, status, type, dt, lastaction,owner) ;
    	ret.fop = fop;
    	ret.filehash = filehash;
    	ret.bytelength = bytelength;
    	ret.versionflag=versionflag;
    	ret.status=status;
        return ret;
    }
    public void copyTo(fileInfo nfi)
    {
    	nfi.fop = fop;
    	nfi.filehash = filehash;
    	nfi.bytelength = bytelength;
    	nfi.versionflag=versionflag;
    	nfi.dbid=dbid;
    	nfi.filename=filename;
    	nfi.guid=guid;
    	nfi.parentguid=parentguid;
    	nfi.status=status;
    	nfi.type=type;
    	nfi.dt=dt;
    	nfi.lastaction=lastaction;
    	nfi.owner=owner;
    	//nfi.mod=mod;
    }
    public void copyfrom(fileInfo fi)
    {
        dbid = fi.dbid;
        filename = fi.filename;
        guid = fi.guid;
        parentguid = fi.parentguid;
        status = fi.status;
        type = fi.type;
        dt = fi.dt;
        lastaction = fi.lastaction;
        owner = fi.owner;
        fop = fi.fop;
        filehash = fi.filehash;
        bytelength = fi.bytelength;    
        versionflag=fi.versionflag;
        //mod=fi.mod;
    }
	@Override
	public int compareTo(fileInfo fi) {
		int tmp=this.filename.compareToIgnoreCase(fi.filename);
		if(tmp==0)
			return this.versionflag-fi.versionflag;
		else
			return tmp;
	}
	
	public String ConvertToHTML()
    {
    	StringBuilder sb=new StringBuilder();
    	sb.append(String.format("&nbsp;&nbsp;<b>%-60s</b>", filename).replace(' ', '-')).append(fop.toString()).append(System.getProperty("line.separator")).append("<br>");

    	return sb.toString();
    }
    
}
