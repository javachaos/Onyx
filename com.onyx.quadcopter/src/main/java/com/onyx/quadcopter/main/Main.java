package com.onyx.quadcopter.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ShutdownHook;

public class Main {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Thread coordinator.
     */
    public static final ScheduledExecutorService COORDINATOR = Executors.newSingleThreadScheduledExecutor();

    public static void main(final String[] args) {
        final StateMonitor monitor = new StateMonitor();
        Main.COORDINATOR.scheduleAtFixedRate(monitor, Constants.MONITOR_DELAY, Constants.MONITOR_PERIOD,
                Constants.MONITOR_TIMEUNIT);
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
    public static void appStart() {
        final AppStart start = new AppStart();
        start.start();
    }
}
