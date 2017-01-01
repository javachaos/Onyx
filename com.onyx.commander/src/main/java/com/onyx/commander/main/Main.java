package com.onyx.commander.main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.gui.GuiController;
import com.onyx.commander.logging.StaticOutputStreamAppender;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ShutdownHook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

public class Main extends Application {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  /**
   * Thread coordinator.
   */
  public static final ScheduledExecutorService COORDINATOR =
      Executors.newScheduledThreadPool(Constants.NUM_THREADS);

  public static void main(String[] args) {
    launch(args);
    addHook();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Commander");
    FXMLLoader fxmlLoader = new FXMLLoader();
    TitledPane myPane =
        (TitledPane) fxmlLoader.load(getClass().getResource("/commander.fxml").openStream());
    GuiController guiController = (GuiController) fxmlLoader.getController();
    primaryStage.setOnCloseRequest(guiController);
    Scene myScene = new Scene(myPane);
    myScene.setRoot(myPane);
    
    primaryStage.setScene(myScene);
    primaryStage.show();
    LOGGER.debug("Application Launched.");
    
    //Setup Textarea logging.
    OutputStream os = new TextAreaOutputStream(guiController.getLogTextArea());
    StaticOutputStreamAppender.setStaticOutputStream(os);
    
  }

  /**
   * Shutdown hook.
   */
  private static void addHook() {
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(COORDINATOR, Thread.currentThread()));
  }
  
  private static class TextAreaOutputStream extends OutputStream {
      private TextArea textArea;

      public TextAreaOutputStream(TextArea textArea) {
          this.textArea = textArea;
      }

      @Override
      public void write(int b) throws IOException {
          textArea.appendText(String.valueOf((char) b));
      }
  }

}
