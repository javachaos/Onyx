package com.onyx.quadcopter.devices;

import com.onyx.quadcopter.messaging.ACLMessage;

public interface Updateable {

    /**
     * Updated device is guarenteed to have at least one
     * ACLMessage recieved.
     * 
     * Execute an update given the recieved ACLMessage msg.
     * 
     * @param msg
     * 		the message to be updated with.
     */
    void update(final ACLMessage msg);
}
