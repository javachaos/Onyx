/******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.onyx.quadcopter.tasks;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.Main;
import com.onyx.quadcopter.main.StateMonitor;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;
import com.pi4j.io.gpio.GpioFactory;

/**
 * Shutdown hook.
 *
 * @author fred
 *
 */
public class ShutdownHook extends Thread {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

    /**
     * Main thread reference.
     */
    private final Thread mainThread;

    /**
     * Constructs a new shutdown hook.
     *
     * @param main
     *            the main thread.
     */
    public ShutdownHook(final Thread main) {
	mainThread = main;
    }

    @Override
    public final void run() {
	try {
	    StateMonitor.shutdownState();
	    Main.COORDINATOR.awaitTermination(Constants.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
	    GpioFactory.getExecutorServiceFactory().getGpioEventExecutorService().awaitTermination(Constants.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
	    GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().awaitTermination(Constants.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
	    GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().shutdown();
	    GpioFactory.getExecutorServiceFactory().getGpioEventExecutorService().shutdown();
	    GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().shutdownNow();
	    GpioFactory.getExecutorServiceFactory().getGpioEventExecutorService().shutdownNow();
	    GpioFactory.getDefaultProvider().shutdown();
	    mainThread.join();
	} catch (final InterruptedException e) {
	    ExceptionUtils.logError(ShutdownHook.class, e);
	}
    }
}
