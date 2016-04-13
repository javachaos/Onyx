/******************************************************************************
 * Copyright (c) 2016 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.onyx.quadcopter.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Constants.
 *
 * @author fred
 *
 */
public final class Constants {

    /**
     * Private ctor.
     */
    private Constants() {
    }

    /**
     * Init the constants.
     */
    public static void init() {
	new Constants();
    }

    /**
     * UTF8 Charset.
     */
    public static final Charset UTF8 = Charset.forName("UTF8");

    /**
     * Database file name.
     */
    public static final String DATABASE_NAME = "data.db";

    /**
     * SQLite embedded database driver class.
     */
    public static final String DRIVER = "org.sqlite.JDBC";

    /**
     * Encoding.
     */
    public static final String ENCODING = "UTF-8";

    /**
     * The name of this application.
     */
    public static final String APPLICATION_NAME = "onyx";

    /**
     * The name of this application.
     */
    public static final String APPLICATION_NAME_SP = "Onyx Quadcopter";

    /**
     * The name of this application.
     */
    public static final String APPLICATION_NAME_TEST = "OnyxTest";

    /**
     * Application directory location.
     */
    public static final String APPLICATION_DIR = File.separator + "opt" + File.separator + APPLICATION_NAME;

    /**
     * Directory to store images.
     */
    public static final String IMG_DIR = APPLICATION_DIR + File.separator + "images";

    /**
     * Name of the recovery file.
     */
    public static final String RECOVERY_NAME = "backup_" + Constants.DATABASE_NAME;

    /**
     * Database file.
     */
    public static final File DATABASE_FILE = new File(
	    Constants.APPLICATION_DIR + File.separator + Constants.DATABASE_NAME);

    /**
     * Recovery file.
     */
    public static final File RECOVERY_FILE = new File(
	    Constants.APPLICATION_DIR + File.separator + Constants.RECOVERY_NAME);

    /**
     * Application directory file.
     */
    public static final File APP_DIRECTORY = new File(Constants.APPLICATION_DIR + File.separator);

    /**
     * Application Properties file name.
     */
    public static final String PROPERTY_FILE_NAME = Constants.APPLICATION_DIR + File.separator + "config.properties";

    /**
     * Sleep time in milliseconds. Used to delay the state monitor.
     */
    public static final long SLEEP_TIME = 200;

    /**
     * The time unit used for the Monitor.
     */
    public static final TimeUnit MONITOR_TIMEUNIT = TimeUnit.MILLISECONDS;

    /**
     * The termination timeout in seconds.
     */
    public static final long TERMINATION_TIMEOUT = 1;

    /**
     * The latch count used in the ThreadUtils class.
     */
    public static final int LATCH_COUNT = 1;

    /**
     * The property manager instance.
     */
    public static final PropertyManager PROPERTIES = new PropertyManager();

    /**
     * The number of milliseconds to delay before the start of the state
     * monitor.
     */
    public static final long MONITOR_DELAY = PROPERTIES.getLongProperty("com.onyx.quadcopter.monitor.delay",
	    SLEEP_TIME);

    /**
     * The number of milliseconds to delay before the next update call.
     */
    public static final long MONITOR_PERIOD = PROPERTIES.getLongProperty("com.onyx.quadcopter.monitor.period", 250);

    /**
     * The number of update cycles to run before a call to Device.Alternate
     */
    public static final long ALTERNATE_SPEED = PROPERTIES.getLongProperty("com.onyx.quadcopter.alternate.speed", 10000);

    /**
     * The maximum number of devices.
     */
    public static final int MAX_DEVICES = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.max.devices", 256);

