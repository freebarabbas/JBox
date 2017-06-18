package clsRESTConnector;

//import clsTypes.Config;
//import clsTypes.clsProperties;



public class TestforCleanObj {

	public static void main(String[] args) throws Exception {
		if((args != null) && (args.length != 0)){
			//clsProperties properties = new clsProperties();
			//if (properties.getPropValues()) {
			//if((args != null) && (args.length != 0) && (args.length <= 2)){
			
			// TODO Auto-generated method stub
			//ebProxy m_pxy=new ebProxy("web-proxy.corp.hp.com",8080,"","");
			ebProxy m_pxy=new ebProxy();
			//RestResult rr = RestConnector.GetToken("https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/", "10846130789747:JavaTestUser", "!qaz2wsx", m_pxy);
			//RestResult rr = RestConnector.GetToken(Config.serverlogin, Config.swiftusr, Config.swiftpwd, m_pxy);
			RestResult rr = RestConnector.GetToken(args[0].toString(), args[1].toString(), args[2].toString(), m_pxy);
			String tkn=rr.token;
			String surl=rr.storageurl;//+"/var";
			boolean bolCheckContainer = false;
			//String tkn="AUTH_tke051cb50ffbd45949a31fe1e8c61a2f8";
			//String surl="http://svl12-csl-swift-ctl-001/v1/AUTH_swift";
			//RestResult rr=RestConnector.GetContainer(tkn, surl, m_pxy);
			rr=RestConnector.GetContainer(tkn, surl, m_pxy);
			if(rr.result && rr.data!=null){bolCheckContainer=true;}else{bolCheckContainer=false;}
			System.out.println("First check has container = " + bolCheckContainer);
			if(bolCheckContainer) {
				do {
					String tmp=new String(rr.data);
					String[] lines = tmp.split("\r\n|\n|\r");
					System.out.println("Total containers:" + lines.length);
					for(int i=0;i<lines.length;i++)
					{
						RestResult rr1=RestConnector.GetContainer(tkn, surl+"/"+lines[i], m_pxy);
						System.out.println("Deleting All objects under container:" + lines[i].toString()); //list only get 10,000 object
						if(rr1.result & rr1.data!=null & lines[i]!="")
						{
							String tmp1=new String(rr1.data);
							String[] lines1 = tmp1.split("\r\n|\n|\r");
							for(int j=0;j<lines1.length;j++)
							{
								System.out.println(lines1[j]);
								RestResult rrDelObj = RestConnector.DeleteFile(tkn, surl+"/"+lines[i], lines1[j], m_pxy);
								if(!rrDelObj.result){System.out.println("Delete Container:" +lines[i] + " Object: " +  lines1[j] + " Fail !");}
								if(j==9998){break;}
							}
						}
						RestResult rr2=RestConnector.GetContainer(tkn, surl+"/"+lines[i], m_pxy);
						System.out.println("check container:" + lines[i].toString() + " has message: " + rr2.msg); //list only get 10,000 object
						if(rr2.result & rr2.data==null & rr2.msg=="no any content"){
						    RestResult rrDelCon = RestConnector.DeleteContainer(tkn, surl+"/"+lines[i], m_pxy);
						    if(!rrDelCon.result){System.out.println("Delete Container:" +lines[i] + " Fail !");}
						    System.out.println((i+1)+ " Delete--"+lines[i]);
						}
					}
					rr=RestConnector.GetContainer(tkn, surl, m_pxy);
					if(rr.result && rr.data!=null){bolCheckContainer=true;}else{bolCheckContainer=false;}
					System.out.println("Check has container again = " + bolCheckContainer);
				}while(rr.result && rr.data!=null);
				System.out.println("OK!");
			}
			else
			{System.out.println("Can not find any container ! finish !");}
		}
		else{System.out.println("please input authurl, username and password");}
	}
}