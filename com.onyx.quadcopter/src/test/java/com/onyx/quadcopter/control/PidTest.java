package com.onyx.quadcopter.control;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PidTest {

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
		  assertTrue(output < 1.0);
		  assertTrue(output > 0.0);
		  System.out.println("[PID] IN: " + input + " OUT: " + output);
		}
	}

}
