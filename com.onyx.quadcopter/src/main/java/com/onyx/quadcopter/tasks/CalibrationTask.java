package com.onyx.quadcopter.tasks;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;
import com.onyx.common.state.OnyxState;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.onyx.quadcopter.main.StateMonitor;

public class CalibrationTask extends Task<AclMessage> {

  /**
   * True when the red button has been pressed.
   */
  private boolean pressed;

  /**
   * Construct a new calibration task.
   */
  public CalibrationTask() {
    super(TaskId.CALIBRATE);
  }

  @Override
  public void perform() {
    getDev().sendMessage(DeviceId.OLED_DEVICE,
        "Initiating calibration sequence."
        + "Attach battery and hold red button for 3s.",
        ActionId.PRINT, AclPriority.MAX);
    setAllSpeed(Constants.MOTOR_MAX_SPEED);
    while (!pressed) {
      // Pause this thread but sleep it to avoid busy waiting.
      try {
        Thread.sleep(Constants.MOTOR_INIT_DELAY);
      } catch (InterruptedException e1) {
        ExceptionUtils.logError(getClass(), e1);
      }
      pressed = (StateMonitor.getState() != OnyxState.CALIBRATION);
    }
    setAllSpeed(Constants.MOTOR_MIN_SPEED);
  }

  /**
   * Set the speed of all 4 motors.
   * 
   * @param speed the speed at which to rotate the motors as a percentage.
   */
  private void setAllSpeed(double speed) {
    getDev().sendMessageHigh(DeviceId.MOTOR1, "", speed, ActionId.CHANGE_MOTOR_SPEED);
    getDev().sendMessageHigh(DeviceId.MOTOR2, "", speed, ActionId.CHANGE_MOTOR_SPEED);
    getDev().sendMessageHigh(DeviceId.MOTOR3, "", speed, ActionId.CHANGE_MOTOR_SPEED);
    getDev().sendMessageHigh(DeviceId.MOTOR4, "", speed, ActionId.CHANGE_MOTOR_SPEED);
  }

  @Override
  protected AclMessage complete() {
    AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.PRINT);
    msg.setContent("Calibration complete!");
    msg.setPriority(AclPriority.HIGH);
    msg.setReciever(DeviceId.OLED_DEVICE);
    msg.setSender(getDev().getId());
    getDev().sendMessage(msg);
    return msg;
  }
}
