package com.onyx.quadcopter.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnyxScheduledExecutor extends ScheduledThreadPoolExecutor {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(OnyxScheduledExecutor.class);

    public OnyxScheduledExecutor(final int corePoolSize) {
        super(corePoolSize);
        setRemoveOnCancelPolicy(true);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
            final TimeUnit unit) {
        return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
            final TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
    }

    private Runnable wrapRunnable(final Runnable command) {
        return new LogOnExceptionRunnable(command);
    }

    private class LogOnExceptionRunnable implements Runnable {
        private final Runnable theRunnable;

        public LogOnExceptionRunnable(final Runnable theRunnable) {
            super();
            this.theRunnable = theRunnable;
        }

        @Override
        public void run() {
            try {
                theRunnable.run();
            } catch (final Throwable e) {
                LOGGER.error("General Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

}
