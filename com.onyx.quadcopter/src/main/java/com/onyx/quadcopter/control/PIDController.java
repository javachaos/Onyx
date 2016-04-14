package com.onyx.quadcopter.control;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Constants;

/**
 * A simple PID Controller.
 * 
 * @author fred
 *
 */
public class PIDController extends Device {

    private static final double GAIN_P_X = Constants.PID_GAIN_P_X;
    private static final double GAIN_I_X = Constants.PID_GAIN_I_X;
    private static final double GAIN_D_X = Constants.PID_GAIN_D_X;
    
    private static final double GAIN_P_Y = Constants.PID_GAIN_P_Y;
    private static final double GAIN_I_Y = Constants.PID_GAIN_I_Y;
    private static final double GAIN_D_Y = Constants.PID_GAIN_D_Y;

    private static final double GAIN_P_Z = Constants.PID_GAIN_P_Z;
    private static final double GAIN_I_Z = Constants.PID_GAIN_I_Z;
    private static final double GAIN_D_Z = Constants.PID_GAIN_D_Z;
    
    private Pid xPid, yPid, zPid;

    /**
     * The input orientation provided by gyro;
     */
    private double[] orientation = new double[3];
    
    /**
     * Computed orientation.
     */
    private double[] computedOrientation = new double[3];
    
    /**
     * The controller throttle.
     */
    private double throttle;

    public PIDController() {
	super(DeviceID.PID);
	xPid = new Pid(GAIN_P_X, GAIN_I_X, GAIN_D_X);
	yPid = new Pid(GAIN_P_Y, GAIN_I_Y, GAIN_D_Y);
	zPid = new Pid(GAIN_P_Z, GAIN_I_Z, GAIN_D_Z);
	xPid.setPoint(0);
	yPid.setPoint(0);
	zPid.setPoint(0);
    }

    @Override
    protected void init() {
	
    }

    @Override
    public void shutdown() {

    }

    @Override
    protected void alternate() {

    }

    @Override
    public boolean selfTest() {
	return false;
    }

    @Override
    protected void update() {
	super.update();

	computedOrientation[0] = xPid.compute(orientation[0]);
	computedOrientation[1] = yPid.compute(orientation[1]);
	computedOrientation[2] = zPid.compute(orientation[2]);
	throttle = limit(Constants.MAX_THROTTLE, 0, throttle);
	double esc1 = throttle -  computedOrientation[0] + computedOrientation[1] - computedOrientation[2];
	double esc2 = throttle +  computedOrientation[0] + computedOrientation[1] + computedOrientation[2];
	double esc3 = throttle +  computedOrientation[0] - computedOrientation[1] - computedOrientation[2];
	double esc4 = throttle -  computedOrientation[0] - computedOrientation[1] + computedOrientation[2];
	
	esc1 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc1);
	esc2 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc2);
	esc3 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc3);
	esc4 = limit(Constants.MOTOR_MAX_MS, Constants.DEFAULT_ROTOR_SPEED, esc4);
	
	//Display the Computed Orientation.
	setDisplay("ESC1: " + esc1 + System.lineSeparator() + 
		   "ESC2: " + esc2 + System.lineSeparator() + 
		   "ESC3: " + esc3 + System.lineSeparator() + 
		   "ESC4: " + esc4);
    }
    
    /**
     * Limit the input value between min and max, if
     * the value is less than the min return min, if
     * the value is greater than max return max.
     * 
     * @param max
     *     the max value to clamp to.
     * @param min
     * 	   the min value to clamp to.
     * @param value
     *     the value.
     * @return
     * 	   the value clamped between min and max.
     */
    private double limit(double max, double min, double value) {
	if(value >= max) {
	    return max;
	}
	if(value <= min) {
	    return min;
	}
	return value;
    }
    
    @Override
    public void update(ACLMessage msg) {
	switch (msg.getActionID()) {
	case ORIENT:
	    String[] d0 = msg.getContent().split(":");
	    orientation[0] = Double.parseDouble(d0[0]);
	    orientation[1] = Double.parseDouble(d0[1]);
	    orientation[2] = Double.parseDouble(d0[2]);
	case CHANGE_ORIENT:
	    String[] d1 = msg.getContent().split(":");
	    xPid.setPoint(Double.parseDouble(d1[0]));
	    yPid.setPoint(Double.parseDouble(d1[1]));
	    zPid.setPoint(Double.parseDouble(d1[2]));
	    throttle = Double.parseDouble(d1[3]);
	default:
	    break;
	}
    }
}
