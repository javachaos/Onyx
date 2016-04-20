package com.onyx.commander.gui;

import java.nio.ByteBuffer;

import com.sun.jna.Memory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class VLCDirectRendering extends BorderPane implements Runnable {
    
    /**
     * Set this to <code>true</code> to resize the display to the dimensions of the
     * video, otherwise it will use {@link #WIDTH} and {@link #HEIGHT}.
     */
    private static final boolean useSourceSize = true;

    /**
     * Target width, unless {@link #useSourceSize} is set.
     */
    private static final int WIDTH = 640;

    /**
     * Target height, unless {@link #useSourceSize} is set.
     */
    private static final int HEIGHT = 480;

    /**
     * Frames per second
     */
    private static final double FPS = 24.0;

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

    private boolean isInit = false;
    
    public VLCDirectRendering(String url) {
	super();
	this.url = url;
        canvas = new Canvas();
        pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        pixelFormat = PixelFormat.getByteBgraInstance();
        setCenter(canvas);
    }
    
    public void init() {
	isInit = true;
        mediaPlayerComponent = new FXMediaPlayerComponent();
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        double duration = 1000.0 / FPS;
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration), nextFrameHandler));
    }
    
    public void stop() {
	timeline.stop();
	mediaPlayerComponent.getMediaPlayer().stop();
        mediaPlayerComponent.getMediaPlayer().release();
    }
    
    public DirectMediaPlayerComponent getMediaPlayer() {
	return mediaPlayerComponent;
    }
    /**
     * Implementation of a direct rendering media player component that renders
     * the video to a JavaFX canvas.
     */
    private class FXMediaPlayerComponent extends DirectMediaPlayerComponent {

        public FXMediaPlayerComponent() {
            super(new FXBufferFormatCallback());
        }
    }

    /**
     * Callback to get the buffer format to use for video playback.
     */
    private class FXBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            final int width;
            final int height;
            if (useSourceSize) {
                width = sourceWidth;
                height = sourceHeight;
            }
            else {
                width = WIDTH;
                height = HEIGHT;
            }
            Platform.runLater(new Runnable () {
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
     *
     */
    protected final void renderFrame() {
        Memory[] nativeBuffers = mediaPlayerComponent.getMediaPlayer().lock();
        if (nativeBuffers != null) {
            // FIXME there may be more efficient ways to do this...
            // Since this is now being called by a specific rendering time, independent of the native video callbacks being
            // invoked, some more defensive conditional checks are needed
            Memory nativeBuffer = nativeBuffers[0];
            if (nativeBuffer != null) {
                ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
                BufferFormat bufferFormat = ((DefaultDirectMediaPlayer) mediaPlayerComponent.getMediaPlayer()).getBufferFormat();
                if (bufferFormat.getWidth() > 0 && bufferFormat.getHeight() > 0) {
                    pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                }
            }
        }
        mediaPlayerComponent.getMediaPlayer().unlock();
        try {
	    Thread.sleep(1);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }
    
    private final EventHandler<ActionEvent> nextFrameHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent t) {
            renderFrame();
        }
    };

    private Timeline timeline;
    private String url;

    @Override
    public void run() {
	if (!isInit) {
	    init();
	}
	mediaPlayerComponent.getMediaPlayer().playMedia(url);
        mediaPlayerComponent.getMediaPlayer().setPosition(0.7f);
	timeline.playFromStart();	
    }

}
