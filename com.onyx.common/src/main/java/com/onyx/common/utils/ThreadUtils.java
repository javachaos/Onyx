package com.onyx.common.utils;
/******************************************************************************
 * Copyright (c) 2014 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Thread utilities.
 * 
 * @author fred
 *
 */
public final class ThreadUtils {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);

  /**
   * Count down latch stack.
   */
  private static ArrayBlockingQueue<CountDownLatch> latchStack = new ArrayBlockingQueue<CountDownLatch>(10);

  /**
   * Unused Ctor.
   */
  private ThreadUtils() {}

  /**
   * Thread wait.
   * 
   * @param waitCount the number of countdowns to wait for.
   */
  public static void await(final int waitCount) {
    try {
      LOGGER.debug("Latch awaiting count down.");
      CountDownLatch latch = new CountDownLatch(waitCount);
      latchStack.offer(latch);
      latch.await();
    } catch (InterruptedException e1) {
      ExceptionUtils.fatalError(ThreadUtils.class, e1);
    }
  }
  

  /**
   * Thread wait. With timeout value.
   * 
   * @param waitCount the number of countdowns to wait for.
   * @param timeout the number of timeunits to wait for before timing out.
   * @param unit the timeunit.
   */
  public static void await(final int waitCount, long timeout, TimeUnit unit) {
    try {
      LOGGER.debug("Latch awaiting count down.");
      CountDownLatch latch = new CountDownLatch(waitCount);
      latchStack.offer(latch);
      if (latch.await(timeout, unit)) {
        LOGGER.debug("No countdown required to latches to await.");
      }
    } catch (InterruptedException e1) {
      ExceptionUtils.fatalError(ThreadUtils.class, e1);
    }
  }

  /**
   * Countdown.
   */
  public static void countDown() {
    if (!latchStack.isEmpty()) {
      CountDownLatch latch = latchStack.poll();
      latch.countDown();
      if (latch.getCount() != 0) {
        latchStack.offer(latch);
      }
      LOGGER.debug("Latch count down complete.");
    }
  }

  /**
   * Release all latches and shutdown.
   */
  public static void shutdown() {
    while (!latchStack.isEmpty()) {
      CountDownLatch latch = latchStack.poll();
      while (latch.getCount() > 0) {
        latch.countDown();
      }
      latch = null;
    }
  }
}
