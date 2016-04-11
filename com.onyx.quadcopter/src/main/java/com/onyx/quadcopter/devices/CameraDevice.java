package com.onyx.quadcopter.devices;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import com.onyx.quadcopter.main.Controller;
import com.onyx.quadcopter.utils.Constants;
import com.onyx.quadcopter.utils.ExceptionUtils;

/**
 * Camera Device.
 * 
 * @author fred
 *
 */
public class CameraDevice extends Device {

    static {
	try {
	    System.load(Constants.CAM_NATIVE_LIB);
	} catch (final UnsatisfiedLinkError e) {
	    ExceptionUtils.logError(CameraDevice.class, e);
	}
    }

    /**
     * Webcam instance.
     */
    private VideoCapture webcam;

    /**
     * The matrix.
     */
    private Mat matrix;

    /**
     * Camera Constructor.
     * 
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
	webcam.open("http://192.168.1.163:8080/?dummy=param.mjpg");
    }

    @Override
    public void shutdown() {
	webcam.release();
    }

    @Override
    protected void alternate() {
	matrix = new Mat();
	while (webcam.read(matrix) == false)
	    ;
	String fileName = Constants.IMG_DIR + File.separator + "img_latest.png";
	Highgui.imwrite(fileName, matrix);
	matrix.release();
    }

    @Override
    public boolean selfTest() {
	return webcam.isOpened();
    }

}
