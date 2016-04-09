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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.main.StateMonitor;

/**
 * Helper class to help with exception code.
 * 
 * @author fred
 *
 */
public final class ExceptionUtils {

    /**
     * Private Ctor.
     */
    private ExceptionUtils() {
    }

    /**
     * Log the error and set the Application state to ERROR state.
     * 
     * @param c
     *            the class for the logger.
     * @param e
     *            the exception thrown.
     */
    public static void fatalError(final Class<?> c, final Exception e) {
	e.printStackTrace();
	logError(c, e);
	StateMonitor.errorState();
    }

    /**
     * Log the error and set the Application state to ERROR state.
     * 
     * @param c
     *            the class for the logger.
     * @param e
     *            the exception thrown.
     */
    public static void logError(final Class<?> c, final Exception e) {
	Logger logger = LoggerFactory.getLogger(c);
	logger.error(e.getMessage());
    }

}
