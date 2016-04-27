package com.onyx.quadcopter.devices;


import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.ExceptionListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.SentenceValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

public class GpsDevice extends Device {

  private static final Logger LOGGER = LoggerFactory.getLogger(GpsDevice.class);
  private GGASentence lastSent;
  private SentenceReader reader;

  public GpsDevice() {
    super(DeviceId.GPS_DEVICE);
  }

  @Override
  protected void update() {
    super.update();
  }

  @Override
  public void update(AclMessage msg) {
    switch (msg.getActionId()) {
      case SEND_DATA:
        sendReply(lastSent.toSentence());
        break;
      default:
        break;
    }

  }

  @Override
  protected void init() {
    SerialPort sp = getSerialPort();

    if (sp != null) {
      InputStream is = null;
      try {
        is = sp.getInputStream();
      } catch (IOException e1) {
        LOGGER.error(e1.getMessage());
      }
      reader = new SentenceReader(is);
      SentenceListener sl = new GgaListener();
      reader.addSentenceListener(sl);
      reader.setExceptionListener((ExceptionListener) sl);
      reader.start();
    }
  }

  @Override
  public void shutdown() {
    reader.stop();
  }

  @Override
  protected void alternate() {
    LOGGER.debug(lastSent.toSentence());
    sendMessage(DeviceId.OLED_DEVICE, "Fix Quality: " + lastSent.getFixQuality(), ActionId.DISPLAY);
  }

  @Override
  public boolean selfTest() {
    return true;
  }

  /**
   * Scan serial ports for NMEA data.
   * 
   * @return SerialPort from which NMEA data was found, or null if data was not found in any of the
   *         ports.
   */
  private SerialPort getSerialPort() {
    SerialPort sp = null;
    try {
      Enumeration<?> e1 = CommPortIdentifier.getPortIdentifiers();

      while (e1.hasMoreElements()) {
        CommPortIdentifier id = (CommPortIdentifier) e1.nextElement();

        if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
          sp = (SerialPort) id.open(Constants.GPS_DEVICE, 30);
          sp.setSerialPortParams(Constants.GPS_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
              SerialPort.PARITY_NONE);
          InputStream is = sp.getInputStream();
          InputStreamReader isr = new InputStreamReader(is);
          BufferedReader buf = new BufferedReader(isr);
          LOGGER.info("Scanning port " + sp.getName());

          // try each port few times before giving up
          for (int i = 0; i < 5; i++) {
            try {
              String data = buf.readLine();
              if (SentenceValidator.isValid(data)) {
                LOGGER.info("NMEA data found!");
                break;
              }
            } catch (Exception ex) {
              LOGGER.error(ex.getMessage());
            }
          }
          is.close();
          isr.close();
          buf.close();
        }
      }
      LOGGER.info("NMEA data was not found..");
    } catch (Exception e2) {
      LOGGER.error(e2.getMessage());
    }
    return sp;
  }

  private class GgaListener extends AbstractSentenceListener<GGASentence>
      implements ExceptionListener {
    @Override
    public void sentenceRead(GGASentence sentence) {
      if (sentence != null && sentence.isValid()) {
        LOGGER.trace("GGA position: " + sentence.getPosition());
        lastSent = sentence;
      } else {
        LOGGER.error("Invalid NMEA sentence.");
      }
    }

    @Override
    public void onException(Exception e1) { // Do nothing.
      e1.addSuppressed(e1);
    }

  }
}
