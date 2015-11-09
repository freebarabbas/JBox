package clsRESTConnector;



public class TestforCleanObj {

	public static void main(String[] args) throws Exception {
		
		if((args != null) && (args.length != 0) && (args.length <= 2)){
			// TODO Auto-generated method stub
			//ebProxy m_pxy=new ebProxy("web-proxy.corp.hp.com",8080,"","");
			ebProxy m_pxy=new ebProxy();
			//RestResult rr = RestConnector.GetToken("https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/", "10846130789747:JavaTestUser", "!qaz2wsx", m_pxy);
			RestResult rr = RestConnector.GetToken("http://svl12-csl-swift-ctl-001/auth/v1.0", args[0].toString(), args[1].toString(), m_pxy);
			String tkn=rr.token;
			String surl=rr.storageurl;//+"/var";
			//String tkn="AUTH_tke051cb50ffbd45949a31fe1e8c61a2f8";
			//String surl="http://svl12-csl-swift-ctl-001/v1/AUTH_swift";
			//RestResult rr=RestConnector.GetContainer(tkn, surl, m_pxy);
			rr=RestConnector.GetContainer(tkn, surl, m_pxy);
			System.out.println(rr.result);
			if(rr.result && rr.data!=null)
			{
				String tmp=new String(rr.data);
				//String[] bucks = tmp.split("\r\n|\n|\r");
				//System.out.println("Total containers:" + bucks[1].length());
				//for(int c=0;c<bucks[0].length(); c++)
				//{
				//System.out.println("No. " + c+1 + " container deleting");
				String[] lines = tmp.split("\r\n|\n|\r");
				System.out.println("Total containers:" + lines.length);
				System.out.println("skip 1st container" + lines[0].toString());
				for(int i=1;i<lines.length;i++)
				{
					RestResult rr1=RestConnector.GetContainer(tkn, surl+"/"+lines[i], m_pxy);
					System.out.println("Deleting All objects under container:" + lines[i].toString());
					if(rr1.result & rr1.data!=null & lines[i]!="")
					{
						String tmp1=new String(rr1.data);
						String[] lines1 = tmp1.split("\r\n|\n|\r");
						for(int j=0;j<lines1.length;j++)
						{
							System.out.println(lines1[j]);
							RestConnector.DeleteFile(tkn, surl+"/"+lines[i], lines1[j], m_pxy);
						}
					}
					RestConnector.DeleteContainer(tkn, surl+"/"+lines[i], m_pxy);
					System.out.println((i+1)+ " Delete--"+lines[i]);
				}
				//}
			}
			System.out.println("OK!");
	
		}
	}

}