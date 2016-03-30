package com.onyx.quadcopter.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ShutdownHook;

public class Main {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setThreadFactory(Executors.defaultThreadFactory()).setNameFormat("JOB-%d").setDaemon(false)
            .setPriority(Thread.MAX_PRIORITY).setUncaughtExceptionHandler(new OnyxExceptionHandler()).build();

    /**
     * Thread coordinator.
     */
    public static final ScheduledExecutorService COORDINATOR = Executors.newScheduledThreadPool(Constants.NUM_THREADS,
            threadFactory);

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
