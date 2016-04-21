package com.onyx.commander.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.gui.GuiController;
import com.onyx.commander.utils.Constants;
import com.onyx.quadcopter.tasks.ShutdownHook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * Logger.
     */
    public final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    /**
     * Thread coordinator.
     */
    public static final ScheduledExecutorService COORDINATOR = Executors.newScheduledThreadPool(Constants.NUM_THREADS);
    
    public static void main(String[] args) {
	launch(args);
	addHook();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
       primaryStage.setTitle("Commander");
//       TitledPane myPane = (TitledPane)FXMLLoader.load(
//	       getClass().getResource("/commander.fxml"));
       
       
       FXMLLoader fxmlLoader = new FXMLLoader();
       TitledPane myPane = (TitledPane) fxmlLoader.load(getClass().getResource("/commander.fxml").openStream());
       GuiController guiController = (GuiController) fxmlLoader.getController();
       
       
       Scene myScene = new Scene(myPane);
       myScene.setRoot(myPane);
       primaryStage.setScene(myScene);
       primaryStage.show();
       LOGGER.debug("Application Launched.");
       primaryStage.setOnCloseRequest(guiController);
    }
    
    /**
     * Shutdown hook.
     */
    private static void addHook() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(Thread.currentThread()));
    }
    
}
