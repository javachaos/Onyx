package com.onyx.quadcopter.main;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.onyx.quadcopter.control.RedButton;
import com.onyx.quadcopter.devices.Blackboard;
import com.onyx.quadcopter.devices.CameraDevice;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.devices.GyroMagAcc;
import com.onyx.quadcopter.devices.Motor;
import com.onyx.quadcopter.devices.NettyCommServer;
import com.onyx.quadcopter.devices.OLEDDevice;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.utils.Cleaner;
import com.onyx.quadcopter.utils.Constants;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

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
    
    /**
     * GPIO Controller.
     */
    private GpioController gpio;
    
    /**
     * I2C Bus.
     */
    private I2CBus i2cbus;

    /**
     * Communications server reference.
     */
    private final NettyCommServer commServer;

    public Controller() {
        devices = new MapMaker().concurrencyLevel(Constants.NUM_THREADS).initialCapacity(Constants.MAX_DEVICES)
                .makeMap();
        blackboard = new Blackboard();
        commServer = new NettyCommServer(this);
        Main.COORDINATOR.schedule(commServer, Constants.COMM_SERVER_INIT_DELAY, TimeUnit.SECONDS);
        init();
    }

    private void init() {
        LOGGER.debug("Initializing Controller...");
	setGpio(GpioFactory.getInstance());
	try {
	    setI2CBus(I2CFactory.getInstance(1));
	} catch (IOException e) {
	    LOGGER.error(e.getMessage());
	}
        cleaner = new Cleaner();
        addDevice(commServer);
        addDevice(new RedButton(this));
        addDevice(new OLEDDevice(this));
        addDevice(new GyroMagAcc(this));
        addDevice(new Motor(this, DeviceID.MOTOR1, Constants.GPIO_MOTOR1));
        addDevice(new Motor(this, DeviceID.MOTOR2, Constants.GPIO_MOTOR2));
        addDevice(new Motor(this, DeviceID.MOTOR3, Constants.GPIO_MOTOR3));
        addDevice(new Motor(this, DeviceID.MOTOR4, Constants.GPIO_MOTOR4));
        addDevice(new CameraDevice(this));
        LOGGER.debug("Controller Initialized.");
        for (final Entry<DeviceID, Device> d : devices.entrySet()) {
            d.getValue().initialize();
        }
        initialized = true;
    }

    private void shutdown() {
        LOGGER.debug("Starting Controller shutdown...");
        blackboard.shutdown();
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
                throw new OnyxException("Max devices exceeded.", LOGGER);
            }
        } else {
            throw new OnyxException("Attempting to add null device to Controller.", LOGGER);
        }
    }

    public Device getDevice(final DeviceID d) {
        final Device dev = devices.get(d);
        if (dev != null) {
            return devices.get(d);
        } else {
            throw new OnyxException("Device not found for DeviceID: " + d, LOGGER);
        }
    }

    public void removeDevice(final DeviceID deviceId) {
        if (devices.containsKey(deviceId)) {
            LOGGER.info("Removed device, " + getDevice(deviceId) + " from Controller.");
            devices.remove(deviceId);
        } else {
            LOGGER.info("Device map does not contain DeviceID:" + deviceId + ".");
        }
    }

    private synchronized void update() {
        final Iterator<DeviceID> it = devices.keySet().iterator();
        while (it.hasNext()) {
            getDevice(it.next()).execute();
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

    /**
     * Return the state of this controller.
     *
     * @return
     */
    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        if (isRunning() && initialized) {
            update();
            blackboard.update();
        }
    }

    /**
     * @return the gpio
     */
    public GpioController getGpio() {
	return gpio;
    }

    /**
     * @param gpio the gpio to set
     */
    public void setGpio(GpioController gpio) {
	this.gpio = gpio;
    }

    /**
     * @return the i2cbus
     */
    public I2CBus getI2CBus() {
	return i2cbus;
    }

    /**
     * @param i2cbus the i2cbus to set
     */
    public void setI2CBus(I2CBus i2cbus) {
	this.i2cbus = i2cbus;
    }
}
