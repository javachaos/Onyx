package com.onyx.common.state;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ShutdownAgent;
import com.onyx.common.utils.ThreadUtils;

/**
 * <p>This class represents an abstract state monitor.
 * This class is the base class for all state monitor implementations
 * within the Onyx system.</p>
 * 
 * <p>Application State is managed by a set enum types of {@link OnyxState Onyx states}.</p>
 * 
 * <p>Upon switching states the internal static state of this class is changed,
 * the previous state is held for convenience and the boolean type
 * isStateChanged is set to true which may be accessed by call to {@link #isStateChanged()}</p>
 * 
 * <p>This is intended to be ThreadSafe.</p>
 * 
 * @author fred
 *
 */
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
  private AtomicBoolean stateChanged = new AtomicBoolean(false);

  /**
   * True if the monitor is running.
   */
  protected AtomicBoolean isRunning = new AtomicBoolean(false);
  
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
    if (isRunning.get()) {
      update();
    }
  }
  
  /**
   * Return true if the state monitor is running.
   *
   * @return true if the monitor is running.
   */
  public boolean isRunning() {
    return isRunning.get();
  }
  
  /**
   * True if the state has changed since the last update.
   *
   * @return true if the state has changed.
   */
  public boolean isStateChanged() {
    return stateChanged.get();
  }

  /**
   * Set the stateChanged variable.
   *
   * @param changed the state to be set.
   */
  protected void setStateChanged(final boolean changed) {
    stateChanged.set(changed);
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
  public synchronized void exit() {
    isRunning.set(false);
    ThreadUtils.shutdown();
    COORDINATOR.schedule(new ShutdownAgent(COORDINATOR), Constants.SLEEP_TIME,
        Constants.MONITOR_TIMEUNIT);
    System.exit(status);
  }

  public synchronized static void errorState() {
    state = OnyxState.ERROR;
  }
  
  public synchronized static void startupState() {
    state = OnyxState.STARTUP;
  }
  
  public synchronized static void shutdownState() {
    state = OnyxState.SHUTDOWN;
  }
}
