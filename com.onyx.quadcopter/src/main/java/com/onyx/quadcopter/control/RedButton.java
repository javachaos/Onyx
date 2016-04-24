package com.onyx.quadcopter.control;

import java.util.UUID;

import com.onyx.common.commands.ShutdownCommand;
import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.state.OnyxState;
import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.main.StateMonitor;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Red Button, housed on the outside of the aircraft for performing Very simple commands.
 * 
 * @author fred
 *
 */
public class RedButton extends Device implements GpioPinListenerDigital {

  /**
   * Push Button.
   */
  private GpioPinDigitalInput button;

  /**
   * How long the button has been held down for in nanoseconds.
   */
  private long holdDownTime = 0;
  private long startTime = 0;

  /**
   * Number of nanoseconds per second.
   */
  private static final long NANOSECONDS_PER_SEC = (long) 1e+9;

  // These _SEQ variables hold the information about which sequence to trigger
  // given the length of time the operator holds down this button.
  //
  // Ex. If this button is held down for between 2seconds and less than
  // the start of the shutdown_seq time which is 10seconds.
  // The calibration sequence is entered.

  /**
   * Display Time. [0s-2s].
   */
  private static final long DISPLAY_SEQ = 0;

  /**
   * Calibration Time. [2s-9s]
   */
  private static final long CALIBRATE_SEQ = 2 * NANOSECONDS_PER_SEC;

  /**
   * Shutdown Time. [5s-infs]
   */
  private static final long SHUTDOWN_SEQ = 5 * NANOSECONDS_PER_SEC;

  /**
   * Creates a red button.
   */
  public RedButton() {
    super(DeviceId.RED_BUTTON);
  }

  @Override
  protected void init() {
    button = getController().getGpio().provisionDigitalInputPin(RaspiPin.GPIO_21,
        PinPullResistance.PULL_UP);
    button.addListener(this);
  }

  @Override
  public void shutdown() {
    button.removeAllTriggers();
    button.removeAllListeners();
    button.unexport();
    button = null;
  }

  @Override
  protected void alternate() {}

  @Override
  public boolean selfTest() {
    return getController().getGpio().isState(PinState.HIGH, button);
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent state) {
    LOGGER.debug("Button State: " + state.getState().getName());
    if (state.getState() == PinState.LOW) {
      startTime = System.nanoTime();
      LOGGER.debug("Button Pressed.");
    }
    if (state.getState() == PinState.HIGH) {
      holdDownTime = System.nanoTime() - startTime;
      LOGGER.debug("Button Released. Held down for " + holdDownTime + "  nanoseconds.");
      handleActionSequence(holdDownTime);
    }
  }

  /**
   * Handle Action Sequence.
   * 
   * @param hdt Hold down time, how long this button has been held down for.
   */
  private void handleActionSequence(long hdt) {
    if (hdt >= DISPLAY_SEQ && hdt < CALIBRATE_SEQ) {
      sendMessage(DeviceId.OLED_DEVICE, "NULL",
          ActionId.CHANGE_DISPLAY, AclPriority.MAX);
    } else if (hdt > CALIBRATE_SEQ && hdt < SHUTDOWN_SEQ) {
      LOGGER.debug("Calibration sequence timing entered.");
      if (StateMonitor.getState() == OnyxState.CALIBRATION) {
        StateMonitor.landedState();
      } else {
        StateMonitor.calibrationState();
      }
    } else if (hdt >= SHUTDOWN_SEQ) {
      LOGGER.debug("Initiating Shutdown Sequence...");
      sendMessage(new ShutdownCommand().getAclMessage());
    }
  }

  @Override
  public void update(AclMessage msg) {
    setDisplay("Current Button State: " + button.getState());
  }
}
