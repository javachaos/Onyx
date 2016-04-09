package com.onyx.quadcopter.control;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

/**
 * A simple PID Controller.
 * 
 * @author fred
 *
 */
public class PIDController extends Device {

    private static final float GAIN_P = Constants.PID_GAIN_P;
    private static final float GAIN_I = Constants.PID_GAIN_I;
    private static final float GAIN_D = Constants.PID_GAIN_D;

    private static float prevError = 0;

    private static float error = 0;
    private static float errorSum = 0;

    private float xSetPoint = 0;
    private float ySetPoint = 0;
    private float zSetPoint = 0;

    private float xOut = 0;
    private float yOut = 0;
    private float zOut = 0;

    private float xIn = 0;
    private float yIn = 0;
    private float zIn = 0;

    /**
     * Default orientation. (Level with respect to ground)
     */
    private float[] defaultOrient = new float[] { 0.0f, 0.0f, 0.0f };

    public PIDController(final Controller c) {
	super(c, DeviceID.PID);
    }

    @Override
    protected void update() {

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
}
