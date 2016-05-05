package com.onyx.quadcopter.main;
/******************************************************************************
 * Copyright (c) 2016 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

import com.onyx.common.state.AbstractStateMonitor;
import com.onyx.common.state.OnyxState;
import com.onyx.quadcopter.tasks.CalibrationTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State monitor to monitor the application state.
 *
 * @author fred
 *
 */
public final class StateMonitor extends AbstractStateMonitor implements Runnable {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(StateMonitor.class);

  /**
   * Controller instance.
   */
  private Controller controller;

  /**
   * State monitor constructor.
   */
  public StateMonitor(final Controller controller) {
    super(Main.COORDINATOR);
    if (controller != null) {
      this.controller = controller;
    }
  }

  /**
   * Initialize the state monitor.
   */
  @Override
  protected void init() {
    isRunning.set(true);
    state = OnyxState.STARTUP;
    previousState = OnyxState.SHUTDOWN;
  }

  /**
   * Called every application update cycle. to ensure the application state is correct.
   */
  @Override
  public void update() {
    checkState();
    switch (state) {
      case AIRBORNE:
        doAirborne();
        break;
      case LANDED:
        doLanded();
        break;
      case LANDING:
        doLanding();
        break;
      case ERROR:
        doError();
        break;
      case RECOVERY:
        doRecover();
        break;
      case SHUTDOWN:
        doShutdown();
        break;
      case STARTUP:
        doStartup();
        break;
      case CALIBRATION:
        doCalibration();
        break;
      default:
        break;
    }

    if (isStateChanged()) {
      previousState = state;
    }
  }

  /**
   * Run the state monitor.
   */
  @Override
  public void run() {
    if (isRunning.get()) {
      update();
    }
  }

  /**
   * Return the state of the application.
   *
   * @return the state of the application.
   */
  public static OnyxState getState() {
    return state;
  }

  public static void calibrationState() {
    state = OnyxState.CALIBRATION;
  }

  /**
   * The aircraft in landed.
   */
  public static void landedState() {
    state = OnyxState.LANDED;
  }

  /**
   * The aircraft is landing.
   */
  public static void landingState() {
    state = OnyxState.LANDING;
  }

  /**
   * The aircraft is in flight.
   */
  public static void airborneState() {
    state = OnyxState.AIRBORNE;
  }

  /**
   * Startup state.
   */
  public static void recoveryState() {
    state = OnyxState.RECOVERY;
  }

  private void doCalibration() {
    switch (previousState) {
      case AIRBORNE:
        state = previousState;
        break;
      case ERROR:
        state = previousState;
        break;
      case LANDED:
        controller.executeTask(new CalibrationTask());
        break;
      case LANDING:
        state = previousState;
        break;
      case RECOVERY:
        state = previousState;
        break;
      case SHUTDOWN:
        state = previousState;
        break;
      case STARTUP:
        state = previousState;
        break;
      default:
        break;
    }
  }

  /**
   * Error state.
   */
  private void doError() {
    status = -1;
    state = OnyxState.RECOVERY;
  }

  /**
   * Shutdown the application.
   */
  private void doShutdown() {
    switch (previousState) {
      case AIRBORNE:
        state = previousState;
        break;
      case ERROR:
        state = previousState;
        break;
      case LANDED:
        controller.stop();
        exit();
        break;
      case LANDING:
        state = previousState;
        break;
      case RECOVERY:
        state = previousState;
        break;
      case SHUTDOWN:
        state = previousState;
        break;
      case STARTUP:
        state = previousState;
        break;
      default:
        break;
    }
  }

  /**
   * Attempt to recover from corruption error.
   */
  private void doRecover() {
    //TODO Complete
    if (isStateChanged()) {
      switch (previousState) {
        case AIRBORNE:
          LOGGER.debug("Attempt Recovery.");
          break;
        case ERROR:
          break;
        case LANDED:
          break;
        case LANDING:
          break;
        case RECOVERY:
          break;
        case SHUTDOWN:
          break;
        case STARTUP:
          break;
        default:
          break;

      }
    }
  }

  /**
   * Do Startup state.
   */
  private void doStartup() {
    if (isStateChanged()) {
      Main.appStart(controller);
    }
  }

  private void doLanding() {
    if (isStateChanged()) {
      //TODO Complete
    }
  }

  private void doLanded() {
    if (isStateChanged()) {
      //TODO Complete
    }
  }

  private void doAirborne() {
    if (isStateChanged()) {
      // TODO Complete
    }
  }
}
