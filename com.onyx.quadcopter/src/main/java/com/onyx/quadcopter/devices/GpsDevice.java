package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;

import de.taimos.gpsd4java.api.IObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.SATObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GpsDevice extends Device implements IObjectListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(GpsDevice.class);
  private String dataBuffer;
  private GPSdEndpoint gps;

  public GpsDevice() {
    super(DeviceId.GPS_DEVICE);
  }

  @Override
  protected void update() {
  }

  @Override
  public void update(AclMessage msg) {
    switch (msg.getActionId()) {
      case SEND_DATA:
        sendReply(dataBuffer);
        break;
      default:
        break;
    }
  }

  @Override
  protected void init() {
    try {
      gps = new GPSdEndpoint(Constants.LOCALHOST, Constants.GPSD_PORT, new ResultParser());
      gps.addListener(this);
      gps.start();
    } catch (IOException e1) {
      LOGGER.error(e1.getMessage());
    }
  }

  @Override
  public void shutdown() {
    gps.stop();
  }

  @Override
  protected void alternate() {
    LOGGER.debug(dataBuffer);
    sendMessage(DeviceId.OLED_DEVICE, dataBuffer,
        ActionId.DISPLAY);
  }

  @Override
  public boolean selfTest() {
    return true;
  }

  @Override
  public void handleTPV(final TPVObject tpv) {
    LOGGER.info("TPV: {}", tpv);
  }
  
  @Override
  public void handleSKY(final SKYObject sky) {
    LOGGER.info("SKY: {}", sky);
    for (final SATObject sat : sky.getSatellites()) {
      LOGGER.info("  SAT: {}", sat);
    }
  }
  
  @Override
  public void handleSUBFRAME(final SUBFRAMEObject subframe) {
    LOGGER.info("SUBFRAME: {}", subframe);
  }
  
  @Override
  public void handleATT(final ATTObject att) {
    LOGGER.info("ATT: {}", att);
  }
  
  @Override
  public void handleDevice(final DeviceObject device) {
    LOGGER.info("Device: {}", device);
  }
  
  @Override
  public void handleDevices(final DevicesObject devices) {
    for (final DeviceObject d : devices.getDevices()) {
      LOGGER.info("Device: {}", d);
    }
  }

}
