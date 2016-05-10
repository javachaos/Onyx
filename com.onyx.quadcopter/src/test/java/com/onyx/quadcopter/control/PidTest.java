package com.onyx.quadcopter.control;

import org.junit.Test;

public class PidTest {

	@Test
	public void test() {
		Pid pid = new Pid(2,1,0.5);
		pid.setMaxOutput(1.0);
		pid.setMinOutput(0.0);
		pid.setPoint(0.5);
		while(true) {
		  System.out.println(pid.compute(Math.random()));
		}
	}

}
