package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import upm_ublox6.Ublox6;

import java.io.UnsupportedEncodingException;

public class GpsDevice extends Device {

  private Ublox6 gps;
  private static final Logger LOGGER = LoggerFactory.getLogger(GpsDevice.class);
  private byte[] nmeaBuffer = new byte[Constants.NMEA_BUFF_SIZE];

  public GpsDevice() {
    super(DeviceId.GPS_DEVICE);
  }

  @Override
  protected void update() {
    if (gps.dataAvailable()) {
      gps.readData(nmeaBuffer);
    }
  }

  @Override
  public void update(AclMessage msg) {
    switch (msg.getActionId()) {
      case SEND_DATA:
        try {
          sendReply(new String(nmeaBuffer, Constants.ENCODING));
        } catch (UnsupportedEncodingException e1) {
          LOGGER.error(e1.getMessage());
        }
        break;
      default:
        break;
    }
  }

  @Override
  protected void init() {
    gps = new Ublox6(Constants.UART_PORT);
    gps.setupTty();
  }

  @Override
  public void shutdown() {
    gps.delete();
  }

  @Override
  protected void alternate() {
    try {
      LOGGER.debug(new String(nmeaBuffer, Constants.ENCODING));
      sendMessage(DeviceId.OLED_DEVICE, new String(nmeaBuffer, Constants.ENCODING),
          ActionId.DISPLAY);
    } catch (UnsupportedEncodingException e1) {
      ExceptionUtils.logError(getClass(), e1);
    }
  }

  @Override
  public boolean selfTest() {
    return true;
  }

}
