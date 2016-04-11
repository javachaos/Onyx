package com.onyx.quadcopter.devices;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.utils.Constants;

import upm_ublox6.Ublox6;

public class GPSDevice extends Device {

    private Ublox6 gps;
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private byte[] nmeaBuffer = new byte[Constants.NMEA_BUFF_SIZE];

    public GPSDevice(final Controller c) {
	super(c, DeviceID.GPS_DEVICE);
    }

    @Override
    protected void update() {
	if (gps.dataAvailable()) {
	    gps.readData(nmeaBuffer);
	}
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
	    sendMessage(DeviceID.OLED_DEVICE,
		    new String(nmeaBuffer, Constants.ENCODING), ActionId.DISPLAY);
	} catch (UnsupportedEncodingException e) {
	    LOGGER.error(e.getMessage());
	}
    }

    @Override
    public boolean selfTest() {
	return true;
    }

}
