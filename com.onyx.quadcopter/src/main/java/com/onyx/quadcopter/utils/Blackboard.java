package com.onyx.quadcopter.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.main.Controller;

/**
 * Blackboard message class.
 *
 * All devices share this class to communicate over.
 *
 * (Similar to a classroom blackboard. Each device takes a turn with the chalk,
 * writes a message and passes the chalk to the next device in sequence.)
 *
 * @author fred
 *
 */
public class Blackboard extends Device {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(Blackboard.class);

    /**
     * Database connection instance.
     */
    private static volatile Connection memconn;

    /**
     * True when the database is loaded.
     */
    private static volatile boolean isLoaded;

    public Blackboard(final Controller c) {
        super(c, DeviceID.BLACKBOARD);
        getConnection();
        createTables();
        if (databaseExists()) {
            restore();
        } else {
            backup();
        }
    }

    /**
     * Loads driver.
     */
    private static synchronized void loadDriver() {

        try {
            Class.forName(Constants.DRIVER);
            isLoaded = true;
        } catch (final ClassNotFoundException cnfe) {
            ExceptionUtils.fatalError(Blackboard.class, cnfe);
        }

    }

    public static synchronized final Connection getConnection() {

        if (!isLoaded) {
            loadDriver();
        }

        try {
            if ((memconn == null) || memconn.isClosed()) {
                memconn = DriverManager.getConnection("jdbc:sqlite:");
            }
        } catch (final SQLException ex) {
            ExceptionUtils.fatalError(Blackboard.class, ex);
        }

        return memconn;
    }

    /**
     * Create database tables.
     */
    private static synchronized void createTables() {
        getConnection();
        try {
            final Statement stat = memconn.createStatement();
            stat.addBatch(Constants.DATABASE_CREATE);
            stat.executeBatch();
        } catch (final SQLException e) {
            ExceptionUtils.fatalError(Blackboard.class, e);
        }
    }

    /**
     * Backup the in memory database to file.
     */
    public static synchronized void backup() {
        LOGGER.debug("Backing up database.");
        getConnection();
        try {
            final Statement stat = memconn.createStatement();
            stat.executeUpdate("backup to " + Constants.DATABASE_FILE.getAbsolutePath());
        } catch (final SQLException e) {
            ExceptionUtils.fatalError(Blackboard.class, e);
        }
    }

    /**
     * Restore the in memory database to memory.
     */
    public static synchronized void restore() {
        LOGGER.debug("Restoring database.");
        getConnection();
        try {
            final Statement stat = memconn.createStatement();
            stat.executeUpdate("restore from " + Constants.DATABASE_FILE.getAbsolutePath());
        } catch (final SQLException e) {
            ExceptionUtils.fatalError(Blackboard.class, e);
        }
    }

    /**
     * Shutdown the database file.
     */
    @Override
    public synchronized void shutdown() {
        backup();
        LOGGER.debug("Blackboard shutting down.");
        try {
            if (memconn != null) {
                memconn.close();
                while (!memconn.isClosed()) {
                    try {
                        Thread.sleep(Constants.SLEEP_TIME);
                    } catch (final InterruptedException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        } catch (final SQLException e) {
            ExceptionUtils.fatalError(Blackboard.class, e);
        }
    }

    /**
     * True if the database driver has been loaded.
     *
     * @return
     */
    public static final boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Return true if the database exists.
     *
     * @return true if the database exists.
     */
    public static boolean databaseExists() {
        LOGGER.info("Checking if database exists.");
        return Constants.DATABASE_FILE.exists();
    }

    @Override
    protected void update(final Blackboard b) {
    }

    @Override
    protected void init() {
        loadDriver();
    }

    @Override
    protected void alternate() {
        backup();
    }

    @Override
    public boolean selfTest() {
        LOGGER.debug("Running blackboard self test.");
        // TODO implement
        loadDriver();
        return isLoaded;
    }

}
