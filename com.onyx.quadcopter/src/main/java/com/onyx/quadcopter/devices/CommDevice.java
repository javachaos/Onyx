package com.onyx.quadcopter.devices;

import com.onyx.quadcopter.exceptions.OnyxException;
import com.onyx.quadcopter.main.Controller;

public abstract class CommDevice extends Device {

    public CommDevice(final Controller c) throws OnyxException {
        super(c, DeviceID.DATA_TRANSMITTER);
    }
}
