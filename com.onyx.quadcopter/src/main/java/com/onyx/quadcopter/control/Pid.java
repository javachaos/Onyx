package com.onyx.quadcopter.control;

import com.onyx.quadcopter.utils.Constants;

/**
 * Represents a PID along one axis.
 * 
 * @author fred
 *
 */
public class Pid {

    private double pGain;
    private double iGain;
    private double dGain;

    /**
     * Setpoint.
     */
    private double setPoint = 0.0;
    
    private double lastTime;
    private double iTerm;
    private double sampleRate;
    
    /**
     * True for auto correct enable.
     */
    private boolean auto = true;
    
    /**
     * Maximum PID output.
     */
    private double outMax;
    private double outMin;
    private double lastInput;
    
    /**
     * Default sample period of 100000ns. (100us)
     */
    private int defaultSample = 100000;
    
    /**
     * Create a new PID for one axis.
     * 
     * @param pGain
     * @param iGain
     * @param dGain
     */
    public Pid(double pGain, double iGain, double dGain) {
	this.pGain = pGain;
	this.iGain = iGain;
	this.dGain = dGain;
	this.setSamplePeriod(defaultSample);
    }
    
    /**
     * Change the setpoint value for this PID.
     * @param defaultOrientation
     *     the value to set this PID setpoint to.
     */
    public void setPoint(double defaultOrientation) {
	this.setPoint = defaultOrientation;
    }
    
    /**
     * Compute the output of this PID.
     * @param input
     * @return the output of the PID.
     */
    public double compute(double input) {
	double output = 0;
	if(!auto) {
	    return output;
	}
	long now = System.nanoTime();
	double deltaTime = (double)(now - lastTime);
	
	if (deltaTime >= sampleRate) {
	    double error = setPoint - input;
	    iTerm += (iGain * error);
	    if(iTerm > outMax) {
		iTerm = outMax;
	    } else if (iTerm < outMin) {
		iTerm = outMin;
	    }
	    double dInput = (input - lastInput);
	    
	    output =  pGain * error + iTerm - dGain * dInput;
	    if(output > outMax) {
		output = outMax;
	    } else if(output < outMin) {
		output = outMin;
	    }

	    lastInput = input;
	    lastTime = now;
	}
	return output;
    }

    /**
     * Set Tunings.
     * 
     * @param pGain
     * 	    pGain to set.
     * @param iGain
     * 	    iGain to set.
     * @param dGain
     *      dGain to set.
     */
    public void setTunings(final double pGain, final double iGain, final double dGain) {
	this.pGain = pGain;
	this.iGain = iGain;
	this.dGain = dGain;
    }
    
    /**
     * Set the sample period in nanoseconds.
     * 
     * @param controllerPeriod the time frame between samples.
     * 
     * If the sampleTime is greater than the controller period
     * the sample rate is set to Default of 1000ns.
     */
    public void setSamplePeriod(long controllerPeriod) {
	if (controllerPeriod <= Constants.CONTROLLER_PERIOD * 1000) {
	    this.sampleRate = controllerPeriod;
	} else {
	    this.sampleRate = Constants.CONTROLLER_PERIOD * 1000;
	}
	
    }
    
    /**
     * Set auto correct on.
     * @param auto
     * 		the value of autocorrect.
     */
    public void setAuto(boolean auto) {
	this.auto = auto;
    }
    
    /**
     * Set the maximum output value.
     * @param maxOutput
     * 		the max output for this PID.
     */
    public void setMaxOutput(double maxOutput) {
	this.outMax = maxOutput;
    }
    
    /**
     * Set the minimum output value.
     * @param minOutput
     * 		the min output for this PID.
     */
    public void setMinOutput(double minOutput) {
	this.outMin = minOutput;
    }
}
