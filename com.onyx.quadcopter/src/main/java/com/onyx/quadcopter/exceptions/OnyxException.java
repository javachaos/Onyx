package com.onyx.quadcopter.exceptions;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class OnyxException extends RuntimeException {

    /**
     * Serial Version ID.
     */
    private static final long serialVersionUID = -2201072108555626457L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OnyxException.class);

    public OnyxException(final String errorMsg, final Logger l) {
        super(errorMsg);
        l.error(errorMsg);
    }

    public OnyxException(final String errorMsg) {
        this(errorMsg, LOGGER);
    }

}
