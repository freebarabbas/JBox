package clsRESTConnector;



public class TestforCleanObj {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//ebProxy m_pxy=new ebProxy("web-proxy.corp.hp.com",8080,"","");
		ebProxy m_pxy=new ebProxy();
		RestResult rr = RestConnector.GetToken("https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/", "10846130789747:JavaTestUser", "!qaz2wsx", m_pxy);
		String tkn=rr.token;
		String surl=rr.storageurl;
		rr=RestConnector.GetContainer(tkn, surl, m_pxy);
		if(rr.result && rr.data!=null)
		{
			String tmp=new String(rr.data);
			String[] lines = tmp.split("\r\n|\n|\r");
			System.out.println("Total containers:" + lines.length);
			for(int i=0;i<lines.length;i++)
			{
				RestResult rr1=RestConnector.GetContainer(tkn, surl+"/"+lines[i], m_pxy);
				if(rr1.result & rr1.data!=null)
				{
					String tmp1=new String(rr1.data);
					String[] lines1 = tmp1.split("\r\n|\n|\r");
					for(int j=0;j<lines1.length;j++)
					{
						RestConnector.DeleteFile(tkn, surl+"/"+lines[i], lines1[j], m_pxy);
					}
				}
				RestConnector.DeleteContainer(tkn, surl+"/"+lines[i], m_pxy);
				System.out.println((i+1)+ " Delete--"+lines[i]);
			}
		}

	}

}
