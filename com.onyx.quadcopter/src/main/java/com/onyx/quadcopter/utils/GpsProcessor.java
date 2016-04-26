package com.onyx.quadcopter.utils;

import com.onyx.common.utils.Constants;
import com.onyx.quadcopter.devices.GpsDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * GPS Processor.
 * @author fred
 *
 */
public class GpsProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(GpsDevice.class);
  
  /**
   * Command array.
   */
  private static final String[] CMD_ARRAY = {"/usr/bin/gpscat", Constants.GPS_DEVICE}; 
    
  /**
   * The process builder for this Gps Processor.
   */
  private ProcessBuilder processBuilder;

  /**
   * Process handle.
   */
  private Process process;
  
  /**
   * Create a new GPS Processor.
   */
  public GpsProcessor() {
    processBuilder = new ProcessBuilder(CMD_ARRAY);
    LOGGER.debug("GPS Processor: created");
  }
  
  /**
   * Start the gps processor.
   */
  public void start() {
    try {
      process = processBuilder.start();
      LOGGER.debug("GPS Processor: started");
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
  
  /**
   * Stop this gps processor.
   */
  public void stop() {
    process.destroy();
    LOGGER.debug("GPS Processor: stopped");
  }
  
  /**
   * Get the input stream for the GPS process.
   * @return
   *    the input stream for the GPS process.
   */
  public InputStream getInputStream() {
    return process.getInputStream();
  }
  
  /**
   * Get the output stream for the GPS process.
   * @return
   *    the output stream for the GPS process.
   */
  public OutputStream getOutputStream() {
    return process.getOutputStream();
  }

}
