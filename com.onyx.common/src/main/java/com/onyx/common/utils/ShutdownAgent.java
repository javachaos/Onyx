package com.onyx.common.utils;
/******************************************************************************
 * Copyright (c) 2014 Fred Laderoute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Fred Laderoute - initial API and implementation
 ******************************************************************************/

import java.util.concurrent.ScheduledExecutorService;

/**
 * Thread shutdown agent.
 * 
 * @author fred
 *
 */
public class ShutdownAgent extends Thread {

  /**
   * The coordinator service.
   */
  private ScheduledExecutorService coordinator;

  /**
   * Shutdown agent constructor.
   * 
   * @param ses the scheduled executor service
   */
  public ShutdownAgent(final ScheduledExecutorService ses) {
    this.coordinator = ses;
  }

  @Override
  public final void run() {
    coordinator.shutdown();
    coordinator.shutdownNow();
  }

}
