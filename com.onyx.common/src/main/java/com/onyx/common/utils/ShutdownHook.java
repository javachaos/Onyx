package com.onyx.common.utils;
/******************************************************************************
 * Copyright (c) 2014 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
   * Executor service.
   */
  private final ScheduledExecutorService ses;

  /**
   * Constructs a new shutdown hook.
   *
   * @param main the main thread.
   */
  public ShutdownHook(final ScheduledExecutorService ses, final Thread main) {
    mainThread = main;
    this.ses = ses;
  }

  @Override
  public final void run() {
    try {
      ses.awaitTermination(Constants.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
      mainThread.join();
    } catch (final InterruptedException e1) {
      LOGGER.error(e1.getMessage());
    }
  }
}
