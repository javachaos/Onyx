package com.onyx.quadcopter.devices;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;

/**
 * Camera Device.
 * @author fred
 *
 */
public class CameraDevice extends Device {

    static {
        try {
            System.load(Constants.CAM_NATIVE_LIB);
        } catch (final UnsatisfiedLinkError e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Webcam instance.
     */
    private VideoCapture webcam;

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
        webcam = new VideoCapture(0);
    }

    @Override
    public void shutdown() {
        webcam.release();
    }

    @Override
    protected void alternate() {
        Mat m = new Mat();
        if (webcam.grab()) {
            while (webcam.read(m) == false);
	    String fileName = Constants.IMG_DIR + File.separator + "img_latest.png";
	    Highgui.imwrite(fileName, m);
	}
    }

    @Override
    public boolean selfTest() {
	return webcam.isOpened();
    }

}
