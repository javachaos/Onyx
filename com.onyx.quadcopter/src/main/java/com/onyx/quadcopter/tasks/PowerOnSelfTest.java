package com.onyx.quadcopter.tasks;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.StartupState;

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

    public PowerOnSelfTest(final Controller c) {
        if (c != null) {
            controller = c;
        } else {
            throw new OnyxException("Controller null.");
        }
    }

    public StartupState test() {
        for (final Entry<DeviceID, Device> d : controller.getDevices()) {
            if (d.getValue().selfTest()) {
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
