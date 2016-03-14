package pkgRESTConnector;

/**
 * The structure of proxy .
 */
public class ebProxy {
	
	/* Proxy address. */
	public String address;
	
	/* Proxy port. */
	public int port;
	
	/* Proxy username. */
	public String username;
	
	/* Proxy password. */
	public String password;
	
	/** The enabled flag. <br>
	 * <b>0</b>: disabled<br>
	 * <b>1</b>: enabled<br>
	 * */
	public int flag;
	/**
	 * Instantiates a new ebproxy.
	 */
	public ebProxy()
	{
		flag=0;
	}
	
	/**
	 * Instantiates a new ebproxy.
	 * @param a the proxy address
	 * @param p the proxy port
	 * @param u the proxy username
	 * @param pwd the proxy password
	 */
	public ebProxy(String a,int p,String u,String pwd)
	{
		address=a;
		port=p;
		username=u;
		password=pwd;
		flag=1;
	}
}

/*
package JBox;

public class ebProxy {
	public String address;
	public int port;
	public String username;
	public String password;
	public int flag;
	public ebProxy()
	{
		flag=0;
	}
	public ebProxy(String a,int p,String u,String pwd)
	{
		address=a;
		port=p;
		username=u;
		password=pwd;
		flag=1;
	}
}
*/