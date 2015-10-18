package fxJBox;

import javafx.application.*;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.*;
import net.sf.image4j.codec.ico.ICODecoder;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import clsTypes.Config;
import clsUtilitues.SyncStatus;
import clsUtilitues.SyncV2;
 
// Java 8 code
public class TestJBoxScene extends Application {
 
	public static boolean bolTEST=false;
	static Stage primaryStage;
	private Scene loginscene;
	private static Scene mainscene;
	
	
	private static java.awt.TrayIcon trayIcon;
    // one icon location is shared between the application tray icon and task bar icon.
    // you could also use multiple icons to allow for clean display of tray icons on hi-dpi devices.
    private static final String iconImageLoc = "img/cloud_icon_plain_blue.ico";
    //private static final String jpgImageLoc = "img/cloud.jpg";
 
    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    //private Stage stage;
 
    // a timer allowing the tray icon to provide a periodic notification event.
    private static Timer notificationTimer = new Timer();
 
    // format used to display the current time in a tray icon notification.
    private static DateFormat timeFormat = SimpleDateFormat.getTimeInstance();
 
    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.
    @Override public void start(final Stage stage) {
        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);
        bolTEST=true;
		try {
			
			TestJBoxScene.primaryStage = stage;
		    loginscene = createLoginScene();
		    mainscene = createMainScene();
	
			primaryStage.setScene(loginscene);
			primaryStage.setTitle("JBox Login");
			primaryStage.show();
			//primaryStage.toFront();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
    	
    }
 
    /**
     * Sets up a system tray icon for the application.
     * @return 
     */
    private static Runnable addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();
 
            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }
 
            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            List<BufferedImage> imgs = ICODecoder.read(new File(iconImageLoc));
            Image image = imgs.get(7);
            trayIcon = new java.awt.TrayIcon(image);
                 

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if (SystemTray.isSupported()) {
								primaryStage.hide();
								showProgramIsMinimizedMsg();
							} else {
								System.exit(0);
							}
						}
					});
				}
			});   
            

			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					//System.out.println(e.getButton());
					//if(e.getClickCount()==2){
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								System.out.println("mouse click to show the mainscene");
								primaryStage.show();
							}
						});
					//}
				}
			});

            
            
            // if the user double-clicks on the tray icon, show the main app stage.
            //trayIcon.addActionListener(event -> Platform.runLater(TestJBoxScene.showStage()));

            // if the user selects the default menu item (which includes the app name), 
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("JBoxMenu");
            openItem.addActionListener(event -> {
            	//primaryStage.show();
            	//Platform.runLater(showStage());
            	System.out.println("mouse click to system tray icon show the mainscene");
            });
            //Platform.runLater(TestJBoxScene.showStage()));
 
            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);
 
            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                notificationTimer.cancel();
                
                //primaryStage.close();
                Platform.exit();
                tray.remove(trayIcon);
            });
 
            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
 
            // add the application tray icon to the system tray.
            tray.add(trayIcon);
            
            
            // create a timer which periodically displays a notification message.
            notificationTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                trayIcon.displayMessage(
                                		"Message." + timeFormat.format(new Date()),
                                		"Time:"+" "+SyncStatus.GetTimeStamp()+" "+"Msg:"+SyncStatus.GetMessage()+" "+"File:"+SyncStatus.GetFileName(),//+" "+"Status:"+SyncStatus.GetStatus(),
                                		TrayIcon.MessageType.INFO
                                )
                            );
                        }
                    },
                    2_000,
                    30_000
            );
            
            
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
		return null;
    }
 
	public static void showProgramIsMinimizedMsg() {
		trayIcon.displayMessage("Message.", "Application is still running.You can clicks icon launch it", TrayIcon.MessageType.INFO);
	}
    
    
    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     * @return 
     */
	/*
    private static Runnable showStage() {
        if (primaryStage != null) {
        	primaryStage.show();
        	//primaryStage.toFront();
        }
		return null;
    }
    */
 
    public static void main(String[] args) throws IOException, java.awt.AWTException {
        // Just launches the JavaFX application.
        // Due to way the application is coded, the application will remain running
        // until the user selects the Exit menu option from the tray icon.
        launch(args);
    }
    
    public static void changeToMain() throws Exception{
		primaryStage.setScene(mainscene);
		primaryStage.setTitle("JBox Main");
		primaryStage.hide();
        //primaryStage.getIcons().add(new javafx.scene.image.Image(jpgImageLoc));		

		TestJBoxScene.addAppToTray();
        // sets up the tray icon (using awt code run on the swing thread).
        //javax.swing.SwingUtilities.invokeLater(TestJBoxScene.addAppToTray());
 
		StarSyncThread(); //using javafx concurrency 
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
				SyncV2 obj = new SyncV2(Config.syncfolders, Config.usermetafile, Config.serverlogin, Config.swiftusr, Config.swiftpwd, Config.proxyobj, 0);
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

