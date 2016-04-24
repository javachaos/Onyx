package com.onyx.quadcopter.main;

import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.onyx.common.utils.ShutdownHook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  static {
    try {
      System.load(Constants.MRAA_NATIVE_LIB);
    } catch (final UnsatisfiedLinkError e1) {
      LOGGER.error("Could not load link library 'mraajava'.");
      ExceptionUtils.logError(Main.class, e1);
    }
  }

  public static final ScheduledExecutorService COORDINATOR =
      Executors.newScheduledThreadPool(Constants.NUM_THREADS);

  /**
   * Main method. Application Entry Point.
   */
  public static void main(final String[] args) {
    begin();
  }
  
  /**
   * Begin execution.
   */
  static void begin() {
    Thread.setDefaultUncaughtExceptionHandler(new OnyxExceptionHandler());
    final Controller controller = Controller.getInstance();
    final StateMonitor monitor = new StateMonitor(controller);
    Main.COORDINATOR.scheduleWithFixedDelay(monitor, Constants.MONITOR_DELAY,
        Constants.MONITOR_PERIOD, Constants.MONITOR_TIMEUNIT);
    Main.COORDINATOR.scheduleAtFixedRate(controller, Constants.CONTROLLER_DELAY,
        Constants.CONTROLLER_PERIOD, Constants.CONTROLLER_TIMEUNIT);
    addHook();
  }

  /**
   * Shutdown hook.
   */
  private static void addHook() {
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(COORDINATOR, Thread.currentThread()));
  }

  /**
   * Start application.
   */
  public static void appStart(final Controller controller) {
    final AppStart start = new AppStart(controller);
    start.start();
  }
}
