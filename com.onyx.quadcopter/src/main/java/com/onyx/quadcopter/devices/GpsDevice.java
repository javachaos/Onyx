package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.quadcopter.utils.GpsProcessor;

import net.sf.marineapi.nmea.event.AbstractSentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.GGASentence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpsDevice extends Device {

  private static final Logger LOGGER = LoggerFactory.getLogger(GpsDevice.class);
  private GGASentence lastSent;
  private GpsProcessor gps;
  
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
    gps = new GpsProcessor();
    gps.start();
    final SentenceReader reader = new SentenceReader(gps.getInputStream());
    reader.addSentenceListener(new GgaListener());
    reader.start();
  }

  @Override
  public void shutdown() {
    gps.stop();
  }

  @Override
  protected void alternate() {
    LOGGER.debug(lastSent.toSentence());
    sendMessage(DeviceId.OLED_DEVICE, "Fix Quality: " + lastSent.getFixQuality(),
        ActionId.DISPLAY);
  }

  @Override
  public boolean selfTest() {
    return true;
  }  

  private class GgaListener extends AbstractSentenceListener<GGASentence> {
    @Override
    public void sentenceRead(GGASentence sentence) {
      if (sentence != null && sentence.isValid()) {
        LOGGER.trace("GGA position: " + sentence.getPosition());
        lastSent = sentence;
      } else {
        LOGGER.error("Invalid NMEA sentence.");
      }
    }
  }

}
