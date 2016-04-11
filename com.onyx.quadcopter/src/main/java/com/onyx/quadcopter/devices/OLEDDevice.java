package com.onyx.quadcopter.devices;

import java.io.IOException;

import com.onyx.quadcopter.display.Display;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.impl.I2CBusImpl;

public class OLEDDevice extends Device {

    /**
     * OLED Device driver.
     */
    private Display oled;

    /**
     * Message index.
     */
    private int msgIndex = 0;

    /**
     * Display counter.
     */
    private int msgDispIndex = 0;

    /**
     * Small message buffer.
     */
    private String[] msgs = new String[Constants.OLED_MAX_MSGS];

    public OLEDDevice(final Controller c) {
	super(c, DeviceID.OLED_DEVICE);
    }

    @Override
    protected void update() {
	if (msgIndex >= Constants.OLED_MAX_MSGS) {
	    msgIndex = 0;
	}
	if (isNewMessage()) {
	    switch (lastMessage.getActionID()) {
	    case PRINT:
		oled.write(lastMessage.getContent());
		break;
	    case DISPLAY:
		msgs[msgIndex++] = lastMessage.getContent();
		break;
	    case CHANGE_DISPLAY:
		incrementDisplay();
		show();
		break;
	    default:
		break;
	    }
	}
    }

    @Override
    protected void init() {
	try {
	    oled = new Display(128, 32, getController().getGpio(), I2CBusImpl.getBus(1), 0x3c, RaspiPin.GPIO_25);
	    oled.begin();
	    oled.dim(false);
	    oled.write("OLED Initialized.");
	} catch (IOException | ReflectiveOperationException e) {
	    ExceptionUtils.logError(getClass(), e);
	}
    }

    @Override
    public void shutdown() {
	oled.clear();
    }

    /**
     * Display the next msg from the msg list.
     */
    private void show() {
	String msg = msgs[msgDispIndex];
	if (msg != null && !msg.isEmpty()) {
	    oled.write(msg);
	}
    }
    
    /**
     * Shift to the next message to display.
     */
    private void incrementDisplay() {
        if (++msgDispIndex >= msgs.length) {
            msgDispIndex = 0;
        }
    }

    @Override
    protected void alternate() {
	show();
    }

    @Override
    public boolean selfTest() {
	return true;
    }

}
