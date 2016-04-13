package com.onyx.quadcopter.devices;

public enum DeviceID {
    DATA_TRANSMITTER(0), BLACKBOARD(1), DATA_RECIEVER(2), MOTOR1(3), MOTOR2(4), MOTOR3(5), MOTOR4(6), COMM_SERVER(
	    7), GYRO_MAG_ACC(
		    8), DCM(9), COMM_CLIENT(10), OLED_DEVICE(11), GPS_DEVICE(12), CAMERA(13), PID(14), RED_BUTTON(15), TASK_DEVICE(16), 
    CONTROLLER(17);

    /**
     * The internal ID field.
     */
    private int id;

    /**
     * Private ActionID constructor.
     *
     * @param i
     *            the id of the enum type.
     */
    private DeviceID(final int i) {
	id = i;
    }

    /**
     * Return the id of the given ActionID.
     *
     * @param t
     *            the ActionID to get the id of
     *
     * @return the ordinal value of the ActionID t
     */
    public static int getId(final DeviceID t) {
	return t.id;
    }
}
