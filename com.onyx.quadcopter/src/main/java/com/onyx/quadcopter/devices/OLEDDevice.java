package com.onyx.quadcopter.devices;

import java.io.IOException;

import com.onyx.quadcopter.display.Display;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;
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
	if(msgIndex >= Constants.OLED_MAX_MSGS) {
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
		    I2CBusImpl.getBus(1),
		    0x3c, RaspiPin.GPIO_25);
	    oled.begin();
	    oled.dim(false);
	    oled.write("OLED Initialized.");
	} catch (IOException | ReflectiveOperationException e) {
	    LOGGER.error(e.getMessage());
	}
    }

    @Override
    public void shutdown() {
	oled.clear();
    }

    @Override
    protected void alternate() {
	String msg = msgs[msgDispIndex++];
	if (msg != null && !msg.isEmpty()) {
	    oled.write(msg);
	}
	if(msgDispIndex >= msgs.length) {
	    msgDispIndex = 0;
	}
    }

    @Override
    public boolean selfTest() {
	return true;
    }

}
