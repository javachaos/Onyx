package com.onyx.quadcopter.main;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxExceptionHandler implements UncaughtExceptionHandler {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxExceptionHandler.class);

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
	t.interrupt();
	LOGGER.error("Error: " + e.toString());
    }

}
