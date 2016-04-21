package com.onyx.quadcopter.main;

import com.google.common.collect.MapMaker;

import com.onyx.quadcopter.communication.OnyxServer;
import com.onyx.quadcopter.control.PidController;
import com.onyx.quadcopter.control.RedButton;
import com.onyx.quadcopter.devices.Blackboard;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceId;
import com.onyx.quadcopter.devices.GpsDevice;
import com.onyx.quadcopter.devices.GyroMagAcc;
import com.onyx.quadcopter.devices.Motor;
import com.onyx.quadcopter.devices.OledDevice;
import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.messaging.AclMessage;
import com.onyx.quadcopter.tasks.Task;
import com.onyx.quadcopter.utils.Cleaner;
import com.onyx.quadcopter.utils.Constants;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Controller class.
 *
 * @author fred
 *
 */
public final class Controller extends Device implements Runnable, StartStopable {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

  /**
   * Devices array.
   */
  private ConcurrentMap<DeviceId, Device> devices;

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
  private OnyxServer commServer;

  /**
   * Singleton Reference.
   */
  private static Controller reference;

  /**
   * Private Controller ctor.
   */
  private Controller() {
    super();
    devices = new MapMaker().concurrencyLevel(Constants.NUM_THREADS)
        .initialCapacity(Constants.MAX_DEVICES).makeMap();
  }

  /**
   * Get a reference to this controller.
   * 
   * @return the controller singleton.
   */
  public static synchronized Controller getInstance() {
    if (reference == null) {
      reference = new Controller();
    }
    return reference;
  }

  @Override
  protected void init() {
    blackboard = new Blackboard();
    commServer = new OnyxServer();
    Main.COORDINATOR.schedule(commServer, 0, TimeUnit.SECONDS);
    setGpio(GpioFactory.getInstance());
    cleaner = new Cleaner();
    addDevice(commServer);
    addDevice(new RedButton());
    addDevice(new OledDevice());
    addDevice(new GyroMagAcc());
    addDevice(new GpsDevice());
    addDevice(new Motor(DeviceId.MOTOR1, Constants.GPIO_MOTOR1));
    addDevice(new Motor(DeviceId.MOTOR2, Constants.GPIO_MOTOR2));
    addDevice(new Motor(DeviceId.MOTOR3, Constants.GPIO_MOTOR3));
    addDevice(new Motor(DeviceId.MOTOR4, Constants.GPIO_MOTOR4));
    addDevice(new PidController());
    devices.values()
      .parallelStream()
      .filter(e -> !e.isInitialized())
      .forEachOrdered(e -> e.initialize());
  }

  @Override
  public void shutdown() {
    LOGGER.debug("Starting Controller shutdown...");
    blackboard.shutdown();
    devices.values().parallelStream().forEachOrdered(d -> {
      LOGGER.debug("Shutting down: " + d.toString());
      d.shutdown();
      cleaner.cleanUp(d);
      LOGGER.debug("Shutdown complete for: " + d.toString());
    });
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
   * @return the blackboard instance.
   */
  public Blackboard getBlackboard() {
    return blackboard;
  }

  /**
   * Add a device to the devices list.
   *
   * @param dev the device to add.
   */
  public void addDevice(final Device dev) {
    if (dev != null) {
      if (deviceCount++ < Constants.MAX_DEVICES) {
        devices.put(dev.getId(), dev);
        LOGGER.debug("Device " + dev + " added to controller.");
      } else {
        throw new OnyxException("Max devices exceeded.", LOGGER);
      }
    } else {
      throw new OnyxException("Attempting to add null device to Controller.", LOGGER);
    }
  }

  /**
   * Get a device instance from this controller.
   * 
   * @param dev the deviceid for the device returned.
   * @return the device associated with deviceId d.
   */
  public Device getDevice(final DeviceId dev) {
    final Device mdev = devices.get(dev);
    if (mdev != null) {
      return devices.get(dev);
    } else {
      throw new OnyxException("Device not found for DeviceID: " + dev, LOGGER);
    }
  }

  /**
   * Remove a device from this Controller.
   * 
   * @param deviceId the device to be removed.
   */
  public void removeDevice(final DeviceId deviceId) {
    if (devices.containsKey(deviceId)) {
      LOGGER.info("Removed device, " + getDevice(deviceId) + " from Controller.");
      devices.remove(deviceId);
    } else {
      LOGGER.info("Device map does not contain DeviceID:" + deviceId + ".");
    }
  }

  /**
   * Pause a running Controller.
   */
  public synchronized void pause() {
    isRunning = false;
  }

  /**
   * Resume a paused Controller.
   */
  public synchronized void resume() {
    isRunning = true;
  }

  /**
   * Get device set.
   * 
   * @return the set of devices.
   */
  public Set<Entry<DeviceId, Device>> getDevices() {
    return devices.entrySet();
  }

  /**
   * Return the state of this controller.
   *
   * @return true if this Controller isRunning.
   */
  public synchronized boolean isRunning() {
    return isRunning;
  }

  /**
   * Get GPIO Controller.
   * @return the gpio
   */
  public GpioController getGpio() {
    return gpio;
  }

  /**
   * Set GPIO Controller.
   * @param mpGpio the gpio to set
   */
  public void setGpio(final GpioController mpGpio) {
    this.gpio = mpGpio;
  }

  /**
   * Execute a high level Task. Future is discarded.
   * 
   * @param task the task to execute.
   * @param <T> the return type of the task t.
   * 
   * @return the Future of task t.
   */
  public <T> Future<T> executeTask(final Task<T> task) {
    return Main.COORDINATOR.submit(task);
  }

  @Override
  protected void alternate() {}

  @Override
  public boolean selfTest() {
    return isInitialized();
  }

  @Override
  public void run() {
    if (isRunning() && isInitialized()) {
      update();
      blackboard.update();
    }
  }
  
  @Override
  public void update(final AclMessage msg) {
    // UNUSED
  }

  @Override
  protected synchronized void update() {
    devices.values().parallelStream().filter(e -> e.isInitialized()).forEach(e -> e.execute());
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
}
