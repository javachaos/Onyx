package com.onyx.quadcopter.devices;

import java.io.IOException;

import com.onyx.quadcopter.display.Display;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;
import com.pi4j.io.gpio.RaspiPin;

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
	if(msgIndex > Constants.OLED_MAX_MSGS) {
	    msgIndex = 0;
	}
	if(isNewMessage()) {
	    msgs[msgIndex++] = lastMessage.getContent();
	}
    }

    @Override
    protected void init() {
	try {
	    oled = new Display(128, 32,
		    getController().getGpio(),
		    getController().getI2CBus(),
		    0x3C, RaspiPin.GPIO_25);
	} catch (ReflectiveOperationException | IOException e) {
	    LOGGER.error(e.getMessage());
	}
    }

    @Override
    public void shutdown() {
	oled.clear();
    }

    @Override
    protected void alternate() {
	oled.clear();
	oled.write(msgs[msgDispIndex++]);
	if(msgDispIndex >= msgs.length) {
	    msgDispIndex = 0;
	}
    }

    @Override
    public boolean selfTest() {
	oled.write("Testing...");
	return true;
    }

}
