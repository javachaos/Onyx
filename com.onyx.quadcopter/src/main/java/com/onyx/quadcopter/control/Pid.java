package com.onyx.quadcopter.control;

import com.onyx.common.utils.Constants;

/**
 * Represents a PID along one axis.
 * 
 * @author fred
 *
 */
public class Pid {

  private double mpGain;
  private double miGain;
  private double mdGain;

  /**
   * Setpoint.
   */
  private double setPoint = 0.0;

  private double lastTime;
  private double miTerm;
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
   * @param mpGain
   *        the p gain for the PID.
   * @param miGain
   *        the i gain for the PID.
   * @param mdGain
   *        the d gain for the PID.
   */
  public Pid(double mpGain, double miGain, double mdGain) {
    this.mpGain = mpGain;
    this.miGain = miGain;
    this.mdGain = mdGain;
    this.setSamplePeriod(defaultSample);
  }

  /**
   * Change the setpoint value for this PID.
   * 
   * @param defaultOrientation the value to set this PID setpoint to.
   */
  public void setPoint(double defaultOrientation) {
    this.setPoint = defaultOrientation;
  }

  /**
   * Compute the output of this PID.
   * 
   * @param input
   *    the input to the PID.
   * @return the output of the PID.
   */
  public synchronized double compute(double input) {
    double output = 0;
    if (!auto) {
      return output;
    }
    long now = System.nanoTime();
    
    //Time difference from last compute.
    double deltaTime = (double) (now - lastTime);

    if (deltaTime >= sampleRate) {
      double error = setPoint - input;
      miTerm += (miGain * error);
      if (miTerm > outMax) {
        miTerm = outMax;
      } else if (miTerm < outMin) {
        miTerm = outMin;
      }
      double mdInput = (input - lastInput);

      output = mpGain * error + miTerm - mdGain * mdInput;
      if (output > outMax) {
        output = outMax;
      } else if (output < outMin) {
        output = outMin;
      }

      lastInput = input;
      lastTime = now;
    } else {
    	try {
			Thread.sleep((long) ((sampleRate - deltaTime) / 1000_000L));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return compute(input);
    }
    return output;
  }

  /**
   * Set Tunings.
   * 
   * @param mpGain p gain to set.
   * @param miGain i gain to set.
   * @param mdGain d gain to set.
   */
  public void setTunings(final double mpGain, final double miGain, final double mdGain) {
    this.mpGain = mpGain;
    this.miGain = miGain;
    this.mdGain = mdGain;
  }

  /**
   * Set the sample period in nanoseconds.
   * 
   * @param controllerPeriod the time frame between samples.
   * 
   *        If the sampleTime is greater than the controller period the sample rate is set to
   *        Default of 1000ns.
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
   * 
   * @param auto the value of autocorrect.
   */
  public void setAuto(boolean auto) {
    this.auto = auto;
  }

  /**
   * Set the maximum output value.
   * 
   * @param maxOutput the max output for this PID.
   */
  public void setMaxOutput(double maxOutput) {
    this.outMax = maxOutput;
  }

  /**
   * Set the minimum output value.
   * 
   * @param minOutput the min output for this PID.
   */
  public void setMinOutput(double minOutput) {
    this.outMin = minOutput;
  }
}
