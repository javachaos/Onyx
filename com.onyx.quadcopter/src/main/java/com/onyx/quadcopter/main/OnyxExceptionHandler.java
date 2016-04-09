package com.onyx.quadcopter.main;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;

public class OnyxExceptionHandler implements UncaughtExceptionHandler {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxExceptionHandler.class);

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
	t.interrupt();
	throw new OnyxException(e.getMessage(), LOGGER);
    }

}
