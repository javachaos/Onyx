package com.onyx.quadcopter.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.messaging.ACLMessage;
import com.onyx.quadcopter.utils.Constants;

public abstract class Device implements Executable {

    /**
     * Logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(Device.class);

    /**
     * Controller reference.
     */
    private final Controller controller;

    /**
     * Keep track of the number of loops.
     */
    private int runCounter = 0;

    private DeviceID id;
    /**
     * True when the device has been initialized.
     */
    protected boolean initialized = false;

    /**
     * The last message posted to the black board for this agent.
     */
    protected volatile ACLMessage lastMessage, previousMessage;

    /**
     * The human readable name for this device.
     */
    private String name;

    /**
     * Create a new device.
     * @param c
     * @param id
     */
    public Device(final Controller c, final DeviceID id) {
        setId(id);
        setName(id.toString());
        if (c != null) {
            controller = c;
        } else {
            LOGGER.error("Device could not be constructed, controller null.");
            throw new OnyxException("Device could not be constructed, controller null.", LOGGER);
        }
    }

    /**
     * Set the name of this device.
     * @param n
     */
    private void setName(final String n) {
        if ((n != null) && (n.length() > 0)) {
            name = n;
        }
    }

    /**
     * Update this device given a reference to the blackboard.
     *
     * @param b
     *            Reference to the blackboard.
     *
     */
    protected abstract void update();

    @Override
    public synchronized void execute() {
        if (!isInitialized()) {
            init();
            initialized = true;
        }
        previousMessage = lastMessage;
        lastMessage = getController().getBlackboard().getMessage(this);
        update();
        runCounter++;
        if (runCounter == Constants.ALTERNATE_SPEED) {
            runCounter = 0;
            LOGGER.debug("Device heartbeat: " + getName() + ".");
            alternate();
        }
    }

    /**
     * Return true if there is a new message.
     * @return
     */
    protected boolean isNewMessage() {
        if ((previousMessage == null) && (lastMessage instanceof ACLMessage)) {
            return lastMessage.isValid() && lastMessage.getReciever() == getId();
        }
        return !previousMessage.equals(lastMessage) 
        	&& lastMessage.isValid();
    }

    /**
     * Return the most recent ACL message.
     *
     * @return the most recent ACL message.
     */
    public ACLMessage getLastACLMessage() {
        return lastMessage;
    }

    /**
     * True if device init() method has been called at least once.
     *
     * @return if the init() method has been called at least once.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize the device.
     */
    protected abstract void init();

    /**
     * Shutdown this device.
     */
    public abstract void shutdown();

    /**
     * Called very ALTERNATE_SPEED, updates. Sort of like a slow update.
     */
    protected abstract void alternate();

    /**
     * Run self test code to ensure everything works.
     *
     * @return true if everything is OK
     */
    public abstract boolean selfTest();

    /**
     * Return the device ID.
     *
     * @return
     */
    public DeviceID getId() {
        return id;
    }

    /**
     * Set the device id.
     *
     * @param id
     */
    private void setId(final DeviceID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Get the name of this device.
     * @return
     */
    private String getName() {
        return name;
    }

    /**
     * Get a reference to the controller.
     * @return
     */
    protected Controller getController() {
        return controller;
    }
}
