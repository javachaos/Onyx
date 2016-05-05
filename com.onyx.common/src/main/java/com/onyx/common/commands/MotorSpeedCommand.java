package com.onyx.common.commands;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.AclPriority;
import com.onyx.common.messaging.ActionId;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.messaging.MessageType;

/**
 * 
 * @author fred
 *
 */
public class MotorSpeedCommand extends Command {

  /**
   * Generated SUID.
   */
  private static final long serialVersionUID = -3652608805183898841L;

  /**
   * Desired motor speed.
   */
  private final double speed;
  
  /**
   * Motor ID.
   */
  private final int motor;
  
  /**
   * Increase the speed of motor 1,2,3 or 4.
   * @param motor
   *    the motor id to change the speed of.
   * @param speed
   *    the desired speed of the motor.
   */
  public MotorSpeedCommand(final DeviceId sender, int motor, double speed) {
    super(CommandType.MOTOR_SPD, sender);
    this.speed = speed;
    this.motor = motor;
  }
  
  /**
   * Increase the speed of motor 1,2,3 or 4.
   * @param motor
   *    the motor id to change the speed of.
   * @param speed
   *    the desired speed of the motor.
   */
  public MotorSpeedCommand(int motor, double speed) {
    super(CommandType.MOTOR_SPD, DeviceId.COMM_CLIENT);
    this.speed = speed;
    this.motor = motor;
  }

  @Override
  public AclMessage getAclMessage() {
    final AclMessage msg = new AclMessage(MessageType.SEND);
    msg.setActionId(ActionId.CHANGE_MOTOR_SPEED);
    msg.setValue(speed);
    msg.setPriority(AclPriority.HIGH);
    msg.setSender(getSender());
    switch (motor) {
      case 1:
        msg.setReciever(DeviceId.MOTOR1);
        break;
      case 2:
        msg.setReciever(DeviceId.MOTOR2);
        break;
      case 3:
        msg.setReciever(DeviceId.MOTOR3);
        break;
      case 4:
        msg.setReciever(DeviceId.MOTOR4);
        break;
        default:
          break;
    }
    return msg;  
  }

}
