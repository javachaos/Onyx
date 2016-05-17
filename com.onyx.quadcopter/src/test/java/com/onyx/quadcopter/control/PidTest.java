package com.onyx.quadcopter.control;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.EvictingQueue;

public class PidTest {

	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(PidTest.class);

	/**
	 * The PID input size to test.
	 */
	private static final int PID_INPUT_SIZE = 10000;

	private static final double SETPOINT = 0.5;
	
	/**
	 * The last 100 values from the pid to compute the moving average.
	 */
	private EvictingQueue<Double> lastHundred = EvictingQueue.create(100);
	
	@Test(timeout=15000)
	public void test() {
		Pid pid = new Pid(2,1,0.5);
		pid.setMaxOutput(1.0);
		pid.setMinOutput(0.0);
		pid.setPoint(SETPOINT);
		
		int x = 0;
		while(x < PID_INPUT_SIZE) {
		  x++;
		  double input = Math.random();
		  double output = pid.compute(input);
		  lastHundred.add(output);
		  assertTrue(output <= 1.0 && output >= 0.0);
		  double ma = lastHundred.parallelStream().mapToDouble(n -> n).average().getAsDouble();
		  if (x > PID_INPUT_SIZE / 2) {
			LOGGER.debug("[PID] IN: " + input + " OUT: " + output + " MA: " + ma + " Set Size: " + lastHundred.size());
		    assertTrue(Math.abs(ma - SETPOINT) < SETPOINT);
		  }
		}
	}

}
