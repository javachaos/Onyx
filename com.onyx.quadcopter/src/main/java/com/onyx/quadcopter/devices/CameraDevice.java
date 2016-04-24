package com.onyx.quadcopter.devices;

import com.onyx.common.messaging.AclMessage;
import com.onyx.common.messaging.DeviceId;
import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import java.io.File;

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
    } catch (final UnsatisfiedLinkError e1) {
      ExceptionUtils.logError(CameraDevice.class, e1);
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
   */
  public CameraDevice() {
    super(DeviceId.CAMERA);
  }

  @Override
  public void update(AclMessage msg) {}

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
    while (webcam.read(matrix) == false) {
    }
    String fileName = Constants.IMG_DIR + File.separator + "img_latest.png";
    Highgui.imwrite(fileName, matrix);
    matrix.release();
  }

  @Override
  public boolean selfTest() {
    return webcam.isOpened();
  }

}
