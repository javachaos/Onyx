package com.onyx.commander.gui;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.commander.communication.OnyxClient;
import com.onyx.commander.main.Main;
import com.onyx.commander.utils.Constants;
import com.onyx.quadcopter.devices.Blackboard;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;

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

    private static final String CMD_REGEX = "([0-9]+):[a-zA-Z0-9\\.-]*:([0-9\\.]*):([0-9]+):([0-9]+)";

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
	Authenticator.setDefault(new HTTPAuthenticator(usernameField.getText(), passField.getText()));
	String ip = ipField.getText();
	if (ip.matches(IPADDRESS_PATTERN)) {
	    loadWebview(ip);
	    client = new OnyxClient(ip, Constants.SERVER_PORT);
	    LOGGER.debug("Connecting to " + ip);
	    Main.COORDINATOR.schedule(client, 0, TimeUnit.SECONDS);
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
	VLCDirectRendering vlcdr = new VLCDirectRendering();
	cameraVBox.getChildren().clear();
	cameraVBox.getChildren().add(vlcdr);
	showFPV(ip, vlcdr);
    }
    
    

    @FXML
    protected void sendCommand() {
	String cmd = commandTextField.getText();
	if (cmd.matches(CMD_REGEX)) {
	    String[] data = cmd.split(":");
	    int id = Integer.parseInt(data[0]);
	    DeviceID d = DeviceID.values()[id];
	    sendMessage(d, data[1], Double.parseDouble(data[2]), ActionId.values()[Integer.parseInt(data[3])],
		    MessageType.values()[Integer.parseInt(data[4])]);
	} else {
	    LOGGER.debug("Invalid command format.");
	}
    }

    /**
     * Send an ACLMessage to the connected server.
     * 
     * @param m
     */
    protected void sendMessage(ACLMessage m) {
	if (m.isValid()) {
	    blackboard.addMessage(m);
	}
    }

    /**
     * Send a message over the network pipe.
     * 
     * @param dev
     *            to device.
     * @param content
     *            content of the message to send.
     * @param value
     *            value of the message
     * @param intent
     *            message intent
     * @param type
     *            message type
     * 
     */
    protected void sendMessage(DeviceID dev, String content, double value, ActionId intent, MessageType type) {
	final ACLMessage m = new ACLMessage(type);
	m.setActionID(intent);
	m.setContent(content);
	m.setReciever(dev);
	m.setSender(DeviceID.COMM_CLIENT);
	sendMessage(m);
    }
    
    private void showFPV(final String ip, VLCDirectRendering vlcdr) {
	String url = "http://" + ip + ":8080/stream/video.mjpeg";
	vlcdr.start(url);
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

    static class HTTPAuthenticator extends Authenticator {
	private String username, password;

	public HTTPAuthenticator(String user, String pass) {
	    username = user;
	    password = pass;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
	    System.out.println("Requesting Host  : " + getRequestingHost());
	    System.out.println("Requesting Port  : " + getRequestingPort());
	    System.out.println("Requesting Prompt : " + getRequestingPrompt());
	    System.out.println("Requesting Protocol: " + getRequestingProtocol());
	    System.out.println("Requesting Scheme : " + getRequestingScheme());
	    System.out.println("Requesting Site  : " + getRequestingSite());
	    return new PasswordAuthentication(username, password.toCharArray());
	}
    }
}
