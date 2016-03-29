package com.onyx.commander.gui;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.communication.NettyCommClient;
import com.onyx.commander.main.Main;
import com.onyx.commander.utils.Constants;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

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
    private ImageView cameraImageview;

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
    
    /**
     * IP Regex.
     */
    private static final String IPADDRESS_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    
    public GuiController() {
    }
    
    @FXML
    protected void connect(final ActionEvent event) {
	//Connect to the Onyx Quadcopter.
	String ip = ipField.getText();
	if (ip.matches(IPADDRESS_PATTERN)) {
	    LOGGER.debug("Connecting to "+ ip);
	    NettyCommClient client = new NettyCommClient(ip, Constants.SERVER_PORT);
	    Main.COORDINATOR.schedule(client, 1, TimeUnit.SECONDS);
	} else {
	    LOGGER.debug("Cannot connect non valid IP address.");
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
        assert cameraImageview != null : "fx:id=\"cameraImageview\" was not injected: check your FXML file 'commander.fxml'.";
        assert orientationLbl != null : "fx:id=\"orientationLbl\" was not injected: check your FXML file 'commander.fxml'.";
        assert hspeedLbl != null : "fx:id=\"hspeedLbl\" was not injected: check your FXML file 'commander.fxml'.";
        assert vspeedLbl != null : "fx:id=\"vspeedLbl\" was not injected: check your FXML file 'commander.fxml'.";
        assert logTextArea != null : "fx:id=\"logTextArea\" was not injected: check your FXML file 'commander.fxml'.";
        assert enterCommandButton != null : "fx:id=\"enterCommandButton\" was not injected: check your FXML file 'commander.fxml'.";
        assert commandTextField != null : "fx:id=\"commandTextField\" was not injected: check your FXML file 'commander.fxml'.";
        assert commandOutputTextArea != null : "fx:id=\"commandOutputTextArea\" was not injected: check your FXML file 'commander.fxml'.";

    }
}

