package com.onyx.quadcopter.devices;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

/**
 * Camera Device.
 * @author fred
 *
 */
public class CameraDevice extends Device {

    /**
     * Webcam instance.
     */
    private Webcam webcam;
    private int imgIndex = 0;
    
    /**
     * Camera Constructor.
     * @param c
     */
    public CameraDevice(final Controller c) {
	super(c, DeviceID.CAMERA);
    }

    @Override
    protected void update() {
    }

    @Override
    protected void init() {
	try {
	    webcam = Webcam.getDefault(Constants.WEBCAM_TIMEOUT, TimeUnit.SECONDS);
	} catch (WebcamException | TimeoutException e) {
	    LOGGER.error("Could not instantiate webcam device. Error: " + e.getMessage());
	}
    }

    @Override
    public void shutdown() {
	webcam.close();
    }

    @Override
    protected void alternate() {
	try {
	    ImageIO.write(webcam.getImage(), "PNG", 
		new File(Constants.IMG_DIR+File.separator + "img" + (imgIndex++) + ".png"));
	} catch (IOException e) {
	    LOGGER.error(e.getMessage());
	}
    }

    @Override
    public boolean selfTest() {
	return webcam.open();
    }

}
