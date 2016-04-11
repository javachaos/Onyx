package com.onyx.quadcopter.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.tasks.ShutdownHook;
import com.onyx.quadcopter.utils.Constants;

public class Main {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static {
	try {
	    System.load(Constants.MRAA_NATIVE_LIB);
	} catch (final UnsatisfiedLinkError e) {
	    LOGGER.error("Could not load link library 'mraajava'.");
	}
    }
    
    public static final ScheduledExecutorService COORDINATOR = Executors.newScheduledThreadPool(Constants.NUM_THREADS);

    public static void main(final String[] args) {
	Thread.setDefaultUncaughtExceptionHandler(new OnyxExceptionHandler());
	final Controller controller = new Controller();
	final StateMonitor monitor = new StateMonitor(controller);
	Main.COORDINATOR.scheduleWithFixedDelay(monitor, Constants.MONITOR_DELAY, Constants.MONITOR_PERIOD,
		Constants.MONITOR_TIMEUNIT);
	Main.COORDINATOR.scheduleAtFixedRate(controller, Constants.CONTROLLER_DELAY, Constants.CONTROLLER_PERIOD,
		Constants.CONTROLLER_TIMEUNIT);
	addHook();
    }

    /**
     * Shutdown hook.
     */
    private static void addHook() {
	Runtime.getRuntime().addShutdownHook(new ShutdownHook(Thread.currentThread()));
    }

    /**
     * Start application.
     */
    public static void appStart(final Controller c) {
	final AppStart start = new AppStart(c);
	start.start();
    }
}
