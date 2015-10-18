package fxController;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import clsTypes.Config;
import clsTypes.fileInfo;
import clsTypes.userMetaData;
import clsUtilitues.SyncStatus;
import fxDataModel.FileDataMode;

public class MainController implements Initializable {
	public static List<fileInfo> filelist;
	//Define Table
	@FXML
	TitledPane tpMenu;
	@FXML
	TableView<FileDataMode> tvTable;
	@FXML
	TableColumn<FileDataMode,String> colFileName; 
	@FXML
	TableColumn<FileDataMode,String> colStatus; 
	@FXML
	TableColumn<FileDataMode,String> colSize;
	@FXML 
	TableColumn<FileDataMode,String> colUpdateDate;
	@FXML 
	TableColumn<FileDataMode,String> colCreateDate;	
	@FXML 
	TableColumn<FileDataMode,String> colDirectory;
	@FXML 
	TableColumn<FileDataMode,String> colType;
	@FXML 
	TableColumn<FileDataMode,String> colParentGUID;
	@FXML 
	TableColumn<FileDataMode,String> colFileGUID;
	@FXML 
	TableColumn<FileDataMode,String> colContentGUID;		
	
	//Create Table Data
    final static ObservableList<FileDataMode> data = FXCollections.observableArrayList(
    	//new FileDataMode("test24.txt", "UpToday", "910 KB", "2014-09-26 21:48:28", "2014-09-26 21:48:28", "C:\\JBOXSync\\TEST\\", "File", "PARENTGUID", "FILEGUID", "CONTENTGUID")
 	);

    private static boolean bolDisplayFileOnly = false; 
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		colFileName.setText("File Name");
		colFileName.setMinWidth(200);
		
