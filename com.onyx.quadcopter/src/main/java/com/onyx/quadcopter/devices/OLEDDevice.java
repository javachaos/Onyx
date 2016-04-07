package com.onyx.quadcopter.devices;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

import upm_i2clcd.SSD1306;

public class OLEDDevice extends Device {

    /**
     * OLED Device driver.
     */
    private SSD1306 oled;

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
	oled = new SSD1306(Constants.I2C_BUS_ID);
    }

    @Override
    public void shutdown() {
	oled.clear();
	oled.delete();
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
	try {
	    Thread.sleep(1);
	} catch (InterruptedException e) {
	    LOGGER.error(e.getMessage());
	}
	return oled.clear() == 1;
    }

}
