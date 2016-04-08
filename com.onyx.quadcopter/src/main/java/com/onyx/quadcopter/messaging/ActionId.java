package com.onyx.quadcopter.messaging;

/**
 * Represents an ActionID for an ACL message.
 *
 * @author fredladeroute
 *
 */
public enum ActionId {

    /**
     * Null action ID.
     */
    NULL(0),

    /**
     * Land the craft. Content has location to land data.
     */
    LAND(1),

    /**
     * Lift off the ground and maintain stable flight above ground. Content has
     * hover altitude.
     */
    TAKEOFF(2),

    /**
     * The content contains orientation data.
     */
    ORIENT(3),

    /**
     * Provide CNC with altitude.
     */
    GET_HEIGHT(4),

    /**
     * Provide CNC with orientation.
     */
    GET_ORIENT(5),

    /**
     * Provide CNC with image.
     */
    GET_IMAGE(6),

    /**
     * Travel to GPS co-ordinate and altitude, provided in content.
     */
    GOTO(7),

    /**
     * Provide CNC with temperature reading.
     */
    GET_TEMP(8),

    /**
     * Print content to OLED.
     */
    PRINT(9),

    /**
     * Change the speed of the motor provided in content string.
     */
    CHANGE_MOTOR_SPEED(10),

    /**
     * Sent content string to client.
     */
    SEND_DATA(11),

    /**
     * No action is performed on the contents of the message.
     */
    NO_ACTION(12), 
    
    /**
     * Change the pulse width
     */
    CHANGE_PULSE_WIDTH(13);

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
    private ActionId(final int i) {
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
    public static int getId(final ActionId t) {
        return t.id;
    }
}