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
package com.onyx.quadcopter.utils;

import java.util.Stack;
import java.util.concurrent.CountDownLatch;

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
    private static Stack<CountDownLatch> latchStack = new Stack<CountDownLatch>();

    /**
     * Unused Ctor.
     */
    private ThreadUtils() {
    }

    /**
     * Thread wait.
     * 
     * @param waitCount
     *            the number of countdowns to wait for.
     */
    public static void await(final int waitCount) {
        try {
            LOGGER.debug("Latch awaiting count down.");
            CountDownLatch latch = new CountDownLatch(waitCount);
            latchStack.push(latch);
            latch.await();
        } catch (InterruptedException e) {
            ExceptionUtils.fatalError(ThreadUtils.class, e);
        }
    }

    /**
     * Countdown.
     */
    public static void countDown() {
        if (!latchStack.isEmpty()) {
            CountDownLatch l = latchStack.pop();
            l.countDown();
            if (l.getCount() != 0) {
                latchStack.push(l);
            }
            LOGGER.debug("Latch count down complete.");
        }
    }

    /**
     * Release all latches and shutdown.
     */
    public static void shutdown() {
        while (!latchStack.isEmpty()) {
            CountDownLatch l = latchStack.pop();
            while (l.getCount() > 0) {
                l.countDown();
            }
            l = null;
        }
    }
}
