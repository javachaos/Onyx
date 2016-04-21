package com.onyx.quadcopter.main;
/******************************************************************************
 * Copyright (c) 2016 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

/**
 * Defines the state of the application.
 *
 * @author fred
 *
 */
public enum OnyxState {

  /**
   * Defines an Exiting state.
   */
  SHUTDOWN,

  /**
   * Defines an Error state.
   */
  ERROR,

  /**
   * The start up state.
   */
  STARTUP,

  /**
   * Recovery state.
   */
  RECOVERY,

  /**
   * Aircraft is landing.
   */
  LANDING,

  /**
   * Aircraft is landed.
   */
  LANDED,

  /**
   * Aircraft is off the ground.
   */
  AIRBORNE,

  /**
   * Calibrate the ESCs.
   */
  CALIBRATION;
}
