package com.onyx.quadcopter.main;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Blackboard;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.devices.Motor;
import com.onyx.quadcopter.exceptions.OnyxException;
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
    private final ConcurrentMap<DeviceID, Device> devices;

    /**
     * Number of devices.
     */
    private final int deviceCount = 0;

    /**
     * Blackbaord instance.
     */
    private final Blackboard blackboard;

    private Cleaner cleaner;

    private boolean isRunning = false;

    private volatile boolean initialized = false;

    public Controller() {
        devices = new ConcurrentHashMap<DeviceID, Device>(Constants.MAX_DEVICES);
        blackboard = new Blackboard(this);
        init();
    }

    private void init() {
        LOGGER.debug("Initializing Controller...");
        cleaner = new Cleaner();
        addDevice(blackboard);
        addDevice(new Motor(this, DeviceID.MOTOR1, Constants.GPIO_MOTOR1));
        addDevice(new Motor(this, DeviceID.MOTOR2, Constants.GPIO_MOTOR2));
        addDevice(new Motor(this, DeviceID.MOTOR3, Constants.GPIO_MOTOR3));
        addDevice(new Motor(this, DeviceID.MOTOR4, Constants.GPIO_MOTOR4));
        LOGGER.debug("Controller Initialized.");
        initialized = true;
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
        final Iterator<Entry<DeviceID, Device>> it = devices.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getValue().execute();
        }
    }

    public synchronized void start() {
        if (!initialized) {
            init();
        }
        isRunning = true;
    }

    public synchronized void stop() {
        isRunning = false;
        shutdown();
    }

    public Set<Entry<DeviceID, Device>> getDevices() {
        return devices.entrySet();
    }

    @Override
    public void run() {
        if (isRunning() && initialized) {
            update();
        }
    }

    /**
     * Return the state of this controller.
     *
     * @return
     */
    public synchronized boolean isRunning() {
        return isRunning;
    }
}
