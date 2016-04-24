package com.onyx.common.state;

import java.util.concurrent.ScheduledExecutorService;

import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ShutdownAgent;
import com.onyx.common.utils.ThreadUtils;

public abstract class AbstractStateMonitor implements Runnable {
  
  /**
   * The exit status.
   */
  protected int status = 0;
  
  /**
   * The app state manager.
   */
  protected static OnyxState state;

  /**
   * The previous application state.
   */
  protected OnyxState previousState = null;
  
  /**
   * True if the application state has changed since the last update.
   */
  private boolean stateChanged;

  /**
   * True if the monitor is running.
   */
  protected volatile boolean isRunning = false;
  
  private ScheduledExecutorService COORDINATOR;
  
  /**
   * State monitor constructor.
   */
  public AbstractStateMonitor(final ScheduledExecutorService ses) {
    COORDINATOR = ses;
    init();
  }

  protected abstract void init();
  
  protected abstract void update();
  
  /**
   * Run the state monitor.
   */
  @Override
  public void run() {
    if (isRunning) {
      update();
    }
  }
  
  /**
   * Return true if the state monitor is running.
   *
   * @return true if the monitor is running.
   */
  public boolean isRunning() {
    return isRunning;
  }
  
  /**
   * True if the state has changed since the last update.
   *
   * @return true if the state has changed.
   */
  public boolean isStateChanged() {
    return stateChanged;
  }

  /**
   * Set the stateChanged variable.
   *
   * @param changed the state to be set.
   */
  protected void setStateChanged(final boolean changed) {
    stateChanged = changed;
  }
  
  /**
   * Return the state of the application.
   *
   * @return the state of the application.
   */
  public static OnyxState getState() {
    return state;
  }
  
  /**
   * Check if the state has changed.
   */
  protected void checkState() {
    if (state != previousState) {
      setStateChanged(true);
    } else if (state == previousState) {
      setStateChanged(false);
    }
  }

  /**
   * Exit the application normally.
   */
  public void exit() {
    isRunning = false;
    ThreadUtils.shutdown();
    COORDINATOR.schedule(new ShutdownAgent(COORDINATOR), Constants.SLEEP_TIME,
        Constants.MONITOR_TIMEUNIT);
    System.exit(status);
  }

  public static void errorState() {
    state = OnyxState.ERROR;
  }
  
  public static void startupState() {
    state = OnyxState.STARTUP;
  }
  
  public static void shutdownState() {
    state = OnyxState.SHUTDOWN;
  }
}
