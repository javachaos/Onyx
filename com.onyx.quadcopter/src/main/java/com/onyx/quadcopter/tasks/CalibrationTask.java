package com.onyx.quadcopter.tasks;

import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.main.OnyxState;
import com.onyx.quadcopter.main.StateMonitor;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ACLPriority;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;
import com.onyx.quadcopter.utils.ExceptionUtils;

public class CalibrationTask extends Task<ACLMessage> {

    /**
     * True when the red button has been pressed.
     */
    private boolean pressed;

    /**
     * Construct a new calibration task.
     */
    public CalibrationTask() {
	super(TaskID.CALIBRATE);
    }

    @Override
    public void perform() {
	    getDev().sendMessage(DeviceID.OLED_DEVICE,
		    "Initiating calibration sequence. Attach battery and hold red button for 3 seconds when ready.",
		    ActionId.PRINT, ACLPriority.MAX);
	    setAllSpeed(100.0);
	    while(!pressed) {
		//Pause this thread but sleep it to avoid busy waiting.
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		    ExceptionUtils.logError(getClass(), e);
		}
		pressed = (StateMonitor.getState() != OnyxState.CALIBRATION);
	    }
	    setAllSpeed(0.0);
    }

    /**
     * Set the speed of all 4 motors.
     * @param speed
     * 		the speed at which to rotate the motors as a percentage.
     */
    private void setAllSpeed(double speed) {
	getDev().sendMessageHigh(DeviceID.MOTOR1, "", speed, ActionId.CHANGE_MOTOR_SPEED);
	getDev().sendMessageHigh(DeviceID.MOTOR2, "", speed, ActionId.CHANGE_MOTOR_SPEED);
	getDev().sendMessageHigh(DeviceID.MOTOR3, "", speed, ActionId.CHANGE_MOTOR_SPEED);
	getDev().sendMessageHigh(DeviceID.MOTOR4, "", speed, ActionId.CHANGE_MOTOR_SPEED);
    }

    @Override
    protected ACLMessage complete() {
	ACLMessage m = new ACLMessage(MessageType.SEND);
	m.setActionID(ActionId.PRINT);
	m.setContent("Calibration complete!");
	m.setPriority(ACLPriority.HIGH);
	m.setReciever(DeviceID.OLED_DEVICE);
	m.setSender(getDev().getId());
	getDev().sendMessage(m);
	return m;
    }
}