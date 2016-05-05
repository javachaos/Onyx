package com.onyx.quadcopter.exceptions;

import org.slf4j.Logger;

public class OnyxException extends RuntimeException {

  /**
   * Serial Version ID.
   */
  private static final long serialVersionUID = -2201072108555626457L;

  public OnyxException(final String errorMsg, final Logger log) {
    super(errorMsg);
    log.error(errorMsg);
  }

  public OnyxException(final Throwable throwable, final Logger log) {
    super(throwable);
    log.error(throwable.getMessage());
  }

}
