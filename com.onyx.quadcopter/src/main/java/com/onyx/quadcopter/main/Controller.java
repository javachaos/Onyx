package com.onyx.quadcopter.main;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.utils.Blackboard;
import com.onyx.quadcopter.utils.Cleaner;
import com.onyx.quadcopter.utils.Constants;

/**
 * Controller class.
 *
 * @author fred
 *
 */
public class Controller implements Runnable {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    /**
     * Devices array.
     */
    private final HashMap<DeviceID, Device> devices;

    /**
     * Number of devices.
     */
    private final int deviceCount = 0;

    /**
     * Blackbaord instance.
     */
    private final Blackboard blackboard;

    private Cleaner cleaner;
    private volatile boolean isRunning = false;

    public Controller() {
        devices = new HashMap<DeviceID, Device>(Constants.MAX_DEVICES);
        blackboard = new Blackboard(this);
        init();
    }

    private void init() {
        cleaner = new Cleaner();
        addDevice(blackboard);
        LOGGER.debug("Controller Initialized.");
    }

    private void shutdown() {
        LOGGER.debug("Starting Controller shutdown...");
        for (final Entry<DeviceID, Device> d : devices.entrySet()) {
            d.getValue().shutdown();
            cleaner.cleanUp(d.getValue());
        }
        cleaner.doClean();
        devices.clear();
        LOGGER.debug("Controller shutdown complete.");
    }

    /**
     * Return the blackboard.
     *
     * @return
     */
    public Blackboard getBlackboard() {
        return blackboard;
    }

    /**
     * Add a device to the devices list.
     *
     * @param d
     */
    public void addDevice(final Device d) {
        if (d != null) {
            if (deviceCount < Constants.MAX_DEVICES) {
                devices.put(d.getId(), d);
                LOGGER.debug("Device " + d + " added to controller.");
            } else {
                throw new OnyxException("Max devices exceeded.");
            }
        } else {
            throw new OnyxException("Attempting to add null device to Controller.");
        }
    }

    public Device getDevice(final DeviceID d) {
        return devices.get(d);
    }

    public void removeDevice(final DeviceID deviceId) {
        if (devices.containsKey(deviceId)) {
            LOGGER.info("Removed device, " + getDevice(deviceId) + " from Controller.");
            devices.remove(deviceId);
        } else {
            LOGGER.info("Device map does not contain DeviceID:" + deviceId + ".");
        }
    }

    private void update() {
        for (final Entry<DeviceID, Device> d : devices.entrySet()) {
            d.getValue().run();
        }
    }

    public void start() {
        init();
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
        shutdown();
    }

    public Set<Entry<DeviceID, Device>> getDevices() {
        return devices.entrySet();
    }

    @Override
    public void run() {
        if (isRunning) {
            update();
        }
    }
}
