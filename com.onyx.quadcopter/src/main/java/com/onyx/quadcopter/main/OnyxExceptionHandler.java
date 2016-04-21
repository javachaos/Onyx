package com.onyx.quadcopter.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;


public class OnyxExceptionHandler implements UncaughtExceptionHandler {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(OnyxExceptionHandler.class);

  @Override
  public void uncaughtException(final Thread thread, final Throwable ex) {
    thread.interrupt();
    LOGGER.error("Error: " + ex.toString());
  }

}
