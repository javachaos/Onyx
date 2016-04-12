package com.onyx.quadcopter.devices;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

import com.onyx.quadcopter.display.Display;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.impl.I2CBusImpl;

/**
 * Represents an OLED Device.
 * @author fred
 *
 */
public class OLEDDevice extends Device {

    /**
     * OLED Device driver.
     */
    private Display oled;

    /**
     * Display Messages.
     */
    private ConcurrentHashMap<DeviceID, String> msgs = new ConcurrentHashMap<DeviceID, String>(Constants.OLED_MAX_MSGS);

    /**
     * The current device to show on OLED.
     */
    private DeviceID currentDevice;
    
    private EnumSet<DeviceID> deviceSet = EnumSet.allOf(DeviceID.class);

    /**
     * Creates a new OLED Device.
     * @param c controller.
     */
    public OLEDDevice(final Controller c) {
	super(c, DeviceID.OLED_DEVICE);
    }

    @Override
    protected void update() {
	if (msgs.size() >= Constants.OLED_MAX_MSGS) {
	    msgs.clear();
	}
	if (isNewMessage()) {
	    while(getMessages().size() > 0) {
                ACLMessage msg = getMessages().poll();
	        switch (msg.getActionID()) {
	        case PRINT:
		    oled.write(lastMessage.getContent());
		    break;
	        case DISPLAY:
	            msgs.put(lastMessage.getSender(), msg.getContent());
		    break;
	        case CHANGE_DISPLAY:
		    showNext();
		    break;
	        default:
	            break;
	        }
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
    private void showNext() {
	currentDevice = deviceSet.iterator().next();
        oled.write(msgs.get(currentDevice));
    }

    @Override
    protected void alternate() {
	showNext();
    }

    @Override
    public boolean selfTest() {
	return true;
    }

}
