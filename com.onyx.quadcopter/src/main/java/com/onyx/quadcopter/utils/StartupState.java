package com.onyx.quadcopter.utils;
/******************************************************************************
 * Copyright (c) 2014 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

/**
 * The state of the application.
 * 
 * @author fred
 *
 */
public enum StartupState {

  /**
   * Startup was unsuccessful and requires recovery.
   */
  UNSUCCESSFUL,

  /**
   * Startup was successful.
   */
  SUCCESSFUL,

  /**
   * User exited application before startup could complete.
   */
  EXIT;

}
