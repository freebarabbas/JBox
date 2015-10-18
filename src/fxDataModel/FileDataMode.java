package fxDataModel;

import javafx.beans.property.SimpleStringProperty;

//JBoxFile object
public class FileDataMode {
	public final SimpleStringProperty FileName; 
	public final SimpleStringProperty Status; 
	public final SimpleStringProperty Size;
	public final SimpleStringProperty UpdateDate; 
	public final SimpleStringProperty CreateDate; 
	public final SimpleStringProperty Directory; 
	public final SimpleStringProperty Type; 
	public final SimpleStringProperty ParentGUID;
	public final SimpleStringProperty FileGUID; 	
	public final SimpleStringProperty ContentGUID; 		
	
	public FileDataMode(String strFileName, String strStatus, String strSize, String strUpdateDate, String strCreateDate, String strDirectory, String strType, String strParentGUID, String strFileGUID, String strContentGUID) {	
		this.FileName = new SimpleStringProperty(strFileName);
		this.Status = new SimpleStringProperty(strStatus);
		this.Size = new SimpleStringProperty(strSize);
		this.UpdateDate = new SimpleStringProperty(strUpdateDate);
		this.CreateDate = new SimpleStringProperty(strCreateDate);
		this.Directory = new SimpleStringProperty(strDirectory);
		this.Type = new SimpleStringProperty(strType);
		this.ParentGUID = new SimpleStringProperty(strParentGUID);
		this.FileGUID = new SimpleStringProperty(strFileGUID);
		this.ContentGUID = new SimpleStringProperty(strContentGUID);		
	}
	public String getFileName(){ return FileName.get();}
	public void setFileName(String vFileName) { FileName.set(vFileName);}
	
	public String getStatus(){ return Status.get();}
	public void setStatus(String vStatus) { Status.set(vStatus); }
	
	public String getSize(){ return Size.get();}
	public void setSize(String vSize) { Size.set(vSize); }
	
	public String getUpdateDate(){ return UpdateDate.get();}
	public void setUpdateDate(String vUpdateDate) { UpdateDate.set(vUpdateDate);}
	
	public String getCreateDate(){ return CreateDate.get();}
	public void setCreateDate(String vCreateDate) { CreateDate.set(vCreateDate);}
	
	public String getDirectory(){ return Directory.get();}
	public void setDirectory(String vDirectory) { Directory.set(vDirectory);}
	
	public String getType(){ return Type.get();}
	public void setType(String vType) { Type.set(vType);}
	
	public String getParentGUID(){ return ParentGUID.get();}
	public void setParentGUID(String vParentGUID) { ParentGUID.set(vParentGUID);}
	
	public String getFileGUID(){ return FileGUID.get();}
	public void setFileGUID(String vFileGUID) { FileGUID.set(vFileGUID);}

	public String getContentGUID(){ return ContentGUID.get();}
	public void setContentGUID(String vContentGUID) { ContentGUID.set(vContentGUID);}
	
}