package com.onyx.commander.gui;

import com.onyx.commander.communication.OnyxClient;
import com.onyx.commander.main.Main;
import com.onyx.commander.utils.Constants;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * Gui Controller class.
 * 
 * @author fred
 *
 */
public final class GuiController implements EventHandler<WindowEvent> {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(GuiController.class);


  /**
   * Main Pane.
   */
  @FXML
  private TitledPane mainPane;

  /**
   * Anchor Pane.
   */
  @FXML
  private AnchorPane anchorPane;

  /**
   * Connect button.
   */
  @FXML
  private Button connectButton;

  /**
   * Connection status label.
   */
  @FXML
  private Label connStatusLbl;

  /**
   * Username field.
   */
  @FXML
  private TextField usernameField;

  /**
   * Password field.
   */
  @FXML
  private PasswordField passField;

  /**
   * IP field.
   */
  @FXML
  private TextField ipField;

  /**
   * Camera titled pane.
   */
  @FXML
  private TitledPane cameraTitledPane;

  /**
   * Orientation Label.
   */
  @FXML
  private Label orientationLbl;

  /**
   * Horizontal speed label.
   */
  @FXML
  private Label hspeedLbl;

  /**
   * Vertical speed label.
   */
  @FXML
  private Label vspeedLbl;

  /**
   * Log text area.
   */
  @FXML
  private TextArea logTextArea;

  /**
   * Enter command button.
   */
  @FXML
  private Button enterCommandButton;

  /**
   * Command text field.
   */
  @FXML
  private TextField commandTextField;

  /**
   * Command output TextArea.
   */
  @FXML
  private TextArea commandOutputTextArea;

  /**
   * Onyx Client.
   */
  private OnyxClient client;

  /**
   * Camera VBox.
   */
  @FXML
  private VBox cameraVBox;

  /**
   * Camera Image View.
   */
  @FXML
  private ImageView cameraImageView;
  
  @FXML
  private LineChart<Double, Double> engineSpeedChart;

  /**
   * VLC Direct rendering future.
   */
  private Future<?> vlcdrFuture;

  private UpdateUiService uiUpdateService;

  /**
   * IP Regex.
   */
  private static final String IPADDRESS_PATTERN =
      "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
          + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

  /**
   * Command Regex.
   */
  private static final String CMD_REGEX = "[a-zA-Z0-9\\.-]*:[a-zA-Z0-9\\.-]*";

  /**
   * Connect event.
   * 
   * @param event the action event.
   */
  @FXML
  protected void connect(final ActionEvent event) {
    String ip = ipField.getText();
    if (ip.matches(IPADDRESS_PATTERN)) {
      client = new OnyxClient(ip, Constants.SERVER_PORT);
      LOGGER.debug("Connecting to " + ip);
      Main.COORDINATOR.submit(client);
      loadWebview(ip);
      uiUpdateService = new UpdateUiService(client, vlcdrFuture, 
          connStatusLbl,
          commandOutputTextArea,
          engineSpeedChart);
      uiUpdateService.setExecutor(Main.COORDINATOR);
      uiUpdateService.setPeriod(Duration.seconds(0.25));
      uiUpdateService.start();
    } else {
      LOGGER.debug("Cannot connect non valid IP address.");
    }
  }

  /**
   * Load the webview.
   * 
   * @param ip the ip address.
   */
  private void loadWebview(final String ip) {
    String url = "http://" + ip + ":8080/stream/video.mjpeg";
    VlcDirectRenderingPane vlcdr = new VlcDirectRenderingPane(url);
    cameraVBox.getChildren().clear();
    cameraVBox.getChildren().add(vlcdr);
    vlcdrFuture = Main.COORDINATOR.submit(vlcdr);
  }

  /**
   * Send a command to the server.
   */
  @FXML
  protected void sendCommand() {
    String cmd = commandTextField.getText();
    if (cmd.matches(CMD_REGEX)) {
      client.sendMessage(cmd);
    } else {
      LOGGER.debug("Invalid command format.");
    }
  }

  /**
   * Initialized JavaFX components.
   */
  @FXML
  void initialize() {
    assert mainPane != null : "fx:id=\"mainPane\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert anchorPane != null : "fx:id=\"anchorPane\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert connectButton != null : "fx:id=\"connectButton\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert connStatusLbl != null : "fx:id=\"connStatusLbl\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert usernameField != null : "fx:id=\"usernameField\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert passField != null : "fx:id=\"passField\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert ipField != null : "fx:id=\"ipField\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert cameraTitledPane != null : "fx:id=\"cameraTitledPane\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert cameraVBox != null : "fx:id=\"cameraVBox\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert cameraImageView != null : "fx:id=\"cameraImageView\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert orientationLbl != null : "fx:id=\"orientationLbl\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert hspeedLbl != null : "fx:id=\"hspeedLbl\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert vspeedLbl != null : "fx:id=\"vspeedLbl\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert logTextArea != null : "fx:id=\"logTextArea\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert enterCommandButton != null : "fx:id=\"enterCommandButton\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert commandTextField != null : "fx:id=\"commandTextField\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert commandOutputTextArea != null : "fx:id=\"commandOutputTextArea\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert engineSpeedChart != null : "fx:id=\"engineSpeedChart\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    cameraImageView.setImage(new Image(GuiController.class.getResourceAsStream("/default.jpg")));
  }
  
  @Override
  public void handle(final WindowEvent event) {
    if (client != null) {
      client.shutdown();
    }
    System.exit(0);
  }
}
