package com.onyx.commander.gui;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.communication.OnyxClient;
import com.onyx.commander.main.Main;
import com.onyx.common.commands.CommandUtils;
import com.onyx.common.utils.Constants;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

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


  @FXML
  private TextField usernameField;

  @FXML
  private TextArea logTextArea;

  @FXML
  private Button stopPidBtn;

  @FXML
  private TextArea commandOutputTextArea;

  @FXML
  private Slider throttleSlider;

  @FXML
  private Label connStatusLbl;

  @FXML
  private TextField ipField;

  @FXML
  private VBox cameraVBox;

  @FXML
  private Button connectButton;

  @FXML
  private Button enterCommandButton;

  @FXML
  private PasswordField passField;

  @FXML
  private ImageView cameraImageView;

  @FXML
  private AnchorPane anchorPane;

  @FXML
  private Label throttleLbl;

  @FXML
  private LineChart<?, ?> engineSpeedChart;

  @FXML
  private Button startPidBtn;

  @FXML
  private Label pidStatusLbl;

  @FXML
  private TitledPane mainPane;

  @FXML
  private TitledPane cameraTitledPane;

  @FXML
  private TextField commandTextField;

  /**
   * VLC Direct rendering future.
   */
  private Future<?> vlcdrFuture;

  /**
   * Onyx Client.
   */
  private OnyxClient client;
  
  /**
   * IP Regex.
   */
  private static final String IPADDRESS_PATTERN =
      "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
          + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

  /**
   * Command Regex.
   */
  private static final String CMD_REGEX = "[a-zA-Z0-9\\.-_]+:[([a-zA-Z0-9\\.-_]*)([,]*)]+";

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
      UpdateUiService.start(client, vlcdrFuture, 
          connStatusLbl,
          commandOutputTextArea,
          engineSpeedChart);
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
      client.sendMessage(CommandUtils.parseCommand(cmd));
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
    assert stopPidBtn != null : "fx:id=\"stopPidBtn\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert startPidBtn != null : "fx:id=\"startPidBtn\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert pidStatusLbl != null : "fx:id=\"pidStatusLbl\""
        + " was not injected: check your FXML file 'commander.fxml'.";
    assert throttleLbl != null : "fx:id=\"throttleLbl\""
            + " was not injected: check your FXML file 'commander.fxml'.";
    assert throttleSlider != null : "fx:id=\"throttleSlider\""
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

  public TextArea getLogTextArea() {
    return logTextArea;
  }
}