		colFileName.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("FileName"));
		colStatus.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("Status"));
		colSize.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("Size"));
		
		colUpdateDate.setText("UpdateDate");
		colUpdateDate.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("UpdateDate"));

		colCreateDate.setText("CreateDate");
		colCreateDate.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("CreateDate"));
		
		colDirectory.setText("Directory");
		colDirectory.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("Directory"));
		
		colType.setText("Type");
		colType.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("Type"));
		
		colParentGUID.setText("ParentGUID");
		colParentGUID.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("ParentGUID"));
		
		colFileGUID.setText("FileGUID");
		colFileGUID.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("FileGUID"));
		
		colContentGUID.setText("ContentGUID");
		colContentGUID.setCellValueFactory(new PropertyValueFactory<FileDataMode, String>("ContentGUID"));		
		
		
		tvTable.setItems(data);
		//StarSyncMain();
		readMetaDataintoTable();
		
	}	

	public void actStatus(ActionEvent event){
		readMetaDataintoTable();
	}
	
	public void actSettings(ActionEvent event){
		if (!bolDisplayFileOnly) {bolDisplayFileOnly=true;}else{bolDisplayFileOnly=false;}
		System.out.println("Change Display File Only Setting to " + bolDisplayFileOnly);
	}
	
	public void actHelps(ActionEvent event) throws InterruptedException{
		System.out.println("Helps !");
		/*
		long start = new Date().getTime();
		while((bolDisplayFileOnly) && (new Date().getTime() - start < 1000L))
		{
			System.out.println("Time:"+" "+SyncStatus.GetTimeStamp()+" "+"Msg:"+SyncStatus.GetMessage()+" "+"File:"+SyncStatus.GetFileName()+" "+"Status:"+SyncStatus.GetStatus());
		}
		*/
		
		while(bolDisplayFileOnly)
		{
			//System.out.println("Time:"+" "+SyncStatus.GetTimeStamp()+" "+"Msg:"+SyncStatus.GetMessage()+" "+"File:"+SyncStatus.GetFileName()+" "+"Status:"+SyncStatus.GetStatus());
			try {
				if (SyncStatus.GetMessage()!="All to update"){
					System.out.println("Time:"+" "+SyncStatus.GetTimeStamp()+" "+"Msg:"+SyncStatus.GetMessage()+" "+"File:"+SyncStatus.GetFileName()+" "+"Status:"+SyncStatus.GetStatus());
				}
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
		/*
		long lngPERIOD = 1000L;
		long lnglastTime = System.currentTimeMillis() - lngPERIOD;
		//long lngthisTime;// = System.currentTimeMillis();
		while((bolDisplayFileOnly)&&((System.currentTimeMillis() - lnglastTime) >= lngPERIOD))
		{
			//lngthisTime = System.currentTimeMillis();
		    //if ((lngthisTime - lnglastTime) >= lngPERIOD) {
		    	lnglastTime = System.currentTimeMillis();
		    	System.out.println("Time:"+" "+SyncStatus.GetTimeStamp()+" "+"Msg:"+SyncStatus.GetMessage()+" "+"File:"+SyncStatus.GetFileName()+" "+"Status:"+SyncStatus.GetStatus());
		    //}
		}		
		*/
		
	}
	
	public static void addNewFileData(String strFileName, String strStatus, String strBytelength, String strUpdateDate, String strCreateDate, String strDirectory, String strType, String strParentGUID, String strFileGUID, String strContentGUID){
		FileDataMode entry = new FileDataMode(strFileName, strStatus, strBytelength, strUpdateDate, strCreateDate, strDirectory, strType, strParentGUID, strFileGUID, strContentGUID);
		data.add(entry);
		
	}
	
	public static void readMetaDataintoTable(){
		System.out.println("clean table content");
		data.clear();
		System.out.println("re-read local metadat again");
		try{
		//	System.out.println("Start the Read MetaData");
            File localmetafile=new File(Config.usermetafile);
            if (localmetafile.exists())
            {
            	userMetaData local = new userMetaData(Config.usermetafile);
	        	Iterator<fileInfo> it = local.filelist.iterator();
	        	while (it.hasNext()) 
	            {
	        		fileInfo fi=it.next();
	        		if (bolDisplayFileOnly){
	        			if (fi.type==0){addNewFileData(new File(fi.filename.toString()).getName(), getStatus(fi.status), humanReadableByteCount(fi.bytelength, true), fi.lastaction.toString(), fi.dt.toString(), new File(fi.filename.toString()).getParent(), getType(fi.type), fi.parentguid.toString(), fi.guid.toString(), fi.filehash.toString());}
	        		}else{
		        		addNewFileData(new File(fi.filename.toString()).getName(), getStatus(fi.status), humanReadableByteCount(fi.bytelength, true), fi.lastaction.toString(), fi.dt.toString(), new File(fi.filename.toString()).getParent(), getType(fi.type), fi.parentguid.toString(), fi.guid.toString(), fi.filehash.toString());
	        		}
	        		//addNewFileData(fi.filename.toString(), Integer.toString(fi.status), Long.toString(fi.bytelength), fi.lastaction.toString(), fi.dt.toString(), fi.filename.toString(), Integer.toString(fi.type), fi.parentguid.toString(), fi.guid.toString(), fi.filehash.toString());
	            }
	        
	        }else{Config.logger.debug("local metadata does not exist");}
		}
		catch(Exception e)
		{
			Config.logger.fatal("cannot read local metadata"+e.getMessage());
			e.printStackTrace();
		}
		
	}

	private static String getStatus(int intStatus){
		if (intStatus == 0){return "UpToDate";}else{return "Syncing";}
	}
	
	private static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " Bytes";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	private static String getType(int intType){
        String strReturn;
		switch (intType)
        {
            case 2:
            	strReturn = "RootFolder";
            	break;
			case 1:
				strReturn = "SubFolder";
				break;
			case 0:
				strReturn = "File";
				break;
			default:
				strReturn = "Cannot Recognition";
                break;
        }
		return strReturn;
	}

}
	

