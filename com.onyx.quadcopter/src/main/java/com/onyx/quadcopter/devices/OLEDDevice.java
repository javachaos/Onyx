package com.onyx.quadcopter.devices;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.onyx.quadcopter.display.Display;
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
     * Thread safe counter.
     */
    private AtomicInteger counter = new AtomicInteger();

    /**
     * Creates a new OLED Device.
     * @param c controller.
     */
    public OLEDDevice() {
	super(DeviceID.OLED_DEVICE);
    }

    @Override
    public void update(final ACLMessage msg) {
	if (msgs.size() >= Constants.OLED_MAX_MSGS) {
	    msgs.clear();
	}
	switch (msg.getActionID()) {
        case PRINT:
	    oled.write(msg.getContent());
	    break;
        case DISPLAY:
            msgs.put(msg.getSender(), msg.getContent());
	    break;
        case CHANGE_DISPLAY:
	    showNext();
	    break;
        default:
            break;
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

    private void incrementCounter() {
        if (counter.incrementAndGet() >= DeviceID.values().length) {
	    counter.set(0);
        }
    }
    /**
     * Display the next msg from the msg list.
     */
    private void showNext() {
	incrementCounter();
	while (msgs.get(DeviceID.values()[counter.get()]) == null) {
	    incrementCounter();
	}
	show();
    }
    
    private void show() {
        oled.write(msgs.get(DeviceID.values()[counter.get()]));
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
