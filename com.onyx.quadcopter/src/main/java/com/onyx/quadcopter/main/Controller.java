package com.onyx.quadcopter.main;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.onyx.quadcopter.control.PIDController;
import com.onyx.quadcopter.control.RedButton;
import com.onyx.quadcopter.devices.Blackboard;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.devices.GyroMagAcc;
import com.onyx.quadcopter.devices.Motor;
import com.onyx.quadcopter.devices.NettyCommServer;
import com.onyx.quadcopter.devices.OLEDDevice;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.tasks.Task;
import com.onyx.quadcopter.utils.Cleaner;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

/**
 * Controller class.
 *
 * @author fred
 *
 */
public class Controller extends Device implements Runnable, StartStopable {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    /**
     * Devices array.
     */
    private ConcurrentMap<DeviceID, Device> devices;

    /**
     * Number of devices.
     */
    private int deviceCount = 0;

    /**
     * Blackboard instance.
     */
    private Blackboard blackboard;

    /**
     * Object tasked with cleanup after shutdown.
     */
    private Cleaner cleaner;

    /**
     * True if the controller is running.
     */
    private boolean isRunning = false;

    /**
     * GPIO Controller.
     */
    private GpioController gpio;

    /**
     * Communications server reference.
     */
    private NettyCommServer commServer;
    
    /**
     * Singleton Reference.
     */
    private static Controller reference;

    /**
     * Private Controller ctor.
     */
    private Controller() {
	super();
	devices = new MapMaker().concurrencyLevel(Constants.NUM_THREADS).initialCapacity(Constants.MAX_DEVICES)
		.makeMap();
    }
    
    /**
     * Get a reference to this controller.
     * @return
     */
    public static synchronized Controller getInstance() {
	if (reference == null) {
	    reference = new Controller();
	}
	return reference;
    }

    @Override
    protected void init() {
	LOGGER.debug("Initializing Controller...");
	blackboard = new Blackboard();
	commServer = new NettyCommServer();
	Main.COORDINATOR.schedule(commServer, Constants.COMM_SERVER_INIT_DELAY, TimeUnit.SECONDS);
	setGpio(GpioFactory.getInstance());
	cleaner = new Cleaner();
	addDevice(commServer);
	addDevice(new RedButton());
	addDevice(new OLEDDevice());
	addDevice(new GyroMagAcc());
	addDevice(new Motor(DeviceID.MOTOR1, Constants.GPIO_MOTOR1));
	addDevice(new Motor(DeviceID.MOTOR2, Constants.GPIO_MOTOR2));
	addDevice(new Motor(DeviceID.MOTOR3, Constants.GPIO_MOTOR3));
	addDevice(new Motor(DeviceID.MOTOR4, Constants.GPIO_MOTOR4));
	addDevice(new PIDController());
	LOGGER.debug("Controller Initialized.");
	for (final Entry<DeviceID, Device> d : devices.entrySet()) {
	    d.getValue().initialize();
	}
    }

    @Override
    public void shutdown() {
	LOGGER.debug("Starting Controller shutdown...");
	blackboard.shutdown();
	for (final Entry<DeviceID, Device> d : devices.entrySet()) {
	    LOGGER.debug("Shutting down: " + d.getKey().toString());
	    d.getValue().shutdown();
	    cleaner.cleanUp(d.getValue());
	    LOGGER.debug("Shutdown complete for: " + d.getKey().toString());
	}
	gpio.shutdown();
	devices.clear();
	cleaner.cleanUp(gpio);
	cleaner.cleanUp(devices);
	cleaner.doClean();
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
     * 		the device to add.
     */
    public void addDevice(final Device d) {
	if (d != null) {
	    if (deviceCount++ < Constants.MAX_DEVICES) {
		devices.put(d.getId(), d);
		LOGGER.debug("Device " + d + " added to controller.");
	    } else {
		throw new OnyxException("Max devices exceeded.", LOGGER);
	    }
	} else {
	    throw new OnyxException("Attempting to add null device to Controller.", LOGGER);
	}
    }

    /**
     * Get a device instance from this controller
     * @param d
     * 		the deviceid for the device returned.
     * @return
     * 		the device associated with deviceId d.
     */
    public Device getDevice(final DeviceID d) {
	final Device dev = devices.get(d);
	if (dev != null) {
	    return devices.get(d);
	} else {
	    throw new OnyxException("Device not found for DeviceID: " + d, LOGGER);
	}
    }

    /**
     * Remove a device from this Controller.
     * @param deviceId
     * 		the device to be removed.
     */
    public void removeDevice(final DeviceID deviceId) {
	if (devices.containsKey(deviceId)) {
	    LOGGER.info("Removed device, " + getDevice(deviceId) + " from Controller.");
	    devices.remove(deviceId);
	} else {
	    LOGGER.info("Device map does not contain DeviceID:" + deviceId + ".");
	}
    }

    @Override
    protected synchronized void update() {
	//No call to super.update() for we do not wish to adapt our behavior from super.
//	final Iterator<DeviceID> it = devices.keySet().iterator();
	try {
	    devices.values().parallelStream().filter(e -> e.isInitialized())
	                                     .forEach(e -> e.execute());                               
	    //devices.forEach((id,dev) -> dev.execute());
	} catch (Throwable t) {
            ExceptionUtils.logError(getClass(), t);
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
	}
//	while (it.hasNext()) {
//	    Device d = getDevice(it.next());
//	    try {
//	        d.execute();
//	    } catch (Throwable t) {
//		ExceptionUtils.logError(d.getClass(), t);
//		Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
//	    }
//	}
    }

    @Override
    public synchronized void start() {
	initialize();
	isRunning = true;
    }

    @Override
    public synchronized void stop() {
	isRunning = false;
	shutdown();
    }

    public synchronized void pause() {
	isRunning = false;
    }
    
    public synchronized void resume() {
	isRunning = true;
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
	if (isRunning() && isInitialized()) {
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
     * @param gpio
     *            the gpio to set
     */
    public void setGpio(GpioController gpio) {
	this.gpio = gpio;
    }

    /**
     * Execute a high level Task.
     * Future is discarded.
     * @param t
     * 		the task to execute.
     */
    public void executeTask(Task<?> t) {
	Main.COORDINATOR.submit(t);
    }

    @Override
    protected void alternate() {
    }

    @Override
    public boolean selfTest() {
	return isInitialized();
    }

    @Override
    public void update(ACLMessage msg) {
	//UNUSED
    }
}
