package fxController;

import java.net.URL;
import java.util.ResourceBundle;

import clsRESTConnector.RestConnector;
import clsRESTConnector.RestResult;
import clsRESTConnector.ebProxy;
import clsTypes.Config;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

//import fxJBox.JBoxScene;

public class LoginController implements Initializable {
	String strUserName = "10846130789747:JBOX@hp.com";
	String strPassWord = "Wang_634917";
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		txtUserName.setText(strUserName);
		txtPassWord.setText(strPassWord);	
		
		String[] args = new String[0];
		if(!Config.InitLogger())
		{
			System.out.println("cannot start logger, stop");
			return;
		}else{
			Config.logger.debug(Config.ConvertToHTML());
			Config.logger.info("initial start logger");
			try{
				Config.logger.info("Initialize the paramters");//System.out.println("getting init config");
				Config.InitConfig(args);
			}
			catch(Exception e)
			{
				//System.out.println("can't get init config");
				Config.logger.fatal("can't get init config" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@FXML
	private Button btnLogin; 
	@FXML
	private Button btnCancel;
	@FXML
	private Label lblTitle;
	@FXML
	private TextField txtUserName, txtAccessURL;
	@FXML
	private PasswordField txtPassWord;
	
	public void actLogin(ActionEvent event) throws Exception{

		
        RestResult rr;
        ebProxy pxy = Config.proxyobj;

		rr = RestConnector.GetToken(txtAccessURL.getText(), txtUserName.getText(), txtPassWord.getText(), pxy);
        if (rr.token=="")
        {
        	Config.logger.error("Login Failure, cannot connect and get token."+rr.msg+" is empty");
			lblTitle.setText("Login Failure !");
			lblTitle.setTextFill(Color.web("#d61402"));
        }
        else{
        	Config.logger.debug("Get token:"+rr.token);
        	Config.settoken(rr.token);
        	Config.logger.debug("Set Token in Config");
        	Config.setswiftusr(txtUserName.getText());
        	Config.logger.debug("Set swift username in Config");
        	Config.setswiftpwd(txtPassWord.getText());
        	Config.logger.debug("Set swift password in Config");
        	//tkn=rr.token;
			lblTitle.setText("Login In Process ...");
			lblTitle.setTextFill(Color.web("#0e57dd"));
			
			//System.out.println("Login In !");
			//fxJBox.JBoxScene.changeToMain();
			fxJBox.TestJBoxScene.changeToMain();

			//if (fxJBox.TestJBoxScene.bolTEST){fxJBox.TestJBoxScene.changeToMain();}
			//else{fxJBox.JBoxScene.changeToMain();}

        }
	}

	public void actCancel(ActionEvent event){
		lblTitle.setText("Welcome to JBox");
		lblTitle.setTextFill(Color.web("#0e57dd"));
		txtUserName.clear();
		txtPassWord.clear();;
	}

}
