package com.onyx.quadcopter.main;
/******************************************************************************
 * Copyright (c) 2014 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

import com.onyx.quadcopter.tasks.PowerOnSelfTest;
import com.onyx.quadcopter.utils.StartupState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application start.
 *
 * @author fred
 *
 */
public final class AppStart extends Thread {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(AppStart.class);

  /**
   * Controller object.
   */
  private Controller controller;

  /**
   * Application Start class.
   * @param controller
   *    the controller.
   */
  public AppStart(final Controller controller) {
    if (controller != null) {
      this.controller = controller;
    }
  }

  /**
   * Get Controller instance.
   *
   * @return an instance of the gui.
   */
  public Controller getController() {
    return controller;
  }

  /**
   * Initialize the application.
   *
   * @return sucess or fail.
   */
  public StartupState init() {
    LOGGER.info("Begin Init.");
    return powerOnSelfTest();
  }

  /**
   * Perform pre flight checks.
   *
   * @return machine state, SUCCESSFUL or UNSUCCESSFUL
   */
  private StartupState powerOnSelfTest() {
    final PowerOnSelfTest startTest = new PowerOnSelfTest(controller);
    return startTest.test();
  }

  @Override
  public void run() {
    final StartupState state = init();
    if (state == null) {
      return;
    }
    checkExitState(state);
    switchState(state);
  }

  /**
   * Check if the state is the exit state.
   *
   * @param state the startup state.
   */
  private void checkExitState(final StartupState state) {
    if (state == StartupState.EXIT) {
      StateMonitor.shutdownState();
    }
  }

  /**
   * Switch on state.
   *
   * @param state the startup state.
   */
  private void switchState(final StartupState state) {
    switch (state) {
      case SUCCESSFUL:
        successState();
        break;
      case UNSUCCESSFUL:
        StateMonitor.errorState();
        break;
      default:
        break;
    }
  }

  /**
   * Success state.
   */
  private void successState() {
    controller.start();
    LOGGER.info("Controller start successful.");
    StateMonitor.landedState();
  }
}
