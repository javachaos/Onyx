package com.onyx.quadcopter.tasks;

import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.StartupState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

/**
 * Test all devices calling selfTest() for each device in the controller.
 *
 * @author fred
 *
 */
public class PowerOnSelfTest {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(PowerOnSelfTest.class);

  /**
   * Controller reference.
   */
  private final Controller controller;

  /**
   * Power on self test Ctor.
   * @param controller
   *    the controller.
   */
  public PowerOnSelfTest(final Controller controller) {
    if (controller != null) {
      this.controller = controller;
    } else {
      throw new OnyxException("Controller null.", LOGGER);
    }
  }

  /**
   * Test each device.
   * 
   * @return
   *    the start up state.
   */
  public StartupState test() {
    if (Constants.SIMULATION) {
      return StartupState.SUCCESSFUL;
    }
    for (final Entry<DeviceId, Device> d : controller.getDevices()) {
      final Device dev = d.getValue();
      while (!dev.isInitialized()) {
        try {
          // Wait for the device to be initialized.
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          ExceptionUtils.logError(getClass(), e1);
        }
      }
      if (dev.selfTest()) {
        continue;
      } else {
        LOGGER.debug("Power on self test failed. Device: " + d + " did not pass.");
        return StartupState.UNSUCCESSFUL;
      }
    }
    LOGGER.debug("Power on self test completed successfully.");
    return StartupState.SUCCESSFUL;
  }

}
