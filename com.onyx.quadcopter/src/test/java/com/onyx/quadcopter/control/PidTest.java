package com.onyx.quadcopter.control;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PidTest {

	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(PidTest.class);
	
	@Test
	public void test() {
		Pid pid = new Pid(2,1,0.5);
		pid.setMaxOutput(1.0);
		pid.setMinOutput(0.0);
		pid.setPoint(0.5);
		
		int x = 0;
		while(x < 100) {
		  x++;
		  double input = Math.random();
		  double output = pid.compute(input);
		  assertTrue(output < 1.0 && output > 0.0);
		  LOGGER.debug("[PID] IN: " + input + " OUT: " + output);
		}
	}

}
