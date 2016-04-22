package com.onyx.commander.gui;

import com.sun.jna.Memory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DefaultDirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.nio.ByteBuffer;


/**
 * Vlc Direct rendering pane.
 * 
 * @author fred
 *
 */
public final class VlcDirectRenderingPane extends BorderPane implements Runnable {

  /**
   * Set this to <code>true</code> to resize the display to the dimensions of the video, otherwise
   * it will use {@link #WIDTH} and {@link #HEIGHT}.
   */
  private static final boolean USE_SRC_SIZE = true;

  /**
   * Target width, unless {@link #USE_SRC_SIZE} is set.
   */
  private static final int WIDTH = 640;

  /**
   * Target height, unless {@link #USE_SRC_SIZE} is set.
   */
  private static final int HEIGHT = 480;

  /**
   * Frames per second.
   */
  private static final double FPS = 24.0;

  /**
   * Milliseconds per second.
   */
  private static final double MILLIS_PER_SEC = 1000.0;

  /**
   * Media position.
   */
  private static final float MEDIA_POS = 0.7f;

  /**
   * Lightweight JavaFX canvas, the video is rendered here.
   */
  private Canvas canvas;

  /**
   * Pixel writer to update the canvas.
   */
  private PixelWriter pixelWriter;

  /**
   * Pixel format.
   */
  private WritablePixelFormat<ByteBuffer> pixelFormat;

  /**
   * The vlcj direct rendering media player component.
   */
  private DirectMediaPlayerComponent mediaPlayerComponent;

  /**
   * True if this pane has been initialized.
   */
  private boolean isInit = false;

  /**
   * Create a new VlcDirectRenderingPane.
   * 
   * @param murl the URL of the media file.
   */
  public VlcDirectRenderingPane(final String murl) {
    super();
    this.url = murl;
    canvas = new Canvas();
    pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
    pixelFormat = PixelFormat.getByteBgraInstance();
    setCenter(canvas);
  }

  /**
   * Initialize this Pane.
   */
  public void init() {
    isInit = true;
    mediaPlayerComponent = new FxMediaPlayerComponent();
    timeline = new Timeline();
    timeline.setCycleCount(Timeline.INDEFINITE);
    double duration = MILLIS_PER_SEC / FPS;
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration), e -> renderFrame()));
  }

  /**
   * Stop this media pane.
   */
  public void stop() {
    timeline.stop();
    mediaPlayerComponent.getMediaPlayer().stop();
    mediaPlayerComponent.getMediaPlayer().release();
  }

  /**
   * Get the media player.
   * 
   * @return the media player instance.
   */
  public DirectMediaPlayerComponent getMediaPlayer() {
    return mediaPlayerComponent;
  }

  /**
   * Implementation of a direct rendering media player component that renders the video to a JavaFX
   * canvas.
   */
  private class FxMediaPlayerComponent extends DirectMediaPlayerComponent {

    /**
     * Create a new FXMediaPlayer.
     */
    FxMediaPlayerComponent() {
      super(new FxBufferFormatCallback());
    }
  }

  /**
   * Callback to get the buffer format to use for video playback.
   */
  private class FxBufferFormatCallback implements BufferFormatCallback {

    @Override
    public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
      final int width;
      final int height;
      if (USE_SRC_SIZE) {
        width = sourceWidth;
        height = sourceHeight;
      } else {
        width = WIDTH;
        height = HEIGHT;
      }
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          canvas.setWidth(width);
          canvas.setHeight(height);
        }
      });
      return new RV32BufferFormat(width, height);
    }
  }

  /**
   * Render the next frame.
   */
  protected void renderFrame() {
    Memory[] nativeBuffers = mediaPlayerComponent.getMediaPlayer().lock();
    if (nativeBuffers != null) {
      // FIXME there may be more efficient ways to do this...
      // Since this is now being called by a specific rendering time,
      // independent of the native video callbacks being
      // invoked, some more defensive conditional checks are needed
      Memory nativeBuffer = nativeBuffers[0];
      if (nativeBuffer != null) {
        ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
        BufferFormat bufferFormat =
            ((DefaultDirectMediaPlayer) mediaPlayerComponent.getMediaPlayer()).getBufferFormat();
        if (bufferFormat.getWidth() > 0 && bufferFormat.getHeight() > 0) {
          pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(),
              pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
        }
      }
    }
    mediaPlayerComponent.getMediaPlayer().unlock();
  }

  /**
   * Media Timeframe.
   */
  private Timeline timeline;

  /**
   * Media URL.
   */
  private String url;

  @Override
  public void run() {
    if (!isInit) {
      init();
    }
    mediaPlayerComponent.getMediaPlayer().playMedia(url);
    mediaPlayerComponent.getMediaPlayer().setPosition(MEDIA_POS);
    timeline.playFromStart();
  }

}
