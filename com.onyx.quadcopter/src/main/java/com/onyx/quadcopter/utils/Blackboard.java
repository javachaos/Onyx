package com.onyx.quadcopter.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.devices.Device;
import com.onyx.quadcopter.devices.DeviceID;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.messaging.ActionId;
import com.onyx.quadcopter.messaging.MessageType;

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
     * Prepared statements.
     */
    private static PreparedStatement addMessageStmt;
    private static PreparedStatement restoreBlackboardStmt;
    private static PreparedStatement backupBlackboardStmt;
    private static PreparedStatement createTablesStmt;
    private static PreparedStatement getMessageStmt;
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
            final PreparedStatement p = createTablesStmt;
            p.execute();
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
            final PreparedStatement p = backupBlackboardStmt;
            p.execute();
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
            final PreparedStatement p = restoreBlackboardStmt;
            p.execute();
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
    protected void update() {
    }

    @Override
    protected void init() {
        loadDriver();
        try {// Setup prepared statments.
            addMessageStmt = getConnection().prepareStatement(Constants.ADD_MESSAGE_STATEMENT);
            restoreBlackboardStmt = getConnection()
                    .prepareStatement("restore from " + Constants.DATABASE_FILE.getAbsolutePath());
            backupBlackboardStmt = getConnection()
                    .prepareStatement("backup to " + Constants.DATABASE_FILE.getAbsolutePath());
            getMessageStmt = getConnection().prepareStatement(Constants.GET_MESSAGE_STATEMENT);
            createTablesStmt = getConnection().prepareStatement(Constants.DATABASE_CREATE);

        } catch (final SQLException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
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

    /**
     * Add a message to the blackboard.
     *
     * @param aclMessage
     */
    public static void addMessage(final ACLMessage aclMessage) {
        getConnection();
        try {
            final PreparedStatement p = addMessageStmt;
            p.setInt(1, DeviceID.getId(aclMessage.getReciever()));
            p.setInt(2, DeviceID.getId(aclMessage.getSender()));
            p.setInt(3, ActionId.getId(aclMessage.getActionID()));
            p.setInt(4, MessageType.getId(aclMessage.getMessageType()));
            p.setString(5, aclMessage.getContent());
            p.setDouble(6, aclMessage.getValue());
            p.executeUpdate();
        } catch (final SQLException e) {
            ExceptionUtils.fatalError(Blackboard.class, e);
        }
    }

    /**
     * Get a message for Device device. Searches the database for all messages
     * which are destined to device returning the first occurence.
     *
     * @param device
     *            the device to find messages for
     *
     * @return the first ACLMessage found within the blackboard
     *
     */
    public static ACLMessage getMessage(final Device device) {
        getConnection();
        ACLMessage m = new ACLMessage(MessageType.EMPTY);
        try {
            final PreparedStatement preparedStatement = getMessageStmt;
            preparedStatement.setInt(1, DeviceID.getId(device.getId()));
            final ResultSet rs = preparedStatement.executeQuery();
            final int to = rs.getInt(0);
            final int from = rs.getInt(1);
            final int actionid = rs.getInt(2);
            final int msgType = rs.getInt(3);
            final String content = rs.getString(4);
            final double value = rs.getDouble(5);
            m = new ACLMessage(MessageType.values()[msgType]);
            m.setReciever(DeviceID.values()[to]);
            m.setSender(DeviceID.values()[from]);
            m.setActionID(ActionId.values()[actionid]);
            m.setContent(content);
            m.setValue(value);
        } catch (final SQLException e) {
            ExceptionUtils.fatalError(Blackboard.class, e);
        }
        return m;
    }

}
