package com.onyx.quadcopter.devices;

import com.onyx.quadcopter.main.Controller;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class RedButton extends Device implements GpioPinListenerDigital {
    
    /**
     * Push Button.
     */
    private GpioPinDigitalInput button;
    
    public RedButton(final Controller c) {
	super(c, DeviceID.RED_BUTTON);
    }

    @Override
    protected void update() {
    }

    @Override
    protected void init() {
        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        button = getController().getGpio().provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_UP);
        button.addListener(this);
    }

    @Override
    public void shutdown() {
    }

    @Override
    protected void alternate() {
    }

    @Override
    protected boolean selfTest() {
	return getController().getGpio().isState(PinState.HIGH, button);
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent state) {
	LOGGER.debug("Button State: " + state.getState().getName());
	if (state.getState() == PinState.LOW) {
	    LOGGER.debug("Button Pressed.");
	}
    }

}
