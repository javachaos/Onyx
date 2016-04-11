package com.onyx.quadcopter.devices;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

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
     * Messages.
     */
    private ConcurrentHashMap<DeviceID, String> msgs = new ConcurrentHashMap<DeviceID, String>();
    
    /**
     * Current Device to display.
     */
    private DeviceID currentDisplay;
    
    /**
     * Iterator.
     */
    private Iterator<DeviceID> iterator = msgs.keySet().iterator();
    /**
     * The current string to display.
     */
    private String dispStr = null;

    public OLEDDevice(final Controller c) {
	super(c, DeviceID.OLED_DEVICE);
    }

    @Override
    protected void update() {
	if (msgs.size() >= Constants.OLED_MAX_MSGS) {
	    msgs.clear();
	}
	if (isNewMessage()) {
	    switch (lastMessage.getActionID()) {
	    case PRINT:
		oled.write(lastMessage.getContent());
		break;
	    case DISPLAY:
		msgs.put(lastMessage.getSender(), lastMessage.getContent());
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
	dispStr = msgs.getOrDefault(currentDisplay, dispStr);
	if (dispStr != null && !dispStr.isEmpty()) {
	    oled.write(dispStr);
	}
    }
    
    /**
     * Shift to the next message to display.
     */
    private synchronized void incrementDisplay() {
	currentDisplay = iterator.next();
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
