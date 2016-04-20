package com.onyx.quadcopter.devices;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;

import upm_ublox6.Ublox6;

public class GPSDevice extends Device {

    private Ublox6 gps;
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private byte[] nmeaBuffer = new byte[Constants.NMEA_BUFF_SIZE];

    public GPSDevice() {
	super(DeviceID.GPS_DEVICE);
    }

    @Override
    protected void update() {
	if (gps.dataAvailable()) {
	    gps.readData(nmeaBuffer);
	}
    }

    @Override
    public void update(ACLMessage msg) {
	// TODO Complete msg handling.
    }

    @Override
    protected void init() {
	gps = new Ublox6(Constants.UART_PORT);
    }

    @Override
    public void shutdown() {
	gps.delete();
    }

    @Override
    protected void alternate() {
	try {
	    LOGGER.debug(new String(nmeaBuffer, Constants.ENCODING));
	    sendMessage(DeviceID.OLED_DEVICE, new String(nmeaBuffer, Constants.ENCODING), ActionId.DISPLAY);
	} catch (UnsupportedEncodingException e) {
	    ExceptionUtils.logError(getClass(), e);
	}
    }

    @Override
    public boolean selfTest() {
	return true;
    }

}
