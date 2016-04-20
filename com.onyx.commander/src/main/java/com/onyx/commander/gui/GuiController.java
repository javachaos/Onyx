package com.onyx.commander.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.communication.OnyxClient;
import com.onyx.commander.main.Main;
import com.onyx.commander.utils.Constants;
import com.onyx.quadcopter.devices.Blackboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class GuiController {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(GuiController.class);

    @FXML
    private TitledPane mainPane;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button connectButton;

    @FXML
    private Label connStatusLbl;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField ipField;

    @FXML
    private TitledPane cameraTitledPane;

    @FXML
    private Label orientationLbl;

    @FXML
    private Label hspeedLbl;

    @FXML
    private Label vspeedLbl;

    @FXML
    private TextArea logTextArea;

    @FXML
    private Button enterCommandButton;

    @FXML
    private TextField commandTextField;

    @FXML
    private TextArea commandOutputTextArea;

    private OnyxClient client;

    @FXML
    private VBox cameraVBox;

    @FXML
    private ImageView cameraImageView;

//    /**
//     * MJpeg stream.
//     */
//    private MjpegInputStream stream;

    /**
     * IP Regex.
     */
    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
	    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
	    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static final String CMD_REGEX = "[a-zA-Z0-9\\.-]*:[a-zA-Z0-9\\.-]*";

    private static Blackboard blackboard = new Blackboard();

    public GuiController() {
    }

    /**
     * Return a reference of the blackboard.
     * 
     * @return
     */
    public static Blackboard getBlackboard() {
	return blackboard;
    }

    @FXML
    protected void connect(final ActionEvent event) {
	//Authenticator.setDefault(new HTTPAuthenticator(usernameField.getText(), passField.getText()));
	String ip = ipField.getText();
	if (ip.matches(IPADDRESS_PATTERN)) {
	    client = new OnyxClient(ip, Constants.SERVER_PORT);
	    LOGGER.debug("Connecting to " + ip);
	    Main.COORDINATOR.submit(client);
	    loadWebview(ip);
	} else {
	    LOGGER.debug("Cannot connect non valid IP address.");
	}
    }

    /**
     * Load the webview.
     * 
     * @param ip
     *            the ip address.
     */
    private void loadWebview(String ip) {
	String url = "http://" + ip + ":8080/stream/video.mjpeg";
	VLCDirectRendering vlcdr = new VLCDirectRendering(url);
	cameraVBox.getChildren().clear();
	cameraVBox.getChildren().add(vlcdr);
	Main.COORDINATOR.submit(vlcdr);
    }
    
    

    @FXML
    protected void sendCommand() {
	String cmd = commandTextField.getText();
	if (cmd.matches(CMD_REGEX)) {
            client.addMessage(cmd);
	} else {
	    LOGGER.debug("Invalid command format.");
	}
    }

    @FXML
    void initialize() {
	assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'commander.fxml'.";
	assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'commander.fxml'.";
	assert connectButton != null : "fx:id=\"connectButton\" was not injected: check your FXML file 'commander.fxml'.";
	assert connStatusLbl != null : "fx:id=\"connStatusLbl\" was not injected: check your FXML file 'commander.fxml'.";
	assert usernameField != null : "fx:id=\"usernameField\" was not injected: check your FXML file 'commander.fxml'.";
	assert passField != null : "fx:id=\"passField\" was not injected: check your FXML file 'commander.fxml'.";
	assert ipField != null : "fx:id=\"ipField\" was not injected: check your FXML file 'commander.fxml'.";
	assert cameraTitledPane != null : "fx:id=\"cameraTitledPane\" was not injected: check your FXML file 'commander.fxml'.";
	assert cameraVBox != null : "fx:id=\"cameraVBox\" was not injected: check your FXML file 'commander.fxml'.";
	assert cameraImageView != null : "fx:id=\"cameraImageView\" was not injected: check your FXML file 'commander.fxml'.";
	assert orientationLbl != null : "fx:id=\"orientationLbl\" was not injected: check your FXML file 'commander.fxml'.";
	assert hspeedLbl != null : "fx:id=\"hspeedLbl\" was not injected: check your FXML file 'commander.fxml'.";
	assert vspeedLbl != null : "fx:id=\"vspeedLbl\" was not injected: check your FXML file 'commander.fxml'.";
	assert logTextArea != null : "fx:id=\"logTextArea\" was not injected: check your FXML file 'commander.fxml'.";
	assert enterCommandButton != null : "fx:id=\"enterCommandButton\" was not injected: check your FXML file 'commander.fxml'.";
	assert commandTextField != null : "fx:id=\"commandTextField\" was not injected: check your FXML file 'commander.fxml'.";
	assert commandOutputTextArea != null : "fx:id=\"commandOutputTextArea\" was not injected: check your FXML file 'commander.fxml'.";
	cameraImageView.setImage(new Image(GuiController.class.getResourceAsStream("/default.jpg")));

    }
}
