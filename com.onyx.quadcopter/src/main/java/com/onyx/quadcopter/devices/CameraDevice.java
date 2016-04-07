package com.onyx.quadcopter.devices;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
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
    private Webcam webcam = Webcam.getDefault();
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
	return true;
    }

}
