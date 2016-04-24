package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.onyx.quadcopter.display.Display;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.impl.I2CBusImpl;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an OLED Device.
 * 
 * @author fred
 *
 */
public class OledDevice extends Device {

  /**
   * OLED Device driver.
   */
  private Display oled;

  /**
   * Display Messages.
   */
  private ConcurrentHashMap<DeviceId, String> msgs =
      new ConcurrentHashMap<DeviceId, String>(Constants.OLED_MAX_MSGS);

  /**
   * Thread safe counter.
   */
  private AtomicInteger counter = new AtomicInteger();

  private int iterationCount;

  /**
   * Creates a new OLED Device.
   */
  public OledDevice() {
    super(DeviceId.OLED_DEVICE);
  }

  @Override
  public void update(final AclMessage msg) {
    if (msgs.size() >= Constants.OLED_MAX_MSGS) {
      msgs.clear();
    }
    switch (msg.getActionId()) {
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

    if (iterationCount++ == Constants.OLED_UPDATE_SPEED) {
      iterationCount = 0;
      show();
    }
  }

  @Override
  protected void init() {
    try {
      oled = new Display(128, 32, getController().getGpio(), I2CBusImpl.getBus(1), 0x3c,
          RaspiPin.GPIO_25);
      oled.begin();
      oled.dim(false);
      oled.write("OLED Initialized.");
    } catch (IOException | ReflectiveOperationException e1) {
      ExceptionUtils.logError(getClass(), e1);
    }
  }

  @Override
  public void shutdown() {
    oled.clear();
  }

  private void incrementCounter() {
    if (counter.incrementAndGet() >= DeviceId.values().length) {
      counter.set(0);
    }
  }

  /**
   * Display the next msg from the msg list.
   */
  private void showNext() {
    incrementCounter();
    while (msgs.get(DeviceId.values()[counter.get()]) == null) {
      incrementCounter();
    }
    show();
  }

  private void show() {
    oled.write(msgs.get(DeviceId.values()[counter.get()]));
  }

  @Override
  protected void alternate() {}

  @Override
  public boolean selfTest() {
    return true;
  }

}
