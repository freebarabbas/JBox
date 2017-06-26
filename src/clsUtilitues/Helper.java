package clsUtilitues;

public class Helper {
	
	private String m_type;
	
	public Helper(String p_type){
		m_type=p_type;
	}

	private String GetSync(){
		String strSync=""
		+ "    Sync[s]:Sync with every 5 second with multiple clients\n"
		+ "          <Options for s>(mandatory): s\n"
		+ "    <example>\n"
		+ "    e.g: java -jar ./JBox.jar s \n"
		+ "\n";
		return strSync;
	}
	
	private String GetQuery(){
		String strQuery=""
		+ "   Query[q]:Query current file level metadata, list all the files and chunk level metadata, list all the versions\n"
		+ "          <Options for q>: q\n"
		+ "                    levle: f: file level or c: chunk level\n"
		+ "                file guid: e.g 078ab3e97c284ce9b3efcc5d8d6343a9\n"
		+ "    <example>\n"
		+ "    e.g: java -jar ./JBox.jar q f - display file level metadata contnent \n"
		+ "    e.g: java -jar ./JBox.jar q c <file guid> - display all versions w/ Version. No. \n"
		+ "    e.g: java -jar ./JBox.jar q f <file guid> - display whole chunk level metadata content. \n"
		+ "\n";		
		return strQuery;
	}
	
	private String GetRetrieve(){
		String strRetrieve=""
		+ "   Retrieve[r]:Retrieve/Download file(Backup)\n"
		+ "          <Options for r>: r\n"
		+ "                    levle: c - chunk level only\n"
		+ "                file guid: e.g 078ab3e97c284ce9b3efcc5d8d6343a9\n"
		+ "                  version: option <if version doesn't specify, it will download lastest version>\n"
		+ "         output file name: e.g /home/johnny/test (mandatory)\n"
		+ "    <example>\n"
		+ "    e.g: java -jar ./JBox.jar r <level:c> <file guid> <output file name> - download lastest version to local. \n"
		+ "    e.g: java -jar ./JBox.jar r <level:c> <file guild> <version no> <output file name> - download specific version to local. \n"
		+ "\n";
		return strRetrieve;
	}
	public  void GetMenu() throws Exception{
		switch (m_type){
		case "q":
			System.out.print("Usage: JBox.jar or java -jar ./JBox.jar \n"
					+ "                [q] for Query \n"
					+ GetQuery()
					+ "");			
			break;
		case "r":
			System.out.print("Usage: JBox.jar or java -jar ./JBox.jar \n"
					+ "                [r] for Retrieve \n"
					+ GetRetrieve()
					+ "");			
			break;
		case "s":
			System.out.print("Usage: JBox.jar or java -jar ./JBox.jar \n"
					+ "                [s] for Sync \n"
					+ GetSync()
					+ "");			
			break;
		default:
			System.out.print("Usage: JBox.jar or java -jar ./JBox.jar \n"
					+ "                [q] for Query \n"
					+ "                [r] for Retrieve \n"
					+ "                [s] for Sync \n"
					+ GetQuery()
					+ GetRetrieve()
					+ GetSync()
					+ "");
			break;
		}
	}
	
}