    /**
     * Controller update frequency in microseconds.
     */
    public static final long CONTROLLER_PERIOD = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.controller.period",
	    5000);

    /**
     * Time unit for controller update period.
     */
    public static final TimeUnit CONTROLLER_TIMEUNIT = TimeUnit.MICROSECONDS;

    /**
     * Database create statement.
     */
    public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS blackboard ("
	    + "ID INTEGER PRIMARY KEY AUTOINCREMENT, TO_DEVICE_ID INTEGER, FROM_DEVICE_ID INTEGER,"
	    + "ACTION_ID INTEGER, MSG_TYPE INTEGER, CONTENT VARCHAR(32), VALUE REAL);";

    /**
     * Create add message statment.
     */
    public static final String ADD_MESSAGE_STATEMENT = "INSERT INTO blackboard (TO_DEVICE_ID, FROM_DEVICE_ID, "
	    + "ACTION_ID, MSG_TYPE, CONTENT, VALUE) VALUES (?,?,?,?,?,?);";

    /**
     * Create get message statement.
     */
    public static final String GET_MESSAGE_STATEMENT = "SELECT * FROM blackboard WHERE TO_DEVICE_ID='?';";

    /**
     * Transmission QoS value.
     */
    public static final int TRANSMIT_QOS = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.comms.transmit.qos", 2);

    /**
     * Max GPIO pin number.
     */
    public static final int GPIO_MAX = 40;

    /**
     * Min GPIO pin number.
     */
    public static final int GPIO_MIN = 0;

    /**
     * PWM GPIO Pins.
     */
    public static final short GPIO_MOTOR1 = (short) PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.devices.motor1.pin", 0);
    public static final short GPIO_MOTOR2 = (short) PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.devices.motor2.pin", 1);
    public static final short GPIO_MOTOR3 = (short) PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.devices.motor3.pin", 2);
    public static final short GPIO_MOTOR4 = (short) PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.devices.motor4.pin", 3);

    /**
     * True if this is a simulation run.
     */
    public static final boolean SIMULATION = PROPERTIES.getBooleanProperty("com.onyx.quadcopter.simulation", true);

    /**
     * Number of threads to use.
     */
    public static final int NUM_THREADS = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.numthreads",
	    Runtime.getRuntime().availableProcessors());

    /**
     * Maximum Blackboard size.
     */
    public static final int BLACKBOARD_SIZE = 256;

    /**
     * Onyx Natty server port.
     */
    public static final int PORT = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.port", 8888);

    /**
     * Send and recieve buffer size.
     */
    public static final int NETWORK_BUFFER_SIZE = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.nio.buffer.size",
	    1024);

    /**
     * Number of threads to dedicate to network IO.
     */
    public static final int NUM_NIO_THREADS = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.nio.threads", 1);

    /**
     * CommServer initialization delay in Seconds.
     */
    public static final long COMM_SERVER_INIT_DELAY = PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.nio.commserver.startdelay", 5);

    /**
     * Native libraries directory
     */
    public static final String NATIVES_DIR = PROPERTIES.getStringProperty("com.onyx.quadcopter.natives.dir",
	    APPLICATION_DIR + File.separator + "natives");

    /**
     * Java native library name.
     */
    public static final String GYRO_NATIVE_LIB = PROPERTIES.getStringProperty("com.onyx.quadcopter.natives.gyro",
	    NATIVES_DIR + File.separator + "libjavaupm_lsm9ds0.so");

    /**
     * MRAA Java native library.
     */
    public static final String MRAA_NATIVE_LIB = PROPERTIES.getStringProperty("com.onyx.quadcopter.natives.mraa",
	    NATIVES_DIR + File.separator + "libmraajava.so");

    /**
     * The i2c bus ID.
     */
    public static final int I2C_BUS_ID = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.i2c.bus.id", 0);

    /**
     * Orientation change threashold. Any data which changes by a factor of this
     * value after a single update is discarded.
     */
    public static final float ORIENTATION_THRESHOLD = PROPERTIES
	    .getFloatProperty("com.onyx.quadcopter.orient.threshold", 1000.0f);

    /**
     * Initial speed of the motors.
     */
    public static final int MOTOR_INIT_SPEED = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.motor.init.speed", 0);

    /**
     * Amount of time to delay the start of the controller thread. In
     * milliseconds.
     */
    public static final long CONTROLLER_DELAY = PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.controller.start.delay", 1000);

    /**
     * Max number of locally stored messages to keep.
     */
    public static final int OLED_MAX_MSGS = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.oled.max.msgs", 32);

    /**
     * Default UART port ID.
     */
    public static final int UART_PORT = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.uart.port", 0);

    /**
     * NMEA Buffer size.
     */
    public static final int NMEA_BUFF_SIZE = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.nmea.buffer.size", 256);

    /**
     * P Gain for PID controller.
     */
    public static final float PID_GAIN_P = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.pid.gain.p", 0);

    /**
     * I Gain for PID controller.
     */
    public static final float PID_GAIN_I = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.pid.gain.i", 0);

    /**
     * D Gain for PID controller.
     */
    public static final float PID_GAIN_D = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.pid.gain.d", 0);

    /**
     * PWM Frequency.
     */
    public static final float PWM_FREQ = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.pwm.freq", 50);

    /**
     * How long to wait for camera. In seconds.
     */
    public static final long WEBCAM_TIMEOUT = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.webcam.timeout", 10);

    /**
     * OpenCV Java native library.
     */
    public static final String CAM_NATIVE_LIB = PROPERTIES.getStringProperty("com.onyx.quadcopter.natives.webcam",
	    NATIVES_DIR + File.separator + "libopencv_java2412.so");

    /**
     * Display font for OLED Display.
     */
    public static final int DISP_FONT = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.disp.font", 8);

    /**
     * PCA9685 I2C Address.
     */
    public static final short PCA9685_I2C_ADDRESS = (short) PROPERTIES
	    .getIntegerProperty("com.onyx.quadcopter.i2c.addr.pca9685", 0x40);

    /**
     * Maximum percentage speed of the motors.
     */
    public static final int MOTOR_MAX_SPEED = 100;

    /**
     * Initialization delay in milliseconds for the motors.
     */
    public static final long MOTOR_INIT_DELAY = 2000;

    /**
     * X start position for OLED graphics.
     */
    public static final int OLED_X_START = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.i2c.oled.x", 0);

    /**
     * Y start position for OLED graphics.
     */
    public static final int OLED_Y_START = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.i2c.oled.y", 0);

    /**
     * Max size of the blackboard Concurrent stack.
     */
    public static final int MAX_BLACKBOARD_BUCKET_SIZE = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.blackboard.max.bucket.size", 32);

    /**
     * The default task priority.
     */
    public static final int DEFAULT_TASK_PRIORITY = 0;

}
