package fxJBox;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import clsTypes.Config;
import clsUtilitues.sync;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class JBoxScene extends Application {

	static Stage primaryStage;
	private Scene loginscene;
	private static Scene mainscene;
  
	@Override
	public void start(Stage stage) {
		//bolTEST=true
		try {
			
			JBoxScene.primaryStage = stage;
		    loginscene = createLoginScene();
		    mainscene = createMainScene();

			primaryStage.setScene(loginscene);
			primaryStage.setTitle("JBox Login");
			primaryStage.show();
			
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
					{
				         // @SuppressWarnings("deprecation")
						public void handle(WindowEvent we) {
				        	  System.out.println("Stage is closing and Close the thread");
						}
					}
			);

	
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	//JBOX UI main
	public static void main(String[] args) {
		launch(args);
	}
	

    public static void changeToMain() throws Exception{
		primaryStage.setScene(mainscene);
		primaryStage.setTitle("JBox Main");
		//StarSyncTask(); //can't stop since it's not JavaFX concurrency 
		StarSyncThread(); //using javafx concurrency 
		//StarSyncService();
    }

	//create login scene
	private Scene createLoginScene() throws Exception{			
		try{
			Parent loginroot = FXMLLoader.load(getClass().getResource("LoginScene.fxml"));
			loginscene = new Scene(loginroot);
			loginscene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		} catch (Exception e){
			Logger.getLogger(JBoxScene.class.getName()).log(Level.SEVERE, null, e);
		}
		return loginscene;
	}
	
	//create main scene
	private Scene createMainScene() throws Exception{
		try{
			Parent mainroot = FXMLLoader.load(getClass().getResource("MainTableScene.fxml"));
			mainscene = new Scene(mainroot);
			mainscene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		} catch (Exception e){
			Logger.getLogger(JBoxScene.class.getName()).log(Level.SEVERE, null, e);
		}
		return mainscene;
	}
	

	public static void StarSyncThread() throws IOException{
	    //create task object
	    Task<Void> task = new Task<Void>(){
	      @Override
		protected
	      Void call() throws Exception
	      { 
	        System.out.println("Background sync run task started...");
			try{
				Config.logger.info("Start the sync in main scene");		
				sync obj = new sync(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj);
				obj.run();
			}
			catch(Exception e)
			{
				Config.logger.fatal(e.getMessage());
				e.printStackTrace();
			}
			return null;
	      }
	    };
	
	    //start the background task
	    Thread th = new Thread(task);
	    th.setDaemon(true);
	    System.out.println("Starting background thread...");
	    th.start();
	}

}
