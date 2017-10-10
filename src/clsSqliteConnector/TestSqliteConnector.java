package clsSqliteConnector;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestSqliteConnector {

	
	/**
     * Create a new table in the test database
	 * @throws ClassNotFoundException 
     *
     */
    public static void createNewTable() throws ClassNotFoundException {
    	Class.forName("org.sqlite.JDBC");
        // SQLite connection string
        String url = "jdbc:sqlite:/tmp/test.db";
        
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS USERMETAFILE (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	capacity real\n"
                + ");";
        
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
	/**
     * Connect to a sample database
     *
     * @param fileName the database file name
	 * @throws ClassNotFoundException 
     */
    public static void createNewDatabase(String fileName) throws ClassNotFoundException {
    	Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:/tmp/" + fileName;
 
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
 
    /**
    * Connect to a sample database
     * @throws ClassNotFoundException 
    */
   public static void connect() throws ClassNotFoundException {
	   Class.forName("org.sqlite.JDBC");
       Connection conn = null;
       try {
           // db parameters
           String url = "jdbc:sqlite:/tmp/test.db";
           // create a connection to the database
           conn = DriverManager.getConnection(url);
           
           System.out.println("Connection to SQLite has been established.");
           
           
       } catch (SQLException e) {
           System.out.println(e.getMessage());
       } finally {
           try {
               if (conn != null) {
                   conn.close();
               }
               else{
            	   System.out.println("Create a SQLite db in /tmp/test.db");
            	   createNewDatabase("test.db");
               }
           } catch (SQLException ex) {
               System.out.println(ex.getMessage());
           }
       }
   }
   /**
    * @param args the command line arguments
 * @throws ClassNotFoundException 
    */
   public static void main(String[] args) throws ClassNotFoundException {
       connect();
       createNewTable();
   }
}
