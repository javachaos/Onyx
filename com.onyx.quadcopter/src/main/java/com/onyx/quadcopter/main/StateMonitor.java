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
package com.onyx.quadcopter.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onyx.quadcopter.utils.ThreadUtils;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * State monitor to monitor the application state.
 *
 * @author fred
 *
 */
public final class StateMonitor implements TimerTask {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(StateMonitor.class);

    /**
     * The exit status.
     */
    private int status = 0;
    /**
     * The app state manager.
     */
    private static OnyxState state;

    /**
     * The previous application state.
     */
    private OnyxState previousState = null;

    /**
     * True if the application state has changed since the last update.
     */
    private boolean stateChanged;

    /**
     * True if the monitor is running.
     */
    private volatile boolean isRunning = false;

    /**
     * Controller instance.
     */
    private Controller controller;

    /**
     * State monitor constructor.
     */
    public StateMonitor(final Controller c) {
        if (c != null) {
            controller = c;
        }
        init();
    }

    /**
     * Return true if the state monitor is running.
     *
     * @return true if the monitor is running.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Initialize the state monitor.
     */
    private void init() {
        isRunning = true;
        state = OnyxState.STARTUP;
        previousState = OnyxState.SHUTDOWN;
    }

    /**
     * Called every application update cycle. to ensure the application state is
     * correct.
     */
    public void update() {
        checkState();
        switch (state) {
        case AIRBORNE:
            doAirborne();
            break;
        case LANDED:
            doLanded();
            break;
        case LANDING:
            doLanding();
            break;
        case ERROR:
            doError();
            break;
        case RECOVERY:
            doRecover();
            break;
        case SHUTDOWN:
            doShutdown();
            break;
        case STARTUP:
            doStartup();
            break;
        default:
            break;
        }

        if (isStateChanged()) {
            previousState = state;
        }
    }

    /**
     * Run the state monitor.
     */
    @Override
    public void run(Timeout t) {
        while (isRunning) {
            update();
        }
    }

    /**
     * True if the state has changed since the last update.
     *
     * @return true if the state has changed.
     */
    public boolean isStateChanged() {
        return stateChanged;
    }

    /**
     * Set the stateChanged variable.
     *
     * @param s
     *            the state to be set.
     */
    private void setStateChanged(final boolean s) {
        stateChanged = s;
    }

    /**
     * Return the state of the application.
     *
     * @return the state of the application.
     */
    public static OnyxState getState() {
        return state;
    }

    /**
     * The aircraft in landed.
     */
    public static void landedState() {
        state = OnyxState.LANDED;
    }

    /**
     * The aircraft is landing.
     */
    public static void landingState() {
        state = OnyxState.LANDING;
    }

    /**
     * The aircraft is in flight.
     */
    public static void airborneState() {
        state = OnyxState.AIRBORNE;
    }

    /**
     * Shutdown state.
     */
    public static void shutdownState() {
        state = OnyxState.SHUTDOWN;
    }

    /**
     * Error state.
     */
    public static void errorState() {
        state = OnyxState.ERROR;
    }

    /**
     * Startup state.
     */
    public static void startupState() {
        state = OnyxState.STARTUP;
    }

    /**
     * Startup state.
     */
    public static void recoveryState() {
        state = OnyxState.RECOVERY;
    }

    /**
     * Error state.
     */
    private void doError() {
        status = -1;
        state = OnyxState.RECOVERY;
    }

    /**
     * Shutdown the application.
     */
    private void doShutdown() {
        switch (previousState) {
        case AIRBORNE:
            if (state == OnyxState.LANDED) {
                controller.stop();
                exit();
            }
            break;
        case ERROR:
            doRecover();
            break;
        case LANDED:
            if (state == previousState) {
                controller.stop();
                exit();
            }
            break;
        case LANDING:
            if (state == OnyxState.LANDED) {
                controller.stop();
                exit();
            }
            break;
        case RECOVERY:
            break;
        case SHUTDOWN:
            if (state == previousState) {
                controller.stop();
                exit();
            }
            break;
        case STARTUP:
            break;
        default:
            break;
        }
    }

    /**
     * Attempt to recover from database corruption error.
     */
    private void doRecover() {
        if (isStateChanged()) {
            switch (previousState) {
            case AIRBORNE:
                break;
            case ERROR:
                break;
            case LANDED:
                break;
            case LANDING:
                break;
            case RECOVERY:
                break;
            case SHUTDOWN:
                break;
            case STARTUP:
                break;
            default:
                break;

            }
        }
    }

    /**
     * Do Startup state.
     */
    private void doStartup() {
        if (isStateChanged()) {
            Main.appStart(controller);
        }
    }

    private void doLanding() {
        if (isStateChanged()) {

        }
    }

    private void doLanded() {
        if (isStateChanged()) {
            // TODO Complete
        }
    }

    private void doAirborne() {
        if (isStateChanged()) {
            // TODO Complete
        }
    }

    /**
     * Check if the state has changed.
     */
    private void checkState() {
        if (state != previousState) {
            setStateChanged(true);
            LOGGER.debug("Entering " + state.name() + " state.");
        } else if (state == previousState) {
            setStateChanged(false);
        }
    }

    /**
     * Exit the application normally.
     */
    public void exit() {
        isRunning = false;
        LOGGER.info("Application exiting.");
        ThreadUtils.shutdown();
        // Main.COORDINATOR.newTimeout(new ShutdownAgent(Main.COORDINATOR), Constants.SLEEP_TIME, TimeUnit.MILLISECONDS);
        System.exit(status);
    }
}
