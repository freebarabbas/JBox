package clsTypes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class include static functions to operate sqlite database.
 */
public class dbop {
	
	/** The database connection. */
	private static Connection conn=null;
	
	/**
	 * Initial the connection.
	 *
	 * @param cs the connection string
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	public static void InitConnection(String cs) throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:"+cs);
	}
	
	/**
	 * Close connection.
	 *
	 * @throws SQLException the SQL exception
	 */
	public static void CloseConnection() throws SQLException
	{
		if(conn!=null)
		{
			conn.close();
			conn=null;
		}
	}
	
	/**
	 * Gets the database schema version.
	 *
	 * @return the database version
	 * @throws Exception the exception
	 */
	public static int GetDBVersion() throws Exception
    {
        if (conn==null)
            throw new Exception("Must init sql connection firstly");
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        ResultSet rs = statement.executeQuery("select dbversion from version");
        int v=0;
        if(rs.next())
        {
          v=rs.getInt("dbversion");
        }
        rs.close();
        statement.close();
        return v;
    }
	
	/**
	 * Gets the guid by file name.
	 *
	 * @param filename the filename which need to query
	 * @return the file guid
	 * @throws Exception the exception
	 */
	public static String GetGuidByFileName(String filename) throws Exception
    {
		if (conn==null)
            throw new Exception("Must init sql connection firstly");
		Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        ResultSet rs = statement.executeQuery("select  guid from usermetadata where path='" + filename + "' COLLATE NOCASE");
        String v="";
        if(rs.next())
        {
          v=rs.getString("guid");
        }
        rs.close();
        statement.close();
        return v;

    }
	
	/**
	 * Insert user meta data.
	 *
	 * @param ts the timestamp of insertion
	 * @param listfi the all file info list
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean InsertUserMetaData(Date ts,List<fileInfo> listfi) throws Exception
    {
		if (conn==null)
            throw new Exception("Must init sql connection firstly");
		boolean commitflg=conn.getAutoCommit();
		conn.setAutoCommit(false);
		boolean ret=true;
		
		try
		{
			for(fileInfo fi :listfi)
			{
				PreparedStatement pst = conn.prepareStatement("insert into UserMetadata (timestamp, parentguid, guid,type,flag,accesstimestamp,writetimestamp,hashvalue,length,path,recordtype) values(?,?,?,?,?,?,?,?,?,?,?)");
			    pst.setString(1, SmallFunctions.Date2String(ts));
			    pst.setString(2, fi.parentguid);
			    pst.setString(3, fi.guid);
			    pst.setInt(4, fi.type);
			    pst.setInt(5, fi.status);
			    pst.setString(6, SmallFunctions.Date2String(fi.lastaction));
			    pst.setString(7, SmallFunctions.Date2String(fi.dt));
			    pst.setString(8, fi.filehash);
			    pst.setLong(9, fi.bytelength);
			    pst.setString(10, fi.filename);
			    pst.setInt(11, 1);
			    pst.execute();
			    Statement statement = conn.createStatement();
			    ResultSet rs = statement.executeQuery("Select last_insert_rowid()");
			    if(rs.next())
		        {
			      fi.dbid=rs.getInt(1);
			    }
			    rs.close();
			    statement.close();
			}
			conn.commit();
		}
		catch (SQLException e)
		{
		     if (conn != null) 
		     {
		        conn.rollback();
		     }
		     ret=false;
		} 
		finally 
		{
			conn.setAutoCommit(commitflg);
		}
		return ret;
		
    }
	
	
	/**
	 * Clear user meta data.
	 *
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean ClearUserMetaData() throws Exception
    {
		if (conn==null)
            throw new Exception("Must init sql connection firstly");
		Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        statement.executeUpdate("delete from UserMetadata where recordtype=1");
        statement.close();
		return true;
    }
	
	/**
	 * Gets the user meta data.
	 *
	 * @return the file list
	 * @throws Exception the exception
	 */
	public static List<fileInfo> GetUserMetaData() throws Exception
    {
		if (conn==null)
            throw new Exception("Must init sql connection firstly");
		Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        List<fileInfo> ret = new ArrayList<fileInfo>();
        ResultSet rs = statement.executeQuery("select * from UserMetadata where recordtype=1");
        while(rs.next())
        {
        	fileInfo fi=new fileInfo(rs.getInt(1), rs.getString(11), rs.getString(4), rs.getString(3), rs.getInt(6), rs.getInt(5), SmallFunctions.String2Date(rs.getString(8)), SmallFunctions.String2Date(rs.getString(7)),"");
        	fi.bytelength=rs.getLong(10);
        	fi.filehash=rs.getString(9);
        	ret.add(fi);
        }
        rs.close();
        statement.close();
        return ret;
    }
	
	/**
	 * Gets the date when creating user meta data .
	 *
	 * @return the date
	 * @throws Exception the exception
	 */
	public static Date GetUserMetaDataTS() throws Exception
    {
        
		if (conn==null)
            throw new Exception("Must init sql connection firstly");
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        ResultSet rs = statement.executeQuery("select max(timestamp) as ts from usermetadata");
        Date v=null;
        if(rs.next())
        {
          if(rs.getString("ts")!=null)
        	  v=SmallFunctions.String2Date(rs.getString("ts"));
        }
        rs.close();
        statement.close();
        return v;	
    }
	
	/**
	 * Insert file status.
	 *
	 * @param filename the filename
	 * @param status the file's status
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean InsertFileStatus(String filename,int status) throws Exception
    {
		if (conn==null)
            throw new Exception("Must init sql connection firstly");
		boolean commitflg=conn.getAutoCommit();
		conn.setAutoCommit(false);
		boolean ret=true;
		
		try
		{
			Statement statement = conn.createStatement();
		    ResultSet rs = statement.executeQuery("select * from FileStatus where fullpath='" + filename + "'  COLLATE NOCASE");
		    boolean found=false;
		    if(rs.next())
	        {
		      found=true;
		    }
		    rs.close();
		    if(found)
		    {
		    	statement.executeUpdate("update FileStatus set status="+Integer.toString(status) +" where FullPath='"+filename+"' COLLATE NOCASE");
		    }
		    else
		    {
		    	statement.executeUpdate("insert into FileStatus (FullPath,Status) values('"+filename+"', "+Integer.toString(status)+")");
		    }
			conn.commit();
		}
		catch (SQLException e)
		{
		     if (conn != null) 
		     {
		        conn.rollback();
		     }
		     ret=false;
		} 
		finally 
		{
			conn.setAutoCommit(commitflg);
		}
		return ret;
    }

    /**
     * Clear all files' status.
     *
     * @return true, if successful
     * @throws Exception the exception
     */
    public static boolean ClearFileStatus() throws Exception
    {
        
    	if (conn==null)
            throw new Exception("Must init sql connection firstly");
		Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        statement.executeUpdate("delete from FileStatus");
        statement.close();
		return true;  	
    }
    
    /**
     * Gets the file status.
     *
     * @param filename the file name
     * @return the file status
     * @throws Exception the exception
     */
    public static int GetFileStatus(String filename) throws Exception
    {
    	if (conn==null)
            throw new Exception("Must init sql connection firstly");
        Statement statement = conn.createStatement();
        statement.setQueryTimeout(30); 
        ResultSet rs = statement.executeQuery("select  status from FileStatus where fullpath='" + filename + "' COLLATE NOCASE");
        int v = 0;
        if(rs.next())
        {
          v=rs.getInt("status");
        }
        rs.close();
        statement.close();
        return v;	
    	
    }
	

}
