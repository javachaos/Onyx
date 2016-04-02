package com.onyx.quadcopter.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.OnyxScheduledExecutor;
import com.onyx.quadcopter.utils.ShutdownHook;

public class Main {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static {
        try {
            System.load(Constants.LIB_MRAA_PATH);
        } catch (final UnsatisfiedLinkError e) {
            LOGGER.error("Could not load link library 'mraajava'.");
        }
    }

    /**
     * Thread coordinator.
     */
    public static final OnyxScheduledExecutor COORDINATOR = new OnyxScheduledExecutor(Constants.NUM_THREADS);

    public static void main(final String[] args) {

        final Controller controller = new Controller();
        final StateMonitor monitor = new StateMonitor(controller);
        Main.COORDINATOR.scheduleAtFixedRate(monitor, Constants.MONITOR_DELAY, Constants.MONITOR_PERIOD,
                Constants.MONITOR_TIMEUNIT);
        Main.COORDINATOR.scheduleAtFixedRate(controller, Constants.CONTROLLER_PERIOD, Constants.CONTROLLER_PERIOD,
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
