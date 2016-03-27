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
    public static final String APPLICATION_NAME = "Onyx";

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
    public static final String APPLICATION_DIR = System.getProperty("user.home") + File.separator + APPLICATION_NAME;

    /**
     * Name of the recovery file.
     */
    public static final String RECOVERY_NAME = "backup_data.db";

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
     * Archive name.
     */
    public static final String ARCHIVE_NAME = Constants.APPLICATION_NAME + ".zip";

    /**
     * Archive directory.
     */
    public static final String ARCHIVE_DIR = System.getProperty("user.home");

    /**
     * Archive path.
     */
    public static final String ARCHIVE_PATH = ARCHIVE_DIR + File.separator + ARCHIVE_NAME;

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
     * Database create statement.
     */
    public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS blackboard ("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT, TO_DEVICE_ID INTEGER, FROM_DEVICE_ID INTEGER, CONTENT VARCHAR(32), TOPIC VARCHAR(32), VALUE REAL)";

    /**
     * Controller update frequency in microseconds.
     */
    public static final long CONTROLLER_PERIOD = PROPERTIES.getIntegerProperty("com.onyx.quadcopter.controller.period",
            1500);

    /**
     * MQTT Message broker URL.
     */
    public static final String BROKER_URL = "tcp://onyx.cnc.io:1883";

    /**
     * MQTT Domain.
     */
    public static final String MQTT_DOMAIN = "quadcopter";

    /**
     * MQTT Thing.
     */
    public static final String MQTT_THING = "onyx";

    /**
     * MQTT Username.
     */
    public static final String MQTT_USERNAME = "fred";

    /**
     * MQTT Password in MD5
     */
    public static final String MQTT_PASSWORD_MD5 = "";

    /**
     * MQTT Keep alive value
     */
    public static final int MQTT_KEEPALIVE = 30;
}
